package org.minos.core.concurrent;

import com.alibaba.ttl.threadpool.TtlExecutors;

import java.util.concurrent.*;

/**
 * Java Executors封装，提供异步线程中上线文切换能力
 */
public class ThreadLocalWrappedExecutors {
    public static ExecutorService newFixedThreadPool(int nThreads) {
        return TtlExecutors.getTtlExecutorService(Executors.newFixedThreadPool(nThreads));
    }

    public static ExecutorService newFixedThreadPool(int nThreads, ThreadFactory threadFactory) {
        return TtlExecutors.getTtlExecutorService(Executors.newFixedThreadPool(nThreads, threadFactory));
    }

    public static ExecutorService newSingleThreadExecutor() {
        return TtlExecutors.getTtlExecutorService(Executors.newSingleThreadExecutor());
    }

    public static ExecutorService newSingleThreadExecutor(ThreadFactory threadFactory) {
        return TtlExecutors.getTtlExecutorService(Executors.newSingleThreadExecutor(threadFactory));
    }

    public static ExecutorService newCachedThreadPool() {
        return TtlExecutors.getTtlExecutorService(Executors.newCachedThreadPool());
    }

    public static ExecutorService newCachedThreadPool(ThreadFactory threadFactory) {
        return TtlExecutors.getTtlExecutorService(Executors.newCachedThreadPool(threadFactory));
    }


    public static ScheduledExecutorService newSingleThreadScheduledExecutor() {
        return TtlExecutors.getTtlScheduledExecutorService(Executors.newSingleThreadScheduledExecutor());
    }

    public static ScheduledExecutorService newSingleThreadScheduledExecutor(ThreadFactory threadFactory) {
        return TtlExecutors.getTtlScheduledExecutorService(Executors.newSingleThreadScheduledExecutor(threadFactory));
    }

    public static ScheduledExecutorService newScheduledThreadPool(int corePoolSize) {
        return TtlExecutors.getTtlScheduledExecutorService(Executors.newScheduledThreadPool(corePoolSize));
    }

    public static ScheduledExecutorService newScheduledThreadPool(
            int corePoolSize, ThreadFactory threadFactory) {
        return TtlExecutors.getTtlScheduledExecutorService(Executors.newScheduledThreadPool(corePoolSize, threadFactory));
    }

    public static ExecutorService newExecutorService(
            int corePoolSize,
            int maximumPoolSize,
            long keepAliveTime,
            TimeUnit unit,
            BlockingQueue<Runnable> workQueue,
            ThreadFactory threadFactory,
            RejectedExecutionHandler handler
    ) {
        ExecutorService executorService = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
        return TtlExecutors.getTtlExecutorService(executorService);
    }

    public static ExecutorService wrap(ExecutorService executorService) {
        return TtlExecutors.getTtlExecutorService(executorService);
    }
}