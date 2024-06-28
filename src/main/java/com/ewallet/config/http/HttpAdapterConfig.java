package com.ewallet.config.http;

import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.HttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@Slf4j
public class HttpAdapterConfig {

    @Value("${http-adapter.pooling-max-connection:100}")
    private Integer cfMaxTotal;

    @Value("${http-adapter.pooling-max-connection-per-route:100}")
    private Integer cfMaxPerRout;

    @Value("${http-adapter.http-timeout:30000}")
    private Integer timeout;

    @Bean
    public HttpClient httpClient() {
        return HttpClientUtil.createClient(cfMaxTotal, cfMaxPerRout, timeout);
    }

    @Bean
    public HttpClientUtil httpClientUtil(HttpClient httpClient) {
        return new HttpClientUtil(httpClient, cfMaxTotal, cfMaxPerRout);
    }
}
