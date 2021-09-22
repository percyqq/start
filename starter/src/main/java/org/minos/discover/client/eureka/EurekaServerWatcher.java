package org.minos.discover.client.eureka;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.netflix.discovery.shared.resolver.EurekaEndpoint;
import org.minos.discover.client.MinosEurekaClientConfigBean;
import org.minos.discover.client.MinosServiceInstance;
import org.minos.discover.client.common.TimeUtils;
import org.springframework.context.Lifecycle;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

import static org.minos.discover.client.common.LoggerUtils.WATCHER_LOGGER;
import static org.minos.discover.client.eureka.ServerLongPollingResponse.Code.OK;

/**
 * @date 2020/6/16 14:43
 */
public class EurekaServerWatcher implements Lifecycle {
    private static final AtomicInteger ID_GENERATOR = new AtomicInteger(1);
    private final String id;
    private final MinosServiceInstance instance;

    private final EndPointSelector<? extends EurekaEndpoint> endpointSelector;
    /**
     * current client epoch version
     */
    private final Map<EurekaEndpoint, AtomicLong> serverEpochRecord;
    /**
     * watch Task
     */
    private final FetchTask forceFetchTask;
    private final FetchTask longPollingTask;
    /**
     * event listener
     */
    private final List<Consumer<ServerLongPollingResponse>> eventListener = Lists.newArrayList();
    private final Lock publishLock = new ReentrantLock();

    private Map<EurekaEndpoint, MinosEurekaHttpClient> eurekaHttpClientContainer = new ConcurrentHashMap<>();

    /**
     * 运行标记
     */
    private volatile boolean shutdown = false;


    public EurekaServerWatcher(MinosServiceInstance instance, MinosEurekaClientConfigBean config,
                               List<? extends EurekaEndpoint> endpointList) {
        this(null, instance, config, endpointList);
    }

    public EurekaServerWatcher(String id, MinosServiceInstance instance, MinosEurekaClientConfigBean config,
                               List<? extends EurekaEndpoint> endpointList) {
        if (StringUtils.isEmpty(id)) {
            this.id = String.valueOf(ID_GENERATOR.getAndAdd(1));
        } else {
            this.id = id;
        }
        this.instance = instance;
        this.endpointSelector = new StickyEndpointSelector<>(endpointList);
        this.serverEpochRecord = new ConcurrentHashMap<>();

        //HTTP CLIENT
        MinosEurekaHttpClientFactory httpClientFactory = new MinosEurekaHttpClientFactory();
        for (EurekaEndpoint endpoint : endpointList) {
            this.eurekaHttpClientContainer.put(endpoint, httpClientFactory.newClient(instance, endpoint, config.getLongPolling().getTimeout()));
            this.serverEpochRecord.put(endpoint, new AtomicLong(0));
        }


        //内部任务
        this.forceFetchTask =
                new FetchTask(getTaskName("ForceFetch"), false, TimeUtils.secToMilli(config.getForceFetch().getInterval()));
        this.longPollingTask = new FetchTask(getTaskName("LongPolling"), true, 1);
    }

    private String getTaskName(String task) {
        return Joiner.on("-").skipNulls().join(this.id, task);
    }

    public void registerListener(Consumer<ServerLongPollingResponse> callback) {
        this.eventListener.add(callback);
    }

    public void deregisterListener(Consumer<ServerLongPollingResponse> callback) {
        this.eventListener.remove(callback);
    }

    @Override
    public void start() {
        //启动强制同步一次，不能再调用start
        this.forceFetchTask.run();

        this.longPollingTask.start();
    }

    @Override
    public void stop() {
        this.shutdown = true;
        this.forceFetchTask.stop();
        this.longPollingTask.stop();
    }

    @Override
    public boolean isRunning() {
        return !shutdown;
    }

    private void recordEpoch(ServerLongPollingResponse response) {
        if (response.getCode() == OK && response.getRcvEpoch() > 0) {
            AtomicLong epoch = serverEpochRecord.computeIfAbsent(response.getServer(), data -> new AtomicLong(0));
            epoch.set(response.getRcvEpoch());
        }
    }


    private void publishEvent(ServerLongPollingResponse event) {
        if (!isRunning()) {
            return;
        }

        try {
            if (publishLock.tryLock(2, TimeUnit.SECONDS)) {
                try {
                    eventListener.forEach(callback -> {
                        try {
                            callback.accept(event);
                        } catch (Exception e) {
                            WATCHER_LOGGER.error("eureka " + id + " event callback fail", e);
                        }
                    });
                } finally {
                    publishLock.unlock();
                }
            }
        } catch (InterruptedException e) {
            WATCHER_LOGGER.error("eureka " + id + " publish event fail", e);
        }
    }

    /**
     * for test
     */
    protected void setHttpClient(Map<EurekaEndpoint, MinosEurekaHttpClient> httpClients) {
        this.eurekaHttpClientContainer = httpClients;
    }

    public MinosEurekaHttpClient getEurekaHttpClient(EurekaEndpoint endpoint) {
        return this.eurekaHttpClientContainer.get(endpoint);
    }

    public Map<EurekaEndpoint, MinosEurekaHttpClient> getEurekaHttpClient() {
        return this.eurekaHttpClientContainer;
    }

    protected EndPointSelector<? extends EurekaEndpoint> getEndpointSelector() {
        return this.endpointSelector;
    }

    private class FetchTask implements Runnable, Lifecycle {
        private final ScheduledExecutorService timer;
        private final String name;
        private final boolean delta;
        private final int interval;
        private volatile boolean shutdown = false;

        private FetchTask(String name, boolean delta, int interval) {
            this.name = name;
            this.delta = delta;
            this.interval = interval;

            this.timer = Executors.newScheduledThreadPool(1, r -> {
                Thread thread = new Thread(r, name);
                thread.setDaemon(true);
                return thread;
            });
        }

        @Override
        public void start() {
            this.timer.schedule(this, interval, TimeUnit.MILLISECONDS);
        }

        @Override
        public void stop() {
            this.shutdown = true;
            this.timer.shutdown();
        }

        @Override
        public boolean isRunning() {
            return !shutdown;
        }

        @Override
        public void run() {
            if (!isRunning()) {
                WATCHER_LOGGER.info("eureka watcher {} is closed!", name);
                return;
            }

            try {
                WATCHER_LOGGER.info("eureka watcher {} run", name);

                EurekaEndpoint serverEndpoint = endpointSelector.select();

                AtomicLong curEpoch = serverEpochRecord.computeIfAbsent(serverEndpoint, data -> new AtomicLong(0));

                ServerLongPollingResponse serverRsp =
                        getEurekaHttpClient(serverEndpoint).epoch(delta ? curEpoch.get() : 0, delta);

                if (serverRsp.getCode() == ServerLongPollingResponse.Code.ERROR ||
                        serverRsp.getCode() == ServerLongPollingResponse.Code.NOT_SUPPORT) {
                    WATCHER_LOGGER.warn("eureka watcher {} Force fetch apps error! Can't fetch apps from {}", name, serverEndpoint.toString());
                    //Server报错，强制重选server
                    endpointSelector.select(true);
                    scheduleNext(5000);
                    return;
                }

                publishEvent(serverRsp);

                recordEpoch(serverRsp);

                scheduleNext(interval);

            } catch (Exception e) {
                WATCHER_LOGGER.error("eureka watcher " + name + "error", e);
            }
        }

        private void scheduleNext(long delay) {
            if (!isRunning()) {
                return;
            }

            if (!this.timer.isShutdown()) {
                this.timer.schedule(this, delay, TimeUnit.MILLISECONDS);
            }
        }
    }
}