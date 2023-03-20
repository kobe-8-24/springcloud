package com.htsc.controller;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.htsc.domain.FrontMetricDTO;
import com.htsc.domain.FrontMqApplicationEvent;
import com.htsc.util.GenerateIndexUtil;
import com.htsc.constant.IndexPrefix;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 获取前端指标
 * 上报es索引 sw_segment_self_front-20230316
 * 并发送消息监听
 */
@RestController
@RequestMapping("/skywalking")
@Slf4j
public class SkywalkingFrontController {
    @Autowired
    private ElasticsearchClient elasticsearchClient;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    private RestTemplate restTemplate;

    @PostMapping("/traceId")
    public String reportFrontMetricsToEsAndMq(@RequestBody Map<String, Object> map) throws IOException {
        // 获取前端指标数据
        String pageId = (String) map.get("pageId");
        String userId = (String) map.get("userId");
        Map<String, Object> frontReportMetric = (Map<String, Object>) map.get("frontReportMetric");

        // 前端上報指標的es索引
        BulkResponse response = getBulkResponse(pageId, userId, frontReportMetric);
        log.info("BulkResponse errors:{}, took:{}, items:{}.", response.errors(), response.took(), response.items());

        // 調用後台接口, 請求頭 添加 pageId、userId
//        ResponseEntity<JSONObject> postForObject = getJsonObjectResponseEntity(pageId, userId);
//        log.info("restTemplate result:{}", postForObject.getBody());

        // pub异步消息
        applicationEventPublisher.publishEvent(new FrontMqApplicationEvent(this, pageId, userId, JSON.toJSONString(frontReportMetric)));

        return JSON.toJSONString(response.items());
    }

    private ResponseEntity<JSONObject> getJsonObjectResponseEntity(String pageId, String userId) {
        // 模拟页面上发起两次请求

        // 第一次 cache-service order-service user-service
//        restTemplate.postForEntity("http://localhost:8083/cache-service/skywalking/id",
//                new HttpEntity<>(getStringObjectMap(), getHttpHeaders(pageId, userId)), JSONObject.class);

        // 第二次 order-service user-service
        return restTemplate.postForEntity("http://localhost:9999/order-service/send",
                new HttpEntity<>(getStringObjectMap(), getHttpHeaders(pageId, userId)), JSONObject.class);
    }

    private Map<String, Object> getStringObjectMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("skywalking_test", "it's OK!!!");
        return map;
    }

    private HttpHeaders getHttpHeaders(String pageId, String userId) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        headers.add("pageId", pageId);
        headers.add("userId", userId);
        return headers;
    }

    private BulkResponse getBulkResponse(String pageId, String userId, Map<String, Object> frontReportMetric) throws IOException {
        String reportFrontIndex = GenerateIndexUtil.generateIndexNameByDay(IndexPrefix.SW_SEGMENT_SELF_FRONT);

        return elasticsearchClient.bulk(e->e.index(reportFrontIndex)
                .operations(getBulkOperations(pageId, userId, frontReportMetric, reportFrontIndex)));
    }

    private List<BulkOperation> getBulkOperations(String pageId, String userId, Map<String, Object> frontReportMetric, String reportFrontIndex) {
        // 上报ES
        // 构建一个批量操作BulkOperation的集合
        List<BulkOperation> bulkOperations = new ArrayList<>();
        bulkOperations.add(new BulkOperation.Builder()
                .create(d-> d.document(getMetricDTO(pageId, userId, JSON.toJSONString(frontReportMetric)))
                        .index(reportFrontIndex)).build());
        return bulkOperations;
    }

    private FrontMetricDTO getMetricDTO(String pageId, String userId, String frontReportMetric) {
        return FrontMetricDTO
                .builder()
                .pageId(pageId)
                .userId(userId)
                .frontReportMetric(frontReportMetric)
                .build();
    }
}
