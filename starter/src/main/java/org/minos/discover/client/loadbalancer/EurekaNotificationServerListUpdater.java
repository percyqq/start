package org.minos.discover.client.loadbalancer;

import com.google.common.collect.Sets;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.netflix.config.DynamicIntProperty;
import com.netflix.loadbalancer.DynamicServerListLoadBalancer;
import com.netflix.loadbalancer.ServerListUpdater;
import org.minos.discover.client.listener.DiscoveryCacheEventPublisher;
import org.minos.discover.client.listener.DiscoveryCacheRefreshEvent;

import java.util.Date;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import static org.minos.discover.client.common.LoggerUtils.UPDATER_LOGGER;

/**
 * A server list updater for the {@link DynamicServerListLoadBalancer}
 * 与原有提供的{@link com.netflix.niws.loadbalancer.EurekaNotificationServerListUpdater}相比支持如下场景
 * 1. 和EurekaClient解耦，不依赖EurekaEventListener，使用独立的@{@link DiscoveryCacheEventPublisher}
 * 2. 支持多个RibbonClient ServerListUpdater注册到一个Updater上
 */
public class EurekaNotificationServerListUpdater implements ServerListUpdater {

    private static class LazyHolder {
        private final static String CORE_THREAD = "EurekaNotificationServerListUpdater.ThreadPoolSize";
        private final static String QUEUE_SIZE = "EurekaNotificationServerListUpdater.queueSize";
        private final static LazyHolder SINGLETON = new LazyHolder();

        private final DynamicIntProperty poolSizeProp = new DynamicIntProperty(CORE_THREAD, 2);
        private final DynamicIntProperty queueSizeProp = new DynamicIntProperty(QUEUE_SIZE, 1000);
        private final ThreadPoolExecutor defaultServerListUpdateExecutor;
        private final Thread shutdownThread;

        private LazyHolder() {
            int corePoolSize = getCorePoolSize();
            defaultServerListUpdateExecutor = new ThreadPoolExecutor(
                    corePoolSize,
                    corePoolSize * 5,
                    0,
                    TimeUnit.NANOSECONDS,
                    new ArrayBlockingQueue<>(queueSizeProp.get()),
                    new ThreadFactoryBuilder()
                            .setNameFormat("EurekaNotificationServerListUpdater-%d")
                            .setDaemon(true)
                            .build()
            );

            poolSizeProp.addCallback(() -> {
                int corePoolSize1 = getCorePoolSize();
                defaultServerListUpdateExecutor.setCorePoolSize(corePoolSize1);
                defaultServerListUpdateExecutor.setMaximumPoolSize(corePoolSize1 * 5);
            });

            shutdownThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    UPDATER_LOGGER.info("Shutting down the Executor for EurekaNotificationServerListUpdater");
                    try {
                        defaultServerListUpdateExecutor.shutdown();
                        Runtime.getRuntime().removeShutdownHook(shutdownThread);
                    } catch (Exception e) {
                        // this can happen in the middle of a real shutdown, and that's ok.
                    }
                }
            });

            Runtime.getRuntime().addShutdownHook(shutdownThread);
        }

        private int getCorePoolSize() {
            int propSize = poolSizeProp.get();
            if (propSize > 0) {
                return propSize;
            }
            return 2; // default
        }        
    }

    public static ExecutorService getDefaultRefreshExecutor() {
        return LazyHolder.SINGLETON.defaultServerListUpdateExecutor;
    }

    private final AtomicBoolean isActive = new AtomicBoolean(false);
    private final AtomicLong lastUpdated = new AtomicLong(System.currentTimeMillis());
    private final ExecutorService refreshExecutor;
    private final DiscoveryCacheEventPublisher cacheEventPublisher;

    private Set<UpdateAction> registeredAction;

    public EurekaNotificationServerListUpdater(DiscoveryCacheEventPublisher cacheEventPublisher) {
        this(cacheEventPublisher, getDefaultRefreshExecutor());
    }

    public EurekaNotificationServerListUpdater(DiscoveryCacheEventPublisher cacheEventPublisher, ExecutorService refreshExecutor) {
        this.cacheEventPublisher = cacheEventPublisher;
        this.refreshExecutor = refreshExecutor;
        this.registeredAction = Sets.newHashSet();
    }

    /**
     * 将UpdateAction放入Listener队列
     */
    @Override
    public synchronized void start(final UpdateAction updateAction) {
        if (updateAction == null) {
            return;
        }

        if (this.registeredAction.contains(updateAction)) {
            UPDATER_LOGGER.info("an update action is already set");
            return;
        }

        cacheEventPublisher.registerListener(event -> {
            if (event instanceof DiscoveryCacheRefreshEvent) {
                if (!refreshExecutor.isShutdown()) {
                    try {
                        refreshExecutor.submit(() -> {
                            try {
                                updateAction.doUpdate();
                                lastUpdated.set(System.currentTimeMillis());
                                registeredAction.add(updateAction);
                            } catch (Exception e) {
                                UPDATER_LOGGER.warn("Failed to update serverList", e);
                            }
                        });  // fire and forget
                    } catch (Exception e) {
                        UPDATER_LOGGER.warn("Error submitting update task to executor, skipping one round of updates", e);
                    }
                }
                else {
                    UPDATER_LOGGER.debug("stopping EurekaNotificationServerListUpdater, as refreshExecutor has been shut down");
                    stop();
                }
            }
        });
    }

    @Override
    public synchronized void stop() {
    }

    @Override
    public String getLastUpdate() {
        return new Date(lastUpdated.get()).toString();
    }

    @Override
    public long getDurationSinceLastUpdateMs() {
        return System.currentTimeMillis() - lastUpdated.get();
    }

    @Override
    public int getNumberMissedCycles() {
        return 0;
    }

    @Override
    public int getCoreThreads() {
        if (isActive.get()) {
            if (refreshExecutor != null && refreshExecutor instanceof ThreadPoolExecutor) {
                return ((ThreadPoolExecutor) refreshExecutor).getCorePoolSize();
            }
        }
        return 0;
    }
}
