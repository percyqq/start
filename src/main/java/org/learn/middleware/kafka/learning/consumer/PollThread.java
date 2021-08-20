package org.learn.middleware.kafka.learning.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class PollThread implements Runnable {

    private KafkaConsumer<String, String> consumer;

    private ConsumerPool consumerPool;

    private ReentrantLock offSetsLock = new ReentrantLock();
    private ReentrantLock needPauseLock = new ReentrantLock();
    private ReentrantLock needResumeLock = new ReentrantLock();

    private Map<TopicPartition, OffsetAndMetadata> offsets = new HashMap<>();
    private Set<TopicPartition> needPauseSet = new HashSet<>();
    private Set<TopicPartition> needResumeSet = new HashSet<>();

    /**
     * 本文主要是演示，故这里为了方便，就在这里创建
     *
     * poll 线程
     * 该线程内部会持有一个KafkaConsumer对象，会循环调用KafkaConsumer的poll方法，尝试获取消息，然后提交到消费线程池中并发消费。
     */
    public PollThread() {
        Properties props = new Properties();
        props.setProperty("bootstrap.servers", "localhost:9092");
        props.setProperty("group.id", "dw_test_kafka_consumer_01");
        props.setProperty("enable.auto.commit", "false");
        props.setProperty("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.setProperty("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Arrays.asList("dw_test_062501"));
        this.consumerPool = new ConsumerPool(this);
    }

    @Override
    public void run() {
        while (true) {
            commitOffsets();
            needResume();
            needPause();
            // 拉取消息
            ConsumerRecords<String, String> consumerRecords = consumer.poll(Duration.ofMillis(100));
            Set<TopicPartition> topicPartitions = consumerRecords.partitions();

            for (Iterator<TopicPartition> it = topicPartitions.iterator(); it.hasNext(); ) {
                TopicPartition topicPartition = it.next();
                List<ConsumerRecord<String, String>> records = consumerRecords.records(topicPartition);
                // 按分区提交任务
                if (records != null && records.isEmpty()) {
                    consumerPool.submitConsumerTask(new ConsumerTask(this, topicPartition, records));
                }
            }
        }
    }

    private void commitOffsets() {
        try {
            offSetsLock.lock();
            consumer.commitSync(offsets);
            offsets.clear();
        } finally {
            offSetsLock.unlock();
        }
    }

    private void needResume() {
        try {
            needResumeLock.lock();
            consumer.resume(needResumeSet);
            needResumeSet.clear();
        } finally {
            needResumeLock.unlock();
        }
    }

    private void needPause() {
        try {
            needPauseLock.lock();
            consumer.pause(needPauseSet);
            needPauseSet.clear();
        } finally {
            needPauseLock.unlock();
        }
    }

    public void addOffset(TopicPartition topicPartition, OffsetAndMetadata offset) {
        try {
            offSetsLock.lock();
            offsets.put(topicPartition, offset);
        } finally {
            offSetsLock.unlock();
        }
    }

    public void addNeedPause(TopicPartition topicPartition) {
        try {
            needPauseLock.lock();
            needPauseSet.add(topicPartition);
        } finally {
            needPauseLock.unlock();
        }
    }

    public void addNeedResume(TopicPartition topicPartition) {
        try {
            needResumeLock.lock();
            needResumeSet.add(topicPartition);
        } finally {
            needResumeLock.unlock();
        }
    }
}
