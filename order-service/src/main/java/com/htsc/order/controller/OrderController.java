package com.htsc.order.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@ResponseBody
public class OrderController {

    @Autowired
    private RestTemplate restTemplate;

//    @GetMapping("/send/{input}")
//    public String sendFoo(@PathVariable String input) {
//
//        return input;
////        return restTemplate.getForObject("http://localhost:8081/user/" + input, String.class);
//    }

    @GetMapping("/send")
    public String sendFoo(@RequestParam("input") String input) {

        return input;
//        return restTemplate.getForObject("http://localhost:8081/user/" + input, String.class);
    }


}
