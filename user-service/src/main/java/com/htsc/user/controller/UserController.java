package com.htsc.user.controller;

import com.alibaba.fastjson.JSONObject;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class UserController {

    @PostMapping("/user")
    public String sendUserInfo(@RequestBody String userMap) {
        Map map = JSONObject.parseObject(userMap, Map.class);

//        throw new RuntimeException("dddd");
         return "hello i am " + map.get("key1");
    }
}
