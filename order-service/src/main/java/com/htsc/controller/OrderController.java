package com.htsc.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
@ResponseBody
public class OrderController {

    @Autowired
    private RestTemplate restTemplate;

    @PostMapping("/send")
    public String sendFoo(@RequestBody String input) {
        Map map = JSONObject.parseObject(input, Map.class);

        map.put("key1", "hahhahaha");
        map.put("key2", "dickinson");

        return (String) map.get("key1");

//        return input;
//        return restTemplate.postForObject("http://localhost:8081/user-service/user", JSON.toJSONString(map), String.class);
    }


}
