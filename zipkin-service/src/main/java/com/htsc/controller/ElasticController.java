package com.htsc.controller;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.indices.CreateIndexResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.*;

/**
 * 模拟 前端上报es新建的索引 sw_segment_self_front-20230316, 并发送消息监听;
 */
@RestController
@RequestMapping("/elastic")
public class ElasticController {
    @Autowired
    private ElasticsearchClient elasticsearchClient;

    @PostMapping("/traceId")
    public String sayHi(@RequestBody Map<String, Object> input) throws IOException, ClassNotFoundException {
        CreateIndexResponse indexResponse = elasticsearchClient.indices().create(c -> c.index("user"));
       return "OK";
    }
}
