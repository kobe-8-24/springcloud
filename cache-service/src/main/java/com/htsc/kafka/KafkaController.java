package com.htsc.kafka;

import com.htsc.kafka.KafkaDelayMessageProducer;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/skywalking")
@Slf4j
public class KafkaController {
    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private KafkaTemplate kafkaTemplate;

    /**
     * 添加自定义指标
     *
     * @return String
     */
    @PostMapping("/id")
    public String sayHi(@RequestBody Map<String, Object> map) {
//        String result = restTemplate.postForObject("http://localhost:9999/order-service/send", JSON.toJSONString(map), String.class);
//        log.info("restTemplate result:{}", result);

//        kafkaTemplate.send("skywalking_delay_message_topic", "test delay msg!!");


        // 配置 Kafka Producer
//        Properties props = new Properties();
//        props.put("bootstrap.servers", "localhost:9092");
//        props.put("key.serializer", StringSerializer.class.getName());
//        props.put("value.serializer", StringSerializer.class.getName());
//        KafkaProducer<String, String> producer = new KafkaProducer<>(props);
//
//        // 创建延迟消息
//        String message = "Delayed Message111111!";
//        int delayTime = 60000; // 60 秒钟的延迟时间
//        long timestamp = System.currentTimeMillis() + delayTime;
//
//        // 发送消息
//        ProducerRecord<String, String> record = new ProducerRecord<>("kafka_delay_topic", null, timestamp, null, message);
//        producer.send(record);

        KafkaDelayMessageProducer producer = new KafkaDelayMessageProducer("kafka_delay_topic");

        producer.sendMessage("Hello, World!", TimeUnit.SECONDS.toMillis(30));
        System.out.println("Message sent!");

        producer.close();

        return "result";
    }


//    @KafkaListener(topics = "kafka_delay_topic", groupId = "mentugroup")
    public void consumeMsg(ConsumerRecord<String,String> record) {
        //获取消息
        String message = record.value();
        //消息偏移量
        long offset = record.offset();
        System.out.println("读取的消息："+message+"\n当前偏移量："+offset);
    }
}
