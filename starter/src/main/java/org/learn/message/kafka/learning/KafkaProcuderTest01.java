package org.learn.message.kafka.learning;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class KafkaProcuderTest01 {

    public static void main(String[] args) throws Exception{

        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092,localhost:9093,localhost:9094");
        KafkaProducer<String, String> producer = new KafkaProducer<>(props, new StringSerializer(), new StringSerializer());

        for(int i =0; i < 10; i ++) {
            ProducerRecord msg = new ProducerRecord("dw_test_062501","test" + i, "hello mq world:" + i);
            Future<RecordMetadata> result = producer.send(msg);
            ;
            System.out.println(result.get(3000, TimeUnit.MILLISECONDS));
        }

        System.out.println("end");

    }
}
