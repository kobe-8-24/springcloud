package com.htsc.cron;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.mapping.KeywordProperty;
import co.elastic.clients.elasticsearch._types.mapping.LongNumberProperty;
import co.elastic.clients.elasticsearch._types.mapping.Property;
import co.elastic.clients.elasticsearch.indices.*;
import co.elastic.clients.transport.endpoints.BooleanResponse;
import com.htsc.constant.IndexPrefix;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

import static com.htsc.util.GenerateIndexUtil.generateIndexNameByDay;

/**
 * 定时任务 每天0点新增 前端指标上报的索引以及mapping
 */
@Component
@Slf4j
public class FrontIndexCronJob implements IndexPrefix {
    @Autowired
    private ElasticsearchClient elasticsearchClient;

    /**
     * 创建索引:若索引存在，先删除再创建
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void createIndexMappings() {
        IndexPrefix.getAllIndexPrefix().forEach(s -> createIndex(generateIndexNameByDay(s), generateIndexMappingsByType(s)));
    }

    private Map<String, Property> generateIndexMappingsByType(String s) {
        Map<String, Property> propertyMap = new HashMap<>();

        switch (s) {
            case IndexPrefix.SW_SEGMENT_SELF_FRONT:
                return createSelfFrontIndexMappings(propertyMap);
            case IndexPrefix.SW_SEGMENT_PAGE_TRACE_RELATION:
                return createPageIdTraceIdRelationIndexMappings(propertyMap);
            case IndexPrefix.SW_SEGMENT_FULL_LINK:
                return createFullLinkIndexMappings(propertyMap);
            default:
                break;
        }

        return propertyMap;
    }

    private Map<String, Property> createFullLinkIndexMappings(Map<String, Property> propertyMap) {
        propertyMap.put("pageId", Property.
                of(property -> property.keyword(KeywordProperty.of(textProperty -> textProperty.index(true)))));
        propertyMap.put("userId", Property.
                of(property -> property.keyword(KeywordProperty.of(textProperty -> textProperty.index(true)))));
        propertyMap.put("finishTime", Property
                .of(property -> property.long_(LongNumberProperty.of(l -> l.index(true)))));
        propertyMap.put("backEndMetricTrace", Property.
                of(property -> property.keyword(KeywordProperty.of(textProperty -> textProperty.index(true)))));
        propertyMap.put("frontMetricTrace", Property.
                of(property -> property.keyword(KeywordProperty.of(textProperty -> textProperty.index(true)))));
        return propertyMap;
    }

    private Map<String, Property> createPageIdTraceIdRelationIndexMappings(Map<String, Property> propertyMap) {
        propertyMap.put("pageId", Property.
                of(property -> property.keyword(KeywordProperty.of(textProperty -> textProperty.index(true)))));
        propertyMap.put("userId", Property.
                of(property -> property.keyword(KeywordProperty.of(textProperty -> textProperty.index(true)))));
        propertyMap.put("traceId", Property.
                of(property -> property.keyword(KeywordProperty.of(textProperty -> textProperty.index(true)))));
        return propertyMap;
    }

    private Map<String, Property> createSelfFrontIndexMappings(Map<String, Property> propertyMap) {
        propertyMap.put("pageId", Property.
                of(property -> property.keyword(KeywordProperty.of(textProperty -> textProperty.index(true)))));
        propertyMap.put("userId", Property.
                of(property -> property.keyword(KeywordProperty.of(textProperty -> textProperty.index(true)))));
        propertyMap.put("frontReportMetric", Property.
                of(property -> property.keyword(KeywordProperty.of(textProperty -> textProperty.index(true)))));
        return propertyMap;
    }

    public void createIndex(String indexName, Map<String, Property> mappings) {
        try {
            //创建索引
            CreateIndexRequest createIndexRequest = CreateIndexRequest
                    .of(e -> e.index(indexName).mappings(m -> m.properties(mappings)));
            //先判断索引是否存在，存在则删除后再创建
            BooleanResponse existRes = elasticsearchClient.indices().exists(ExistsRequest.of(e -> e.index(indexName)));
            if (existRes.value()) {
                deleteIndex(indexName);
            }
            CreateIndexResponse createIndexResponse = elasticsearchClient.indices().create(createIndexRequest);
            log.info("Index {} created {}", indexName, createIndexResponse.acknowledged());
        } catch (Exception e) {
            log.error("Index {} created fail. e:{}", indexName, e);
        }
    }

    private void deleteIndex(String indexName) {
        ElasticsearchIndicesClient indices = elasticsearchClient.indices();
        try {
            DeleteIndexRequest delete_request = new DeleteIndexRequest.Builder().index(indexName).build();
            DeleteIndexResponse delete_response = indices.delete(delete_request);
            log.info("Index {} deleted {}", indexName, delete_response.acknowledged());
        } catch (Exception e) {
            log.error("Index {} deleted fail. e:{}", indexName, e);
        }
    }

}
