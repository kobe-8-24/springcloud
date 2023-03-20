package com.htsc.controller;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
@RequestMapping("/skywalking")
@Slf4j
public class CacheController {
    @Autowired
    private RestTemplate restTemplate;

    /**
     * 添加自定义指标
     *
     * @return String
     */
    @PostMapping("/id")
    public String sayHi(@RequestBody Map<String, Object> map) {
//        String result = restTemplate.postForObject("http://localhost:9999/order-service/send", JSON.toJSONString(map), String.class);
//        log.info("restTemplate result:{}", result);
        return "result";
    }
}
