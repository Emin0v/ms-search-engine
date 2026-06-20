package com.company.searchengine.config;

import com.company.searchengine.config.properties.ClickHouseProperties;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(ClickHouseProperties.class)
public class ClickHouseConfig {

    private static final String CLICKHOUSE_DRIVER_CLASS_NAME = "com.clickhouse.jdbc.ClickHouseDriver";

    private final ClickHouseProperties properties;

    @Bean(name = "clickHouseDataSource")
    public DataSource clickHouseDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(properties.url());
        config.setUsername(properties.username());
        config.setPassword(properties.password());
        config.setDriverClassName(CLICKHOUSE_DRIVER_CLASS_NAME);
        config.setMaximumPoolSize(properties.resolvedMaximumPoolSize());
        config.setMinimumIdle(properties.resolvedMinimumIdle());
        config.setConnectionTimeout(properties.resolvedConnectionTimeoutMs());
        config.setIdleTimeout(properties.resolvedIdleTimeoutMs());
        config.setMaxLifetime(properties.resolvedMaxLifetimeMs());
        config.setPoolName("clickhouse-pool");

        return new HikariDataSource(config);
    }

    @Bean(name = "clickHouseNamedParameterJdbcTemplate")
    public NamedParameterJdbcTemplate clickHouseNamedParameterJdbcTemplate(
            @Qualifier("clickHouseDataSource") DataSource dataSource) {
        return new NamedParameterJdbcTemplate(dataSource);
    }
}
