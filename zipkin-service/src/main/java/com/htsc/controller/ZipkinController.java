package com.htsc.controller;

import brave.Tracer;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.alibaba.fastjson.JSON;
import com.htsc.service.ZipkinService;
import org.apache.skywalking.apm.network.language.agent.v3.SegmentObject;
import org.apache.skywalking.apm.network.language.agent.v3.SpanObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.io.*;
import java.util.*;

/**
 * 模拟 前端上报es新建的索引 sw_segment_self_front-20230316, 并发送消息监听;
 */
@RestController
@RequestMapping("/zipkin")
public class ZipkinController {

    @Autowired
    private ZipkinService zipkinService;

    @Autowired
    private Tracer tracer;

    @Autowired
    private ElasticsearchClient elasticsearchClient;

    @PostMapping("/traceId")
    public String sayHi(@RequestBody Map<String, Object> input) throws IOException, ClassNotFoundException {
        // 根据pageId、userId获取traceId;









        // 执行查询
        SearchResponse<Map> searchResponse = elasticsearchClient.search(srBuilder -> srBuilder
                        .index((String) input.get("indexName"))
                        // terms查询：对输入内容不做分词处理。
                        .query(queryBuilder -> queryBuilder
                                .term(termQueryBuilder -> termQueryBuilder
                                        .field("trace_id")
                                        .value("ea5ff046178e42da92778e5c6274236e.62.16788438736530001"))
                        )
                , Map.class);

        //解析查询结果
        System.out.println(searchResponse);

        Map<String, String> source = searchResponse.hits().hits().get(0).source();

        String data_binary = source.get("data_binary");

        byte[] decode = Base64.getDecoder().decode(data_binary);

        org.apache.skywalking.apm.network.language.agent.v3.SegmentObject segmentObject = SegmentObject.parseFrom(decode);

        String traceId = segmentObject.getTraceId();
        String service = segmentObject.getService();
        String serviceInstance = segmentObject.getServiceInstance();
        List<SpanObject> spansList = segmentObject.getSpansList();

        Map<String, Object> map = new HashMap<>();

        map.put("traceId", traceId);
        map.put("service", service);
        map.put("serviceInstance", serviceInstance);
        map.put("spansInfoList", extracted(spansList));

        return JSON.toJSONString(map);
    }

    private List<Map<String, Object>> extracted(List<SpanObject> spansList) {
        List<Map<String, Object>> list = new ArrayList<>();

        spansList.forEach(s -> {
            Map<String, Object> spanMap = new HashMap<>();
            spanMap.put("spanId", s.getSpanId());
            spanMap.put("spanName", s.getSpanType().name());
            spanMap.put("tags", getTags(s));
            list.add(spanMap);
        });

        return list;
    }

    private List<Map<String, Object>> getTags(SpanObject s) {
        List<Map<String, Object>> list = new ArrayList<>();

        s.getTagsList().forEach(a -> {
            Map<String, Object> map = new HashMap<>();
            map.put("key", a.getKey());
            map.put("value", a.getValue());
            list.add(map);
        });

        return list;
    }

    @PostConstruct
    public void createIndex() {

    }
}
