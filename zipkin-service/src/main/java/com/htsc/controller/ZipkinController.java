package com.htsc.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/zipkin")
public class ZipkinController {

    @GetMapping("/traceId")
    public String sayHi() {
        return "17602516075";
    }
}
