package org.learn.concurrent;

import java.util.Map;
import java.util.concurrent.*;

public class jdk缓存 {

    // https://mp.weixin.qq.com/s?__biz=Mzg3NjU3NTkwMQ==&mid=2247532958&idx=1&sn=6331b28fbe64b888eedbac7a4938a2a2

    public static final ConcurrentHashMap<String, Future<Integer>> SCORE_CACHE = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        ExecutorService service = Executors.newFixedThreadPool(5);
        jdk缓存 cacheService = new jdk缓存();

        for (int i = 0; i < 1; i++) {
            service.submit(() -> {
                Integer e = cacheService.query("135");
                System.out.println("135 : " + e);
            });
        }

        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        SCORE_CACHE.entrySet().forEach(entry -> {
            entry.getValue().cancel(true);
        });


    }

    public Integer query(String name) {
        while (true) {
            Future<Integer> future = SCORE_CACHE.get(name);
            if (future == null) {
                Callable<Integer> callable = () -> loadFromDB(name);
                FutureTask<Integer> futureTask = new FutureTask<>(callable);

                future = SCORE_CACHE.putIfAbsent(name, futureTask);
                if (future == null) {
                    future = futureTask;
                    futureTask.run();
                }
            }

            try {
                future.get();
            } catch (CancellationException e) {
                e.printStackTrace();

                //!!
                // SCORE_CACHE.remove(name, future);


            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private Integer loadFromDB(String name) {
        System.out.println(Thread.currentThread().getName() + " query " + name);

        try {
            TimeUnit.SECONDS.sleep(3);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        return ThreadLocalRandom.current().nextInt(330, 459);
    }


}
