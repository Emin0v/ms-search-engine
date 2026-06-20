
package com.company.searchengine.config;

import io.micrometer.context.ContextExecutorService;
import io.micrometer.context.ContextSnapshotFactory;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class VirtualThreadConfig {

    @Bean(name = "virtualTaskExecutor")
    public ExecutorService virtualTaskExecutor(ContextSnapshotFactory snapshotFactory) {
        ThreadFactory factory = Thread.ofVirtual().name("vt-worker-", 0).factory();

        ExecutorService executor = Executors.newThreadPerTaskExecutor(factory);

        return ContextExecutorService.wrap(executor, snapshotFactory::captureAll);
    }

}