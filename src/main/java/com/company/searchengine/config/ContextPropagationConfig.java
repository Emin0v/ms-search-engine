package com.company.searchengine.config;

import io.micrometer.context.ContextRegistry;
import io.micrometer.context.ContextSnapshotFactory;
import io.micrometer.context.ThreadLocalAccessor;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

@Configuration
public class ContextPropagationConfig {

    @Bean
    public ContextSnapshotFactory contextSnapshotFactory() {
        ContextRegistry registry = ContextRegistry.getInstance()
                .registerThreadLocalAccessor(new MdcAccessor())
                .registerThreadLocalAccessor(new RequestAttributesAccessor());

        return ContextSnapshotFactory.builder()
                .contextRegistry(registry)
                .build();
    }

    static class MdcAccessor implements ThreadLocalAccessor<Map<String, String>> {

        @NotNull
        @Override
        public Object key() {
            return MDC.class;
        }

        @Override
        public Map<String, String> getValue() {
            return MDC.getCopyOfContextMap();
        }

        @Override
        public void setValue(@Nullable Map<String, String> value) {
            if (value != null) {
                MDC.setContextMap(value);
            } else {
                MDC.clear();
            }
        }

        @Override
        public void setValue() {
            MDC.clear();
        }
    }

    static class RequestAttributesAccessor implements ThreadLocalAccessor<RequestAttributes> {

        @NotNull
        @Override
        public Object key() {
            return RequestAttributes.class;
        }

        @Override
        public RequestAttributes getValue() {
            return RequestContextHolder.getRequestAttributes();
        }

        @Override
        public void setValue(@Nullable RequestAttributes value) {
            RequestContextHolder.setRequestAttributes(value);
        }

        @Override
        public void setValue() {
            RequestContextHolder.resetRequestAttributes();
        }

    }
}