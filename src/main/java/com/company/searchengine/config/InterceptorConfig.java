package com.company.searchengine.config;

import com.company.searchengine.interceptor.RequestInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
@ConditionalOnBean(value = RequestInterceptor.class)
public class InterceptorConfig implements WebMvcConfigurer {

    @Value("${application.interceptor.request.exclude-path-patterns}")
    private String[] excludePaths;

    private final RequestInterceptor requestInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(requestInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(excludePaths);
    }

}


