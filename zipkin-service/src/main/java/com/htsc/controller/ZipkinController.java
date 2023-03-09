package com.htsc.controller;

import brave.Tracer;
import com.htsc.service.ZipkinService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
@RequestMapping("/zipkin")
public class ZipkinController {

    @Autowired
    private ZipkinService zipkinService;

    @Autowired
    private Tracer tracer;

    @PostMapping("/traceId")
    public String sayHi(@RequestBody Map<String, Object> input) {
        return zipkinService.sayHello(input);
    }
}
