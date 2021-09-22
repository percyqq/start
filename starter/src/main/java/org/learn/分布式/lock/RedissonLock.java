package org.learn.分布式.lock;


import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

// docker run -itd -p6379:6379  --name redis  redis
// docker run -itd -p8083:80 --name nginx-local   nginx
public class RedissonLock {


    public static void main(String[] args) {

//        ch.qos.logback.classic.LoggerContext loggerContext = (ch.qos.logback.classic.LoggerContext) LoggerFactory.getILoggerFactory();
//        List<ch.qos.logback.classic.Logger> loggerList = loggerContext.getLoggerList();
//        loggerList.forEach(logger -> {
//            logger.setLevel(ch.qos.logback.classic.Level.INFO);
//        });

        org.redisson.config.Config config = new org.redisson.config.Config();
        config.useSingleServer().setAddress("redis://localhost:6379")
                //.setPassword("redis.shtest")
                .setDatabase(0).setConnectionMinimumIdleSize(5).setTimeout(1000);

        RedissonClient redissonClient = Redisson.create(config);

        // tryLock wait timeout
        test1(redissonClient);
    }

    private static void test1(RedissonClient redissonClient) {
        int cnt = 6, init = 11;
        final int[] wtf = {init};

        CountDownLatch latch = new CountDownLatch(cnt);
        for (int i = 1; i <= cnt; i++) {
            int finalI = i;
            new Thread(() -> {
                RLock lock = redissonClient.getLock("dbl-wtf");

                try {
                    System.out.println(new Date() + "   thread oper " + Thread.currentThread().getId() + " start, lock : " + lock);

                    boolean res = lock.tryLock(9000, TimeUnit.MILLISECONDS);

                    System.out.println(new Date() + "   thread oper " + Thread.currentThread().getId() + " get lock : " + lock + ", ==> tryLock : " + res);
                    if (res) {
                        try {
                            int time = new Random().nextInt(5) + 1;
                            System.out.println(new Date() + "   thread oper " + Thread.currentThread().getId() + ", sleep : " + time + " , aaaaaa");
                            TimeUnit.SECONDS.sleep(time);

                            wtf[0]++;

                            if (finalI % 2 == 1) {
                                boolean res1 = lock.tryLock(3, 10, TimeUnit.SECONDS);
                                System.out.println(new Date() + "   thread oper " + Thread.currentThread().getId() + " get lock again: " + lock);
                            }

                            TimeUnit.SECONDS.sleep(2);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    System.out.println(new Date() + "   thread oper " + Thread.currentThread().getId() + ", lock: " + lock + ", locked: " +
                            lock.isLocked() + ", isHeldByCurrentThread : " + lock.isHeldByCurrentThread());


                    if (lock.isHeldByCurrentThread()) {
                        try {
                            lock.unlock();
                            System.out.println(new Date() + "   thread oper " + Thread.currentThread().getId() + ", unlock !!");
                        } catch (Exception exception) {
                            System.err.println(new Date() + "   thread oper " + Thread.currentThread().getId() + " unlock trigger error");
                            exception.printStackTrace();
                        }
                    } else {
                        System.out.println(new Date() + "   thread oper " + Thread.currentThread().getId() + ", not held lock ");
                    }

                    latch.countDown();
                    System.out.println();
                }
            }).start();
        }

        try {
            latch.await();
        } catch (
                InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("wtf : init : " + init + " , end : " + wtf[0]);
        redissonClient.shutdown();
    }


}
