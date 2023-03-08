package com.htsc.service.impl;

import com.htsc.service.ZipkinService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ZipkinServiceImpl implements ZipkinService {

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public String sayHello() {
        return getString();
    }

    private String getString() {
        return restTemplate.getForObject("http://localhost:8080/send/hello", String.class);
    }
}
