package com.htsc.order.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrderController {
//    @Autowired
//    private KafkaTemplate<Object, Object> template;
//
//    @GetMapping("/send/{input}")
//    public void sendFoo(@PathVariable String input) {
//        this.template.send("topic_input", input);
//    }
//
//    @KafkaListener(id = "webGroup", topics = "topic_input")
//    public void listen(String input) {
//        logger.info("input value: {}" , input);
//    }
}
