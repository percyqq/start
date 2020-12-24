package org.minos.discover.client;

import com.netflix.appinfo.ApplicationInfoManager;
import com.netflix.discovery.AbstractDiscoveryClientOptionalArgs;
import com.netflix.discovery.DiscoveryClient;
import com.netflix.discovery.EurekaClientConfig;
import com.netflix.discovery.shared.Applications;
import com.netflix.discovery.shared.resolver.EurekaEndpoint;
import com.netflix.discovery.shared.resolver.aws.AwsEndpoint;
import com.netflix.discovery.shared.resolver.aws.ConfigClusterResolver;
import org.minos.discover.client.eureka.EurekaServerWatcher;
import org.minos.discover.client.eureka.ServerLongPollingResponse;
import org.minos.discover.client.listener.DiscoveryCacheEventPublisher;
import org.minos.discover.client.listener.DiscoveryCacheRefreshEvent;
import org.springframework.cloud.netflix.eureka.CloudEurekaClient;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.util.ReflectionUtils;

import javax.annotation.PreDestroy;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;

import static org.minos.discover.client.common.LoggerUtils.CLIENT_LOGGER;

/**
 * 定制化Client，提供以下功能
 * 1. 支持long polling功能，从server获取变化的instance信息
 *
 */
public class MinosEurekaClient extends CloudEurekaClient implements MinosServiceInstance {

    /**
     * Eureka DiscoveryClient字段，通过反射获取到
     */
    private Lock fetchRegistryUpdateLock;
    private AtomicReference<Applications> localRegionApps;
    private Method filterAndShuffleMethod;
    private Method updateDeltaMethod;

    /**
     * listener
     */
    private final DiscoveryCacheEventPublisher cacheEventPublisher;

    /**
     * current client epoch version
     */
    private final Map<EurekaEndpoint, AtomicLong> serversEpoch = new HashMap<>();

    /**
     * client关闭标记
     */
    private final AtomicBoolean minosEurekaIsShutdown = new AtomicBoolean(false);


    private EurekaServerWatcher eurekaServerWatcher;

    public MinosEurekaClient(
            ApplicationInfoManager applicationInfoManager,
            EurekaClientConfig config,
            MinosEurekaClientConfigBean minosConfig,
            AbstractDiscoveryClientOptionalArgs<?> args,
            ApplicationEventPublisher publisher,
            DiscoveryCacheEventPublisher cacheEventPublisher
            ) throws IllegalAccessException {
        super(applicationInfoManager, config, args, publisher);
        CLIENT_LOGGER.info("EurekaClient shouldFetchRegistry: " + config.shouldFetchRegistry() + ", MinosEurekaClient: " + minosConfig.isEnable());

        this.cacheEventPublisher = cacheEventPublisher;

        if (minosConfig.isEnable()) {
            fetchSuperClassField();

            List<AwsEndpoint> endpointList =
                    new ConfigClusterResolver(config, this.getApplicationInfoManager().getInfo()).getClusterEndpoints();

            //启动eureka watcher
            this.eurekaServerWatcher = new EurekaServerWatcher("MinosEurekaClient", this, minosConfig, endpointList);
            this.eurekaServerWatcher.registerListener(this::updateApplications);
            this.eurekaServerWatcher.start();
        }
    }

    /**
     * 通过反射获取基类的部分字段
     */
    private void fetchSuperClassField() throws IllegalAccessException {

        Field fetchRegistryUpdateLockField = ReflectionUtils.findField(DiscoveryClient.class, "fetchRegistryUpdateLock");
        fetchRegistryUpdateLockField.setAccessible(true);
        this.fetchRegistryUpdateLock = (Lock) fetchRegistryUpdateLockField.get(this);


        Field localRegionAppsField = ReflectionUtils.findField(DiscoveryClient.class, "localRegionApps");
        localRegionAppsField.setAccessible(true);
        this.localRegionApps = (AtomicReference<Applications>) localRegionAppsField.get(this);


        this.filterAndShuffleMethod = ReflectionUtils.findMethod(DiscoveryClient.class, "filterAndShuffle", Applications.class);
        this.filterAndShuffleMethod.setAccessible(true);

        this.updateDeltaMethod = ReflectionUtils.findMethod(DiscoveryClient.class, "updateDelta", Applications.class);
        this.updateDeltaMethod.setAccessible(true);
    }


    private void updateApplications(final ServerLongPollingResponse serverRsp) {
        if (serverRsp.getRcvEpoch() < 0 || serverRsp.getApplications() == null) {
            return;
        }

        try {
            if (fetchRegistryUpdateLock.tryLock(1, TimeUnit.SECONDS)) {
                try {
                    EurekaEndpoint server = serverRsp.getServer();
                    AtomicLong serverEpoch = serversEpoch.computeIfAbsent(server, data -> new AtomicLong(0));

                    //epoch被更新过，或者 返回的epoch = 请求的epoch 不更新
                    if (serverRsp.getRcvEpoch() == serverRsp.getReqEpoch()) {
                        return;
                    }

                    serverEpoch.set(serverRsp.getRcvEpoch());

                    CLIENT_LOGGER.debug("Eureka client apps server [" + server.getHostName() + ":" + server.getPort()
                            + "] epoch set to [" + serverRsp.getRcvEpoch() + "] delta[" + serverRsp.isDelta() + "]");


                    if (serverRsp.isDelta()) {
                        if (serverRsp.getApplications() != null) {
                            if (CLIENT_LOGGER.isTraceEnabled()) {
                                serverRsp.getApplications().forEach(app -> {
                                    app.getInstances().forEach(i -> {
                                        CLIENT_LOGGER.trace(i.getInstanceId() + ":" + i.getActionType() + ":" + i.getStatus());
                                    });
                                });
                            }
                            updateDeltaMethod.invoke(this, new Applications("", 0L, serverRsp.getApplications()));
                        }
                    } else {
                        Applications applications = getApplications();
                        Applications newApplications;
                        if (applications != null) {
                            newApplications = new Applications(applications.getAppsHashCode(), applications.getVersion(), serverRsp.getApplications());
                        } else {
                            newApplications = new Applications("", 0L, serverRsp.getApplications());
                        }
                        localRegionApps.set((Applications) filterAndShuffleMethod.invoke(this, newApplications));
                    }

                    //发布Eureka更新事件，触发LB刷新缓存
                    cacheEventPublisher.fireEvent(new DiscoveryCacheRefreshEvent());

                } catch (IllegalAccessException | InvocationTargetException e) {
                    CLIENT_LOGGER.error("Refresh eureka client apps fail", e);
                } finally {
                    fetchRegistryUpdateLock.unlock();
                }
            } else {
                CLIENT_LOGGER.error("Cannot acquire update lock, aborting updateApplications");
            }
        } catch (InterruptedException e) {
            CLIENT_LOGGER.error("Cannot acquire update lock, aborting updateApplications");
        }
    }

    @PreDestroy
    @Override
    public synchronized void shutdown() {
        stopMinos();

        try {
            super.shutdown();
        } catch (Exception e) {
            CLIENT_LOGGER.error("shutdown base eureka client fail", e);
        }

    }

    private void stopMinos() {
        try {
            if (minosEurekaIsShutdown.compareAndSet(false, true)) {
                CLIENT_LOGGER.info("shutting down minos client");

                if (this.eurekaServerWatcher != null) {
                    this.eurekaServerWatcher.stop();
                }
                CLIENT_LOGGER.info("shutting down minos client complete");
            }
        } catch (Exception exception) {
            CLIENT_LOGGER.error("failed to shutting down minos client", exception);
        }
    }

    @Override
    public String serviceId() {
        return this.getApplicationInfoManager().getInfo().getAppName();
    }

    @Override
    public String instanceId() {
        return this.getApplicationInfoManager().getInfo().getInstanceId();
    }
}
