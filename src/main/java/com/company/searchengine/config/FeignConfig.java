package com.company.searchengine.config;

import static com.company.searchengine.model.constant.ApplicationConstants.HttpHeader.AC_ORIGIN_SERVICE;

import com.company.searchengine.util.WebUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.RequestInterceptor;
import feign.codec.ErrorDecoder;
import feign.okhttp.OkHttpClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableFeignClients(basePackages = "com.company.searchengine.client")
public class FeignConfig {

    @Value("${spring.application.name}")
    private String serviceName;

    private final WebUtil webUtil;

    @Bean
    public OkHttpClient client() {
        return new OkHttpClient();
    }

    @Bean
    public ErrorDecoder errorDecoder(ObjectMapper objectMapper) {
        return new ClientErrorDecoder(objectMapper);
    }

    @Bean
    public RequestInterceptor feignRequestInterceptor() {
        return requestTemplate -> {
            requestTemplate.header(AC_ORIGIN_SERVICE, serviceName);
            webUtil.getAcHeaders().forEach((headerName, headerValue) -> {
                if (!requestTemplate.headers().containsKey(headerName)) {
                    requestTemplate.header(headerName, headerValue);
                }
            });
        };
    }
}