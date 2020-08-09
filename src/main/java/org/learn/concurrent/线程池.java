package org.learn.concurrent;

import java.security.SecureRandom;
import java.util.concurrent.*;

/**
 * @create: 2020-08-05 20:15
 */
public class 线程池 {

    private static final SecureRandom secureRandom = new SecureRandom();

    public static void main(String[] args) {

        ExecutorService executorService = null;//
        Executors.newFixedThreadPool(5);
        executorService = new ThreadPoolExecutor(5, 10,
                30L, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(5));

        ExecutorService cachedThreadPool = Executors.newCachedThreadPool();
        for (int i = 0; i < 25; i++) {
            ExecutorService finalExecutorService = executorService;

            int finalI = i;
            cachedThreadPool.submit(() -> {
                //int time = getRange(20, 40);

                int time = 10 + (5 * finalI % 51);
                try {
                    TimeUnit.SECONDS.sleep(time);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("sleep " + time + ", then create thread");


                finalExecutorService.execute(() -> {
                    int timeRun = getRange(40, 60);
                    try {
                        System.out.println("thread " + Thread.currentThread().getName() + " will run " + timeRun + "s.");
                        TimeUnit.SECONDS.sleep(time);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });


            });
        }

        while (!cachedThreadPool.isTerminated()) {
            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println(" cachedThreadPool.shutdown");
        cachedThreadPool.shutdown();

        while (!executorService.isTerminated()) {
            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        executorService.shutdown();
        System.out.println(" executorService.shutdown");


    }

    private static int get5to15() {
        return getRange(5, 15);
    }

    private static int getRange(int start, int end) {
        int dif = end - start;
        return start + secureRandom.nextInt(dif);
    }
}

