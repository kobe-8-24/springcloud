package com.htsc.kafka;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class KafkaDelayMessageProducer {

    private final KafkaProducer<String, String> producer;
    private final String topicName;

    public KafkaDelayMessageProducer(String topicName) {
        this.topicName = topicName;
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("key.serializer", StringSerializer.class.getName());
        props.put("value.serializer", StringSerializer.class.getName());
        this.producer = new KafkaProducer<>(props);
    }

    public void sendMessage(String message, long delayMs) {
        long timestamp = System.currentTimeMillis() + delayMs;
        ProducerRecord<String, String> record = new ProducerRecord<>(topicName, null, timestamp, null, message);
        producer.send(record);
    }

    public static void main(String[] args) throws InterruptedException {
        KafkaDelayMessageProducer producer = new KafkaDelayMessageProducer("kafka_delay_topic");

        producer.sendMessage("Hello, World!", TimeUnit.SECONDS.toMillis(30));
        System.out.println("Message sent!");

        producer.close();
    }

    public void close() {
        producer.close();
    }
}
