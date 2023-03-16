package com.htsc.controller;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import com.alibaba.fastjson.JSON;
import com.htsc.domain.FrontMetricDTO;
import com.htsc.domain.FrontMqApplicationEvent;
import com.htsc.util.GenerateIndexUtil;
import com.htsc.constant.IndexPrefix;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.ArrayList;
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

    @PostMapping("/traceId")
    public String reportFrontMetricsToEsAndMq(@RequestBody Map<String, Object> map) throws IOException {
        // 获取前端指标数据
        String pageId = (String) map.get("pageId");
        String userId = (String) map.get("userId");
        String frontReportMetric = (String) map.get("frontReportMetric");

        String reportFrontIndex = GenerateIndexUtil.generateIndexNameByDay(IndexPrefix.SW_SEGMENT_SELF_FRONT);

        // 上报ES
        // 构建一个批量操作BulkOperation的集合
        List<BulkOperation> bulkOperations = new ArrayList<>();

        bulkOperations.add(new BulkOperation.Builder()
                .create(d-> d.document(getMetricDTO(pageId, userId, frontReportMetric))
                        .index(reportFrontIndex))
                .build());

        BulkResponse response = elasticsearchClient.bulk(e->e.index(reportFrontIndex).operations(bulkOperations));

        log.info("BulkResponse errors:{}, took:{}, items:{}.", response.errors(), response.took(), response.items());

        // pub异步消息
        applicationEventPublisher.publishEvent(new FrontMqApplicationEvent(this, pageId, userId, frontReportMetric));

        return JSON.toJSONString(response.items());
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
