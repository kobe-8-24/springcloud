package com.htsc.filter;

import brave.Tracer;
import com.alibaba.fastjson.JSON;
import com.htsc.util.RequestParamUtil;
import com.htsc.wrapper.ChangeHeaderRequestWrapper;
import com.htsc.wrapper.ResponseWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Filter.super.init(filterConfig);
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        // 重写请求头、请求参数、响应
        ChangeHeaderRequestWrapper headerMapRequestWrapper = new ChangeHeaderRequestWrapper(request);
        ResponseWrapper responseWrapper = new ResponseWrapper(response);

        headerMapRequestWrapper.addHeader(PAGE_ID, request.getHeader(PAGE_ID));
        headerMapRequestWrapper.addHeader(USER_ID, request.getHeader(USER_ID));

        tracer.currentSpan()
                .tag(PAGE_ID, request.getHeader(PAGE_ID))
                .tag(USER_ID, request.getHeader(USER_ID))
                .tag(REQ_PARAMS, JSON.toJSONString(RequestParamUtil.getParams(headerMapRequestWrapper)));

        filterChain.doFilter(headerMapRequestWrapper, responseWrapper);

        tracer.currentSpan().tag(RESPONSE, new String(responseWrapper.getContent(), StandardCharsets.UTF_8));

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
