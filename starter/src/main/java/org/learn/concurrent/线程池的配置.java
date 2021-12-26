package org.learn.concurrent;

import java.util.concurrent.*;
import java.util.stream.IntStream;

public class 线程池的配置 {

     public static void main(String[] args) {

         //压测时 QPS 2000，平均响应时间为 20ms，正常情况下，40 个线程就可以平衡生产速度，不会堆积。但在 BlockingQueue Size 为 50 时，即使线程池 coreSize 为 1000，还会出现请求被线程池拒绝的情况。
         ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(1000, 2000, 0, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(50), new ThreadPoolExecutor.AbortPolicy());

        CountDownLatch countDownLatch = new CountDownLatch(1);
        IntStream.rangeClosed(1, 2000).forEach(i -> {
            threadPoolExecutor.execute(() -> {
                try {
                    countDownLatch.await();
                    Thread.sleep(20);
                    System.out.println(Thread.currentThread().getName() + " endd");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        });

        new Thread(() -> {
            try {
                Thread.sleep(3333);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(" countDown ...");
            countDownLatch.countDown();
        }).start();
    }

    private static void test() {
        final 线程池的配置 threadPoolTest = new 线程池的配置();

        for (int i = 0; i < 8; i++) {
            new Thread(() -> {

                while (true) {
                    //关键点，通过Executors.newSingleThreadExecutor创建一个单线程的线程池
                    ExecutorService executorService = Executors.newSingleThreadExecutor();
                    FutureTask<String> futureTask = new FutureTask(() -> {
                        Thread.sleep(50);
                        return System.currentTimeMillis() + "";
                    });
                    executorService.execute(futureTask);

                    try {
                        String s = futureTask.get();
                        //System.out.println( Thread.currentThread().getName() + " ==> " + s);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (Error e) {
                        e.printStackTrace();
                    }
                }

            }).start();
        } //子线程不停gc，模拟偶发的gc

        new Thread(() -> {
            while (true) {
                System.gc();
            }
        }).start();

    }

}
