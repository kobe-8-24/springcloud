package com.htsc.order.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@ResponseBody
public class OrderController {

    @Autowired
    private RestTemplate restTemplate;

    @GetMapping("/send/{input}")
    public String sendFoo(@PathVariable String input) {

        return input;
//        return restTemplate.getForObject("http://localhost:8081/user/" + input, String.class);
    }
}
