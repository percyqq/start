package org.learn.concurrent;

import org.learn.algorithm.easy.数组s;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.security.SecureRandom;
import java.sql.Time;
import java.util.concurrent.*;

/**
 * @create: 2020-08-05 20:15
 */
public class 线程池 {

    private static final SecureRandom secureRandom = new SecureRandom();

    //  https://www.cnblogs.com/qingquanzi/p/9018627.html
    //  线程池的关闭。
    public static void main(String[] args) {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(2, 5, 60,
                TimeUnit.SECONDS, new LinkedBlockingQueue<>(10));
//        Future<String> future = executor.submit(() -> {
//            System.out.println("135246");
//            //FutureTask 里的 public void run() 方法
//            return "这次一定！" + 1 / 0;
//        });
//        System.out.println("future的内容:" + future.get());
//
//        Future<?> future1 = executor.submit(() -> {
//            System.out.println("=======");
//        });
//        System.out.println("future的内容:" + future1.get());


        ThreadPoolTaskExecutor executorService = new ThreadPoolTaskExecutor();
        executorService.setThreadNamePrefix("(db-pq-wm)-");
        executorService.setCorePoolSize(5);
        executorService.setMaxPoolSize(10);
        executorService.setQueueCapacity(1000);
        executorService.setKeepAliveSeconds(30);
        executorService.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executorService.initialize();


        executor = new ThreadPoolExecutor(5, 10, 60,
                TimeUnit.SECONDS, new LinkedBlockingQueue<>(10));

        //  https://mp.weixin.qq.com/s?__biz=MzIxNTQ4MzE1NA==&mid=2247483741&idx=1&sn=238fc933c3b9b19ab1754b23283ac6fd&scene=21#wechat_redirect
        //  目前来说，需要多于5个的线程来看为什么，
        //  ThreadPoolExecutor ==> final void runWorker(Worker w) {
        //      ...
        //      由于
        //      finally {
        //          processWorkerExit(w, completedAbruptly);
        //      }
        //  }
        for (int i = 1; i <= 8; i++) {
            int finalI = i;
            executor.execute(() -> {
                int time = secureRandom.nextInt(3);
                String name = Thread.currentThread().getName() + ", sleep ==> " + time;
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (finalI == 3) {
                    System.out.println(name + "wtf: " + 1 / 0);
                } else {
                    System.out.println(name + "135");
                }
            });
        }

//        try {
//            Thread.currentThread().join();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

        System.out.println("executor.shutdown()");
        executor.shutdown();
        try {
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                // 超时的时候向线程池中所有的线程发出中断(interrupted)。
                executor.shutdownNow();
                System.out.println("executor shut down fail!");
            } else {
                System.out.println("executor shut down success!");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.out.println("executor shut down fail by InterruptedException!");
            executor.shutdownNow();
        }
    }

    public static void main1(String[] args) {


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

