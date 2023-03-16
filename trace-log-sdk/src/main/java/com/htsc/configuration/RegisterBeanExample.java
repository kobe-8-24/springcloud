package com.htsc.configuration;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.htsc.filter.SkywalkingHttpFilter;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan
public class RegisterBeanExample {
//    @Bean
//    public TraceHttpFilter get1111() {
//        return new TraceHttpFilter();
//    }

    @Bean
    public SkywalkingHttpFilter getSkywalkingHttpFilter() {
        return new SkywalkingHttpFilter();
    }

    //注入IOC容器
    @Bean
    public ElasticsearchClient elasticsearchClient(){
        // Create the low-level client
        RestClient restClient = RestClient.builder(
                new HttpHost("127.0.0.1", 9200)).build();

        // Create the transport with a Jackson mapper
        ElasticsearchTransport transport = new RestClientTransport(
                restClient, new JacksonJsonpMapper());

        // And create the API client
        ElasticsearchClient elasticsearchClient = new ElasticsearchClient(transport);
        return elasticsearchClient;

    }
}
