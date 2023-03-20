package com.htsc.filter;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import com.alibaba.fastjson.JSON;
import com.htsc.domain.PageTraceRelationDTO;
import com.htsc.util.RequestParamUtil;
import com.htsc.wrapper.ChangeHeaderRequestWrapper;
import com.htsc.wrapper.ResponseWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.skywalking.apm.toolkit.trace.ActiveSpan;
import org.apache.skywalking.apm.toolkit.trace.TraceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.htsc.constant.DefineMetrics.*;

@Component
//@ConditionalOnProperty(value = "spring.application.name", havingValue = "cache-service")
@Slf4j
public class SkywalkingHttpFilter implements Filter {
    @Autowired
    private Environment environment;

    @Autowired
    private ElasticsearchClient elasticsearchClient;

    private static final String DATE_FORMAT = "yyMMdd";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Filter.super.init(filterConfig);
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        // 获取请求
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        // 获取响应
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        // pageId
        String pageId = request.getHeader(PAGE_ID);
        // userId
        String userId = request.getHeader(USER_ID);

        // 上报ES pageId、userId、traceId 映射关系！！
        reportToEs(pageId, userId, TraceContext.traceId());

        // 重写请求头、请求参数
        ChangeHeaderRequestWrapper headerMapRequestWrapper = new ChangeHeaderRequestWrapper(request);
        // 重写响应
        ResponseWrapper responseWrapper = new ResponseWrapper(response);

        // 填充请求头
        if (!StringUtils.isEmpty(pageId)) {
            headerMapRequestWrapper.addHeader(PAGE_ID, pageId);
        }
        if (!StringUtils.isEmpty(userId)) {
            headerMapRequestWrapper.addHeader(USER_ID, userId);
        }

        filterChain.doFilter(headerMapRequestWrapper, responseWrapper);

        // 填充请求头
        if (!StringUtils.isEmpty(pageId)) {
            ActiveSpan.tag(PAGE_ID, pageId);
        }
        if (!StringUtils.isEmpty(userId)) {
            ActiveSpan.tag(USER_ID, userId);
        }

        ActiveSpan.tag(REQ_PARAMS, JSON.toJSONString(RequestParamUtil.getParams(headerMapRequestWrapper)));
        ActiveSpan.tag(SERVER_PORT, environment.getProperty(SERVER_PORT));

        ActiveSpan.tag(RESPONSE, new String(responseWrapper.getContent(), StandardCharsets.UTF_8));

        ServletOutputStream outputStream = response.getOutputStream();
        outputStream.write(responseWrapper.getContent());
        outputStream.flush();
        outputStream.close();
    }

    private void reportToEs(String pageId, String userId, String traceId) throws IOException {
        // pageId、userId、traceId 映射关系写入es
        if (!StringUtils.isEmpty(pageId) && !StringUtils.isEmpty(userId)) {
            // 获取维护 pageId、userId、traceId的索引
            String pageTraceRelationIndex = generateIndexNameByDay(IndexPrefix.SW_SEGMENT_PAGE_TRACE_RELATION);
            // 上报ES
            // 构建一个批量操作BulkOperation的集合
            List<BulkOperation> bulkOperations = new ArrayList<>();

            bulkOperations.add(new BulkOperation.Builder()
                    .create(d-> d.document(getMetricDTO(pageId, userId, traceId))
                            .index(pageTraceRelationIndex))
                    .build());
            try {
                BulkResponse bulkResponse = elasticsearchClient.bulk(e->e.index(pageTraceRelationIndex).operations(bulkOperations));
                log.info("BulkResponse errors:{}, took:{}, items:{}.", bulkResponse.errors(), bulkResponse.took(), bulkResponse.items());
            } catch (ElasticsearchException elasticsearchException) {
                log.error("reportToEs occurs ex:{}", elasticsearchException.getMessage());
            }
        }
    }

    private PageTraceRelationDTO getMetricDTO(String pageId, String userId, String traceId) {
        return PageTraceRelationDTO
                .builder()
                .pageId(pageId)
                .userId(userId)
                .traceId(traceId)
                .build();
    }

    private static String generateIndexNameByDay(String indexPrefix) {
        // 获取当前年月日
        return indexPrefix + new SimpleDateFormat(DATE_FORMAT).format(new Date());
    }

    interface IndexPrefix {
        String SW_SEGMENT_PAGE_TRACE_RELATION = "sw_segment_page_trace_relation-";
    }

    @Override
    public void destroy() {
        Filter.super.destroy();
    }
}
