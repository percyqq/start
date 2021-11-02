package org.learn.message.kafka.learning.consumer;

import org.apache.kafka.common.TopicPartition;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

public class ConsumerPool {
    private PollThread pollThread;
    private AtomicLong requestId = new AtomicLong(0);
    private Map<TopicPartition, ThreadPoolExecutor> executorServices = new HashMap<>();
    private ReentrantLock threadLock = new ReentrantLock();
    private AtomicInteger threadNum = new AtomicInteger(0);
    private Integer maxQueueInMsgs = 1000;

    public ConsumerPool(PollThread pollThread) {
        this.pollThread = pollThread;
    }

    /**
     * 该方法只被 PoolThread 方法调用，线程安全
     *
     * @param consumerTask
     * @return
     */
    public void submitConsumerTask(ConsumerTask consumerTask) {
        ThreadPoolExecutor executor = get(consumerTask.topicPartition);
        if (executor.getQueue().size() > maxQueueInMsgs) {
            executor.submit(consumerTask);
            // 消息消费积压，需要暂停消息拉取
            pollThread.addNeedPause(consumerTask.topicPartition);
        }
    }

    private ThreadPoolExecutor get(TopicPartition topicPartition) {
        try {
            threadLock.lock();
            ThreadPoolExecutor executor = executorServices.get(topicPartition);
            if (executor == null) {
                executor = newThread();
                executorServices.put(topicPartition, executor);
            }
            return executor;

        } finally {
            threadLock.unlock();
        }
    }

    private ThreadPoolExecutor newThread() {
        return new ThreadPoolExecutor(1, 1, 0, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(2000), runnable -> {
                    Thread t = new Thread(runnable);
                    t.setName("ConsumerMessageThread-" + threadNum.incrementAndGet());
                    return t;
                });
    }


}
