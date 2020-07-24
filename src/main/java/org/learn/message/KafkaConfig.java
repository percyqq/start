package org.learn.message;


import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.AcknowledgingMessageListener;
import org.springframework.kafka.listener.ContainerProperties;

import java.util.HashMap;
import java.util.Map;

public class KafkaConfig {

    static {
        Map<String, Object> propsMap = new HashMap<>();

        // boker地址
        propsMap.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "bokerList");

        // 消费消息自动提交
        propsMap.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

        // 自动提交为false的时候可以不设置，去掉
        //propsMap.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");
        // 1000ms  自动提交间隔 1000ms一次  默认 5000ms 5秒

        // session 60秒超时 	默认 30000ms 30秒
        propsMap.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "60000");

        // 心跳间隔时间 默认 3000ms 心跳时间不大于session超时时间的1/3
        propsMap.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, "8000");

        // 请求超时时间  REQUEST_TIMEOUT_MS_CONFIG 需大于 FETCH_MAX_WAIT_MS_CONFIG
        propsMap.put(ConsumerConfig.REQUEST_TIMEOUT_MS_CONFIG, "61000");

        propsMap.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        propsMap.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        //消息分组
        String group = "", groupId = "";
        propsMap.put(ConsumerConfig.GROUP_ID_CONFIG, StringUtils.isNotEmpty(group) ? group : groupId);

        // 批量poll消息模式的时候每次最多poll10条消息 默认 Integer.MAX_VALUE
        propsMap.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "10");

        // 默认 latest  [latest, earliest, none]
        propsMap.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");

        //如果没有足够的数据能够满足fetch.min.bytes，则此项配置是指在应答fetch请求之前，server会阻塞的最大时间。
        // propsMap.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG,  "1000"); //默认 500 ms
        //每次fetch请求时，server应该返回的最小字节数。如果没有足够的数据返回，请求会等待，直到足够的数据才会返回。
        // propsMap.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG,  "1024"); //默认 1 byte


        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        DefaultKafkaConsumerFactory consumerFactory = new DefaultKafkaConsumerFactory(propsMap);

        factory.setConcurrency(5); //并发数
        //factory.setBatchListener(true);// 批量poll消息
        factory.getContainerProperties().setPollTimeout(5000); //poll超时时间3秒
        factory.setConsumerFactory(consumerFactory);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.TIME);  //为新增加
        factory.getContainerProperties().setAckTime(1000);   //为新增加


    }

    public enum AckMode {

        /**
         * Commit after each record is processed by the listener.
         */
        RECORD,

        /**
         * Commit whatever has already been processed before the next poll.
         */
        BATCH,

        /**
         * Commit pending updates after
         * {@link ContainerProperties#setAckTime(long) ackTime} has elapsed.
         */
        TIME,

        /**
         * Commit pending updates after
         * {@link ContainerProperties#setAckCount(int) ackCount} has been
         * exceeded.
         */
        COUNT,

        /**
         * Commit pending updates after
         * {@link ContainerProperties#setAckCount(int) ackCount} has been
         * exceeded or after {@link ContainerProperties#setAckTime(long)
         * ackTime} has elapsed.
         */
        COUNT_TIME,

        /**
         * User takes responsibility for acks using an
         * {@link AcknowledgingMessageListener}.
         */
        MANUAL,

        /**
         * User takes responsibility for acks using an
         * {@link AcknowledgingMessageListener}. The consumer is woken to
         * immediately process the commit.
         */
        MANUAL_IMMEDIATE,

    }


}