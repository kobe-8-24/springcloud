package com.htsc.controller;

import com.htsc.service.ZipkinService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/zipkin")
public class ZipkinController {

    @Autowired
    private ZipkinService zipkinService;

    @GetMapping("/traceId")
    public String sayHi() {
        return zipkinService.sayHello();
    }
}
