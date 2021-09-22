package org.learn.middleware.kafka.learning.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;

import java.util.List;

/**
 * 消费端任务
 */
public class ConsumerTask implements Runnable {
    TopicPartition topicPartition;
    List<ConsumerRecord<String, String>> records;
    PollThread pollThread;

    public ConsumerTask(PollThread pollThread, TopicPartition topicPartition, List<ConsumerRecord<String, String>> records) {
        this.pollThread = pollThread;
        this.topicPartition = topicPartition;
        this.records = records;
    }

    @Override
    public void run() {
        if (records == null || records.isEmpty()) {
            // 输出告警并返回
            return;
        }

        // 这里无需加锁，因为同一个分区的数据会发送到同一个线程
        for (ConsumerRecord record : records) {
            doConsumer(record);
        }

        // 需要恢复拉取
        pollThread.addNeedResume(topicPartition);
        // 提交位点
        pollThread.addOffset(topicPartition, new OffsetAndMetadata(records.get(records.size() - 1).offset()));

    }

    public int routeKey() {
        return Math.abs(topicPartition.hashCode());
    }


    // 具体调用业务注册的事件处理器，这里是demo，故不详细展开
    private void doConsumer(ConsumerRecord record) {

    }
}
