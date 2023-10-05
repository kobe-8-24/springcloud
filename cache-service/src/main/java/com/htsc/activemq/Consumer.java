package com.htsc.activemq;

import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
public class Consumer {
    @JmsListener(destination = "sms.queue")
    public void receiveMsg(String text) {
        System.out.println("接收到消息：" + text);
    }
}
