package com.company.searchengine.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "clickhouse")
public record ClickHouseProperties(
        String url,
        String username,
        String password,
        Integer maximumPoolSize,
        Integer minimumIdle,
        Long connectionTimeoutMs,
        Long idleTimeoutMs,
        Long maxLifetimeMs
) {

    private static final int DEFAULT_MAXIMUM_POOL_SIZE = 10;
    private static final int DEFAULT_MINIMUM_IDLE = 2;
    private static final long DEFAULT_CONNECTION_TIMEOUT_MS = 30_000L;
    private static final long DEFAULT_IDLE_TIMEOUT_MS = 600_000L;
    private static final long DEFAULT_MAX_LIFETIME_MS = 1_800_000L;

    public int resolvedMaximumPoolSize() {
        return maximumPoolSize == null || maximumPoolSize <= 0
                ? DEFAULT_MAXIMUM_POOL_SIZE
                : maximumPoolSize;
    }

    public int resolvedMinimumIdle() {
        int resolvedMinimumIdle = minimumIdle == null || minimumIdle < 0
                ? DEFAULT_MINIMUM_IDLE
                : minimumIdle;

        return Math.min(resolvedMinimumIdle, resolvedMaximumPoolSize());
    }

    public long resolvedConnectionTimeoutMs() {
        return connectionTimeoutMs == null || connectionTimeoutMs <= 0
                ? DEFAULT_CONNECTION_TIMEOUT_MS
                : connectionTimeoutMs;
    }

    public long resolvedIdleTimeoutMs() {
        return idleTimeoutMs == null || idleTimeoutMs <= 0
                ? DEFAULT_IDLE_TIMEOUT_MS
                : idleTimeoutMs;
    }

    public long resolvedMaxLifetimeMs() {
        return maxLifetimeMs == null || maxLifetimeMs <= 0
                ? DEFAULT_MAX_LIFETIME_MS
                : maxLifetimeMs;
    }
}
