package org.learn.java;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;


@Slf4j
public class 自生产自消费 extends 生产消费模板<String> {

    private static ThreadPoolTaskExecutor executor;
    private static SecureRandom secureRandom = new SecureRandom();

    static {
        int concurrents = 10;
        executor = new ThreadPoolTaskExecutor();
        executor.setQueueCapacity(concurrents);
        executor.setMaxPoolSize(concurrents);
        executor.setCorePoolSize(concurrents);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.afterPropertiesSet();

        log.info("创建任务执行者成功，并加入管理队列，容量:{}, 核心线程数:{}，最大线程数:{}", concurrents, concurrents, concurrents);
    }

    public 自生产自消费() {
        super(5, 10, executor);
    }

    public static void main(String[] args) {
        自生产自消费 我 = new 自生产自消费();
        new Thread(() -> 我.start()).start();

    }

    @Override
    protected String getTaskName() {
        return "计划执行";
    }

    @Override
    protected String produced() {
        int time = secureRandom.nextInt(1111);
        SimpleDateFormat sdf = new SimpleDateFormat("yy-MM-dd HH:mm:ss:SSS");
        String wtf = sdf.format(new Date()) + " <--> " + time;
        return wtf;
    }

    private static final ReentrantLock lock = new ReentrantLock();

    @Override
    protected void consume(String plan) {
        // TODO 如果job是部署的集群需要分布式锁，不能出现同一个任务在不同的节点上计算
        boolean locked = false;
        try {
            locked = lock.tryLock(35, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        try {
            int time = secureRandom.nextInt(30);

            log.info("{} --> executeAndSummarize, sleep {}", Thread.currentThread().getName(), time);
            try {
                TimeUnit.SECONDS.sleep(time);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            log.info("{} --> executeAndSummarize ret ", Thread.currentThread().getName());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            boolean isLocked = lock.isLocked();
            boolean isHeldByCurrentThread = lock.isHeldByCurrentThread();
            log.info("{} --> executeAndSummarize finally, isLocked : {}, isHeldByCurrentThread() : {} , locked : {}, lock : {} ",
                    Thread.currentThread().getName(), isLocked, isHeldByCurrentThread, locked, lock);
            if (isHeldByCurrentThread || locked)
                lock.unlock();
        }
    }

}
