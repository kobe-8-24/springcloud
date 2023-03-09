package com.htsc.filter;

import brave.Span;
import brave.SpanCustomizer;
import brave.Tracer;
import com.alibaba.fastjson.JSON;
import com.htsc.util.RequestParamUtil;
import com.htsc.wrapper.ChangeHeaderRequestWrapper;
import com.htsc.wrapper.ResponseWrapper;
import org.springframework.beans.factory.annotation.Autowired;
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
public class TraceHttpFilter implements Filter {
    @Autowired
    private Tracer tracer;

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
        Span span = tracer.currentSpan();

        if (!StringUtils.isEmpty(pageId)) {
            headerMapRequestWrapper.addHeader(PAGE_ID, pageId);
            span.tag(PAGE_ID, pageId);
        }

        if (!StringUtils.isEmpty(userId)) {
            headerMapRequestWrapper.addHeader(USER_ID, userId);
            span.tag(USER_ID, userId);
        }

        span.tag(REQ_PARAMS, JSON.toJSONString(RequestParamUtil.getParams(headerMapRequestWrapper)));

        span.tag("server.port", environment.getProperty("server.port"));

        filterChain.doFilter(headerMapRequestWrapper, responseWrapper);

        span.tag(RESPONSE, new String(responseWrapper.getContent(), StandardCharsets.UTF_8));

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
