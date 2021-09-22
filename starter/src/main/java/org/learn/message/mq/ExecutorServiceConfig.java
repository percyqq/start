package org.learn.message.mq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @date: 2019/12/05
 * @description:
 */
@Configuration
@Slf4j
public class ExecutorServiceConfig {

    // MQ线程池核心大小,线程池最大大小
    @Value("${max.coreThreadPooL.size:50}")
    private int maxCoreThreadPooLSize;

    @Value("${kafka.queue.reader.size:12}")
    private Integer readerSize;

    @Bean("serviceExecutorService")
    public ExecutorService serviceExecutorService() {
        return new ThreadPoolExecutor(maxCoreThreadPooLSize, maxCoreThreadPooLSize, 60, TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(10000),
                new ScmThreadFactory("dish-sync-service"),
                new ThreadPoolExecutor.CallerRunsPolicy());
    }

    @Bean("mqExecutorService")
    public ExecutorService mqExecutorService() {
        return new ThreadPoolExecutor(readerSize, readerSize, 60, TimeUnit.SECONDS,
                new SynchronousQueue<>(true),
                new ScmThreadFactory("dish-sync-mq"),
                new ThreadPoolExecutor.CallerRunsPolicy());
    }

    public static class ScmThreadFactory implements ThreadFactory {
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        public ScmThreadFactory(String threadName) {
            namePrefix = "pool-" + threadName + "-thread-";
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, namePrefix + threadNumber.getAndIncrement());
            if (t.isDaemon()) {
                t.setDaemon(false);
            }
            if (t.getPriority() != Thread.NORM_PRIORITY) {
                t.setPriority(Thread.NORM_PRIORITY);
            }

            t.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                @Override
                public void uncaughtException(Thread t, Throwable e) {
                    log.error(e.getMessage(), e);
                }
            });
            return t;
        }
    }
}
