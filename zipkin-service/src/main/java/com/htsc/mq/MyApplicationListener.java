package com.htsc.mq;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.htsc.constant.IndexPrefix;
import com.htsc.domain.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static com.htsc.constant.CommonMetrics.PAGE_ID;
import static com.htsc.constant.CommonMetrics.USER_ID;
import static com.htsc.util.GenerateIndexUtil.generateIndexNameByDay;

/**
 * 等待5min, 去es sw_segment_page_trace_relation-20230316 獲取
 */
@Component
@Slf4j
public class MyApplicationListener implements ApplicationListener<FrontMqApplicationEvent> {
    @Autowired
    private ElasticsearchClient elasticsearchClient;

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public void onApplicationEvent(FrontMqApplicationEvent event) {
        long t1 = System.currentTimeMillis();
        log.info("start MyApplicationListener. startTime:{}", t1);

        try {
            // 等待5min 当前链路完成！！！
            Thread.sleep(5 * 60 * 1000);
            log.info("start MyApplicationListener. t2:{}", System.currentTimeMillis() - t1);
        } catch (InterruptedException e) {
            log.error("MyApplicationListener occurs ex:{}", e.getMessage());
        }

        // 拿到前端發送的pageId, 查詢 es sw_segment_page_trace_relation-20230316索引獲取traceId
        String pageId = event.getPageId();
        String userId = event.getUserId();
        // TODO pageId 查詢 sw_segment_self_front-20230316 获取前端数据
        // TODO 这一步目前暂时不用查询. 消息里面包含前端指标
        String frontReportMetric = event.getFrontReportMetric();
        List<String> traceIdList = getTraceIds(pageId, userId, generateIndexNameByDay(IndexPrefix.SW_SEGMENT_PAGE_TRACE_RELATION));
        
        // traceId調用skywalking oap接口 獲取 後端數據; 拿不到数据是否要去sw ES索引查询？？？
        String backEndMetricTrace = queryBackEndMetricTrace(traceIdList);
        log.info("backEndMetricTrace:{}", backEndMetricTrace);

        // 组装 前后端数据写入 sw_segment_full_link-20230316
        String fullLinkIndex = generateIndexNameByDay(IndexPrefix.SW_SEGMENT_FULL_LINK);
        // 构建一个批量操作BulkOperation的集合
        List<BulkOperation> bulkOperations = new ArrayList<>();

        bulkOperations.add(new BulkOperation.Builder()
                .create(d-> d.document(getMetricDTO(pageId, userId, frontReportMetric, backEndMetricTrace, System.currentTimeMillis()))
                        .index(fullLinkIndex))
                .build());

        BulkResponse response = null;
        try {
            response = elasticsearchClient.bulk(e -> e.index(fullLinkIndex).operations(bulkOperations));
        } catch (IOException e) {
            log.error("elasticsearchClient OCCURS EX:{}", e.getMessage());
        }

        assert response != null;
        log.info("BulkResponse errors:{}, took:{}, items:{}.", response.errors(), response.took(), response.items());
    }

    private String queryBackEndMetricTrace(List<String> traceIdList) {
        List<String> backEndMetricTrace = new ArrayList<>();
        traceIdList.forEach(traceId -> {
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Type", "application/json");
            HttpEntity<String> entity = new HttpEntity<>(JSON.toJSONString(getGraphqlDTO(traceId)), headers);

            ResponseEntity<JSONObject> postForObject = restTemplate
                    .postForEntity("http://127.0.0.1:8080/graphql", entity, JSONObject.class);

            backEndMetricTrace.add(Objects.requireNonNull(postForObject.getBody()).toString());
        });
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

    private FullLinkDTO getMetricDTO(String pageId, String userId, String frontReportMetric,
                                     String backEndMetricTrace, long finishTime) {
        return FullLinkDTO
                .builder()
                .pageId(pageId)
                .userId(userId)
                .finishTime(finishTime)
                .frontMetricTrace(frontReportMetric)
                .backEndMetricTrace(backEndMetricTrace)
                .build();
    }

    private List<String> getTraceIds(String pageId, String userId, String pageTraceRelationIndex) {
        try {
            SearchResponse<PageTraceRelationDTO> searchResponse = elasticsearchClient.search(
                    s -> s.index(pageTraceRelationIndex).query(q -> q.bool(b -> b
                            .must(m -> m.match(u -> u.field(PAGE_ID).query(pageId)))
                            .must(m -> m.match(u -> u.field(USER_ID).query(userId)))
                    )), PageTraceRelationDTO.class);

                return searchResponse
                        .hits()
                        .hits()
                        .stream()
                        .map(s -> Objects.requireNonNull(s.source()).getTraceId())
                        .distinct()
                        .collect(Collectors.toList());
        } catch (IOException e) {
            log.error("pageTraceRelationIndex query Occurs ex:{}", e.getMessage());
        }

        return new ArrayList<>();
    }
}