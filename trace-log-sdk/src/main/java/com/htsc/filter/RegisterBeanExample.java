package com.htsc.filter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan
public class RegisterBeanExample {
    @Bean
    public TraceHttpFilter get1111() {
        return new TraceHttpFilter();
    }
}
