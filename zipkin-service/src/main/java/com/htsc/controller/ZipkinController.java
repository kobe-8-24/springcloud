package com.htsc.controller;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.htsc.domain.GraphqlDTO;
import com.htsc.domain.VariablesDTO;
import com.htsc.service.ZipkinService;
import org.apache.skywalking.apm.network.language.agent.v3.SegmentObject;
import org.apache.skywalking.apm.network.language.agent.v3.SpanObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

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
    private ElasticsearchClient elasticsearchClient;

    @Autowired
    private RestTemplate restTemplate;

    @PostMapping("/traceId")
    public String sayHi(@RequestBody Map<String, Object> input) throws IOException, ClassNotFoundException {
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

    @PostMapping("/sw_oap")
    public String sayHi111(@RequestBody Map<String, Object> input) throws IOException, ClassNotFoundException {
        List<String> backEndMetricTrace = new ArrayList<>();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        HttpEntity<String> entity = new HttpEntity<>(JSON.toJSONString(getGraphqlDTO((String) input.get("traceId"))), headers);

        ResponseEntity<JSONObject> postForObject = restTemplate
                .postForEntity("http://127.0.0.1:8080/graphql", entity, JSONObject.class);

        backEndMetricTrace.add(Objects.requireNonNull(postForObject.getBody()).toString());

        return JSON.toJSONString(backEndMetricTrace);
    }

    private GraphqlDTO getGraphqlDTO(String traceId) {
        VariablesDTO variablesDTO = new VariablesDTO();
        variablesDTO.setTraceId(traceId);
        GraphqlDTO graphqlDTO = new GraphqlDTO();
        graphqlDTO.setVariables(variablesDTO);
        graphqlDTO.setQuery("query queryTrace($traceId: ID!) {\n  trace: queryTrace(traceId: $traceId)" +
                " {\n    spans {\n      traceId\n      segmentId\n      spanId\n      parentSpanId\n      refs " +
                "{\n        traceId\n        parentSegmentId\n        parentSpanId\n        type\n      }\n      " +
                "serviceCode\n      serviceInstanceName\n      startTime\n      endTime\n      endpointName\n      " +
                "type\n      peer\n      component\n      isError\n      layer\n      tags {\n        key\n        " +
                "value\n      }\n      logs {\n        time\n        data {\n          key\n          " +
                "value\n        }\n      }\n      attachedEvents {\n        " +
                "startTime {\n          seconds\n          " +
                "nanos\n        }\n        event\n        endTime {\n          seconds\n          nanos\n        " +
                "}\n        tags {\n          key\n          value\n        }\n        summary {\n          " +
                "key\n          value\n        }\n      }\n    }\n  }\n  }");
        return graphqlDTO;
    }
}
