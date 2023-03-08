package com.htsc.filter;

import brave.Tracer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@Component
public class TraceHttpFilter implements Filter {
    @Autowired
    private Tracer tracer;

    // 页面pageId
    private static final String PAGE_ID = "pageId";

    // 用户id
    private static final String USER_ID = "userId";


    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Filter.super.init(filterConfig);
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        tracer.currentSpan()
                .tag(PAGE_ID, request.getHeader(PAGE_ID))
                .tag(USER_ID, request.getHeader(USER_ID));
        filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy() {
        Filter.super.destroy();
    }
}
