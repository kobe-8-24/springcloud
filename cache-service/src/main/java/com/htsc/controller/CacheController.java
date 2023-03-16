package com.htsc.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/skywalking")
public class CacheController {

    /**
     * 添加自定义指标
     *
     * @return String
     */
    @GetMapping("/id")
    public String sayHi() {
        return "17602516075";
    }
}
