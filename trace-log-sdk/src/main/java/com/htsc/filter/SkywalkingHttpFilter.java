package com.htsc.filter;

import brave.Span;
import com.alibaba.fastjson.JSON;
import com.htsc.util.RequestParamUtil;
import com.htsc.wrapper.ChangeHeaderRequestWrapper;
import com.htsc.wrapper.ResponseWrapper;
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

import static com.htsc.constant.DefineMetrics.*;

@Component
@ConditionalOnProperty(value = "spring.application.name", havingValue = "cache-service")
public class SkywalkingHttpFilter implements Filter {
    @Autowired
    private Environment environment;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Filter.super.init(filterConfig);
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        String pageId = request.getHeader(PAGE_ID);
        String userId = request.getHeader(USER_ID);

        // 重写请求头、请求参数、响应
        ChangeHeaderRequestWrapper headerMapRequestWrapper = new ChangeHeaderRequestWrapper(request);
        ResponseWrapper responseWrapper = new ResponseWrapper(response);

        if (!StringUtils.isEmpty(pageId)) {
            headerMapRequestWrapper.addHeader(PAGE_ID, pageId);
        }

        if (!StringUtils.isEmpty(userId)) {
            headerMapRequestWrapper.addHeader(USER_ID, userId);
        }

        filterChain.doFilter(headerMapRequestWrapper, responseWrapper);

        if (!StringUtils.isEmpty(pageId)) {
            ActiveSpan.tag(PAGE_ID, pageId);
        }
        if (!StringUtils.isEmpty(userId)) {
            ActiveSpan.tag(USER_ID, userId);
        }
        ActiveSpan.tag(REQ_PARAMS, JSON.toJSONString(RequestParamUtil.getParams(headerMapRequestWrapper)));
        ActiveSpan.tag("server.port", environment.getProperty("server.port"));
        ActiveSpan.tag(RESPONSE, new String(responseWrapper.getContent(), StandardCharsets.UTF_8));

//        TraceContext.putCorrelation(PAGE_ID, pageId);
//        TraceContext.putCorrelation(USER_ID, userId);

        ServletOutputStream outputStream = response.getOutputStream();
        outputStream.write(responseWrapper.getContent());
        outputStream.flush();
        outputStream.close();


    }

    @Override
    public void destroy() {
        Filter.super.destroy();
    }
}
