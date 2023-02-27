package com.htsc.user.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    @GetMapping("/user/{input}")
    public String sendUserInfo(@PathVariable String input) {
        return "hello i am " + input;
    }
}
