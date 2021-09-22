package org.minos.loadbalance;

import com.netflix.loadbalancer.Server;
import com.netflix.niws.loadbalancer.DiscoveryEnabledServer;
import org.minos.core.consts.ContextConstraints;
import org.minos.core.consts.EurekaMetadataConstraints;
import org.minos.core.consts.LoggerNames;
import org.minos.core.consts.ServiceVersions;
import org.minos.core.context.ContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * MinosServerChooser
 *
 * @date 2019/9/18
 */
public class MinosServerChooser implements ServerChooser {

    private static Logger logger = LoggerFactory.getLogger(LoggerNames.LOAD_BALANCE);

    private AtomicInteger counter = new AtomicInteger();

    private static final int MAX_RETRY = 10;

    private ContextHolder contextHolder;

    public MinosServerChooser(ContextHolder contextHolder) {
        this.contextHolder = contextHolder;
    }

    @Override
    public Server choose(List<Server> serverList) {
        serverList = filter(serverList);
        return chooseServer(serverList);
    }

    /**
     * 对服务实例列表做过滤
     * <p>
     * <p>
     * 微服务实例的版本，是通过配置{@code eureka.instance.metadata-map.KRY_LABEL_VERSION}。当配置项没有值，
     * 则认为是稳定版。网关标记的流量版本也同理。
     * <p>
     * 过滤规则是：
     * 当流量是稳定版本，只过滤稳定版。
     * 当流量有指定版本，优先过滤对应版本，当对应版本没有在线实例时，转而选择稳定版。
     *
     * @param serverList
     * @return
     */
    private List<Server> filter(List<Server> serverList) {
        if (isStableRequest()) {
            logger.debug("Request version: stable");

            return stableServers(serverList);
        } else {
            String version = (String) contextHolder.get(ContextConstraints.KRY_LABEL_VERSION);

            logger.debug("Request version: {}", version);

            return specialServers(serverList, version);
        }
    }

    private List<Server> stableServers(List<Server> serverList) {
        List<Server> result = new ArrayList<>();
        for (Server server : serverList) {
            if (isStableServer(server)) {
                result.add(server);
            }
        }

        return result;
    }

    private List<Server> specialServers(List<Server> serverList, String version) {
        List<Server> result = new ArrayList<>();
        for (Server server : serverList) {
            //只添加存活的实例，确保当对应版本的实例都不存活的时候，还能降级选择稳定版本
            if (isSpecialServer(server, version) && isActiveServer(server)) {
                result.add(server);
            }
        }

        if (result.isEmpty()) {
            logger.debug("Can not find any server with version {}, select stable servers", version);

            return stableServers(serverList);
        }

        return result;
    }

    /**
     * 基于RoundRobin算法，轮询选择一个可用的实例
     *
     * @param serverList
     * @return
     */
    private Server chooseServer(List<Server> serverList) {
        if (CollectionUtils.isEmpty(serverList)) {
            logger.debug("Server list is empty, choose a null value");
            return null;
        }

        int retry = 0;
        Server server = null;
        while (server == null && retry++ < MAX_RETRY) {

            int index = getIndex(serverList.size());
            server = serverList.get(index);

            if (server == null) {
                Thread.yield();
                continue;
            }

            if (isActiveServer(server)) {
                return server;
            }

            // Next.
            server = null;
        }

        if (retry >= MAX_RETRY) {
            logger.debug("Choose loop exceeds max retries, throw exception");
            throw new NoAvailableServersException();
        }

        return server;
    }

    private int getIndex(int size) {
        int current = counter.incrementAndGet();
        current = Math.abs(current);
        return current % size;
    }

    private boolean isStableRequest() {
        return !contextHolder.containsKey(ContextConstraints.KRY_LABEL_VERSION)
                || ServiceVersions.STABLE.equals(contextHolder.get(ContextConstraints.KRY_LABEL_VERSION));
    }

    private boolean isStableServer(Server server) {
        if (!(server instanceof DiscoveryEnabledServer)) {
            return false;
        }

        DiscoveryEnabledServer des = (DiscoveryEnabledServer) server;
        Map<String, String> metadata = des.getInstanceInfo().getMetadata();

        String lowerVersion = metadata.get(EurekaMetadataConstraints.KRY_LABEL_VERSION);
        String upperVersion = metadata.get(EurekaMetadataConstraints.KRY_LABEL_VERSION_UPPERCASE);

        return (lowerVersion == null && upperVersion == null)
                || ServiceVersions.STABLE.equals(lowerVersion)
                || ServiceVersions.STABLE.equals(upperVersion);
    }

    private boolean isSpecialServer(Server server, String version) {
        if (!(server instanceof DiscoveryEnabledServer)) {
            return false;
        }

        DiscoveryEnabledServer des = (DiscoveryEnabledServer) server;
        String serverVersion = des
                .getInstanceInfo()
                .getMetadata()
                .get(EurekaMetadataConstraints.KRY_LABEL_VERSION);
        if (StringUtils.isEmpty(serverVersion)) {
            serverVersion = des
                    .getInstanceInfo()
                    .getMetadata()
                    .get(EurekaMetadataConstraints.KRY_LABEL_VERSION_UPPERCASE);
        }
        return Objects.equals(version, serverVersion);
    }

    private boolean isActiveServer(Server server) {
        return server.isAlive() && server.isReadyToServe();
    }
}
