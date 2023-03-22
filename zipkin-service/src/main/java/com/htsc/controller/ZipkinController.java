package com.htsc.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.htsc.domain.GraphqlDTO;
import com.htsc.domain.VariablesDTO;
import com.htsc.service.ZipkinService;
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
    private RestTemplate restTemplate;

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
