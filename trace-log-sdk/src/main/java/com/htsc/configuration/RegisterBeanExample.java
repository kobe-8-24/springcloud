package com.htsc.configuration;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.htsc.filter.SkywalkingHttpFilter;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
@ComponentScan
public class RegisterBeanExample {
    @Bean
    public SkywalkingHttpFilter getSkywalkingHttpFilter() {
        return new SkywalkingHttpFilter();
    }

    @Bean
    public ElasticsearchClient elasticsearchClient() {
        return new ElasticsearchClient(new RestClientTransport(RestClient.builder(new HttpHost("127.0.0.1",
                9200)).build(), new JacksonJsonpMapper()));
    }

    @Bean
    public RestTemplate get() {
        return new RestTemplate();
    }
}
