package com.htsc.service.impl;

import com.alibaba.fastjson.JSON;
import com.htsc.service.ZipkinService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class ZipkinServiceImpl implements ZipkinService {

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public String sayHello(Map<String, Object> input) {
        return getString(input);
    }

    private String getString(Map<String, Object> input) {
        input.put("basketball", "jordan");
        input.put("football", "messi");

        return restTemplate.postForObject("http://localhost:8080/send", JSON.toJSONString(input), String.class);
    }
}
