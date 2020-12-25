package org.learn.utils;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @create: 2020-12-25 17:30
 */
@Slf4j
public class ConcurrentUtil {

    public static void main(String[] args) {

        ExecutorService executorService = new ThreadPoolExecutor(20, 20, 60, TimeUnit.SECONDS,
                new SynchronousQueue<>(true),
                new ScmThreadFactory("dish-sync-mq"),
                new ThreadPoolExecutor.CallerRunsPolicy());
        int count = 135;
        //todo count = 1

        try {
            for (int i = 0; i < count; i++) {
                int finalI = i;
                executorService.execute(new LeakThread(finalI));
            }
        } catch (RejectedExecutionException ree) {
            ree.printStackTrace();
        }
    }

    private static class LeakThread implements Runnable {
        private int index;

        private LeakThread(int index) {
            this.index = index;
        }

        @Override
        public void run() {
            String ret = HttpUtil.getJson("http://127.0.0.1:3603/start/dogs?id=", null);
            System.out.println("========> " + ret);
        }
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
            t.setUncaughtExceptionHandler((thread, throwable) -> log.error(throwable.getMessage(), throwable));
            return t;
        }
    }
}
