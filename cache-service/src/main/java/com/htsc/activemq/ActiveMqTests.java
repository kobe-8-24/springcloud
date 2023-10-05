package com.htsc.activemq;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ActiveMqTests {
    @Autowired
    private Producer producer;

    @Test
    public void sendSimpleQueueMessage() {
        this.producer.sendMsg("提现200.00元");
    }
}
