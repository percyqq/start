package org.learn.java;

import lombok.extern.slf4j.Slf4j;
import org.learn.utils.CollectionUtils;
import org.springframework.core.task.AsyncTaskExecutor;

import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public abstract class 生产消费模板<D> {

    private final LinkedBlockingQueue<D> queue;

    /**
     * 消费线程并发数
     */
    private final int concurrent;

    private final AsyncTaskExecutor taskExecutor;

    private volatile boolean runing = true;

    private AtomicLong count = new AtomicLong(0);

    private CountDownLatch countDownLatch;

    public 生产消费模板(int concurrent, int capacity, AsyncTaskExecutor taskExecutor) {
        this.concurrent = concurrent;
        this.taskExecutor = taskExecutor;
        this.queue = new LinkedBlockingQueue<>(capacity);
    }
    HashMap m;

    protected abstract D produced();

    protected abstract void consume(D item);

    protected abstract String getTaskName();



    /**
     * 启动任务
     */
    public void start() {

        log.debug("启动[{}]并发处理任务...", getTaskName());

        log.debug("初始化消费线程，共计：{}个", concurrent);
        countDownLatch = new CountDownLatch(concurrent);
        for (int i = 0; i < concurrent; i++) {
            taskExecutor.submit(new CustomerTask());
        }

        long startTime = System.currentTimeMillis();
        long producedDoneTime = 0;
        AtomicLong total = new AtomicLong(0);
        AtomicLong count = new AtomicLong(0);
        try {
            producedDoneTime = doProduced(total, count);
            log.debug("等待消费任务完成...");
            countDownLatch.await();
        } catch (Exception e) {
            log.error("生产[" + getTaskName() + "]数据出错", e);
        }
        log.info("并发处理任务[{}]执行完成，生产任务总耗时:{}ms，{}个消费线程总耗时:{}ms, 共生产{}批数据，累计{}条", getTaskName(),
                producedDoneTime, concurrent, System.currentTimeMillis() - startTime, count.longValue(), total.longValue());
    }

    private long doProduced(AtomicLong total, AtomicLong count) {

        log.debug("------------------------------启动生产线程...------------------------------");
        long startTime = System.currentTimeMillis();
        try {
            log.debug("开始生产[{}]数据...", getTaskName());
            do {
                D item = produced();
                if (item == null) {
                    log.debug("没有可生产的[{}]数据，生产任务结束", getTaskName());
                    runing = false;
                    break;
                }
                int size = CollectionUtils.size(item);
                log.info("生产第{}批[{}]数据，一共{}条，添加到待处理队列...", count.incrementAndGet(), getTaskName(), size);

                queue.put(item);

                log.info("添加到队列成功");
                total.addAndGet(size);
            } while (runing);
        } catch (Exception e) {
            log.error("生产[" + getTaskName() + "]数据出错", e);
        }
        log.debug("------------------------------生产线程执行完成------------------------------");
        return System.currentTimeMillis() - startTime;
    }

    public class CustomerTask implements Runnable {

        @Override
        public void run() {
            log.debug("------------------------------启动消费线程...------------------------------");

            D item = null;
            while (!queue.isEmpty() || runing) {
                log.debug("......");
                if (queue.isEmpty()) {
                    log.debug("当前队列为空，等待生产[{}]数据(3s)...", getTaskName());
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    continue;
                }
                try {
                    log.debug("从队列中拉取[{}]数据...", getTaskName());
                    item = queue.poll();
                    if (item == null) {
                        continue;
                    }
                    log.debug("拉取[{}]数据成功，执行处理...", getTaskName());
                    consume(item);
                    log.debug("[{}]数据处理成功", getTaskName());
                    Thread.sleep(1000);
                } catch (Exception e) {
                    log.error("[" + getTaskName() + "]数据消费错误", e);
                }
                log.debug("[{}]数据消费成功.({})", getTaskName(), count.incrementAndGet());
            }
            countDownLatch.countDown();
            log.debug("------------------------------消费线程执行完成------------------------------");
        }
    }



}
