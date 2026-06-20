package com.company.searchengine.client.clickhouse;

import static com.company.searchengine.exception.model.constant.ErrorCode.CLICKHOUSE_QUERY_FAILED;
import static com.company.searchengine.exception.model.constant.ErrorMessage.CLICKHOUSE_QUERY_FAILED_ERROR_MESSAGE;

import com.company.searchengine.exception.BadRequestException;
import com.company.searchengine.model.dto.BuiltQueryDto;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ClickHouseClient {

    private final NamedParameterJdbcTemplate clickHouseTemplate;

    public ClickHouseClient(
            @Qualifier("clickHouseNamedParameterJdbcTemplate")
            NamedParameterJdbcTemplate clickHouseTemplate
    ) {
        this.clickHouseTemplate = clickHouseTemplate;
    }

    public List<Map<String, Object>> search(BuiltQueryDto query) {
        try {
            return clickHouseTemplate.queryForList(query.sql(), query.params());
        } catch (Exception ex) {
            log.error("ClickHouse search query failed. sql={}", query.sql(), ex);
            throw new BadRequestException(CLICKHOUSE_QUERY_FAILED, CLICKHOUSE_QUERY_FAILED_ERROR_MESSAGE);
        }
    }

    public long count(BuiltQueryDto query) {
        try {
            Long count = clickHouseTemplate.queryForObject(
                    query.countSql(),
                    query.params(),
                    Long.class
            );

            return count == null ? 0L : count;
        } catch (Exception ex) {
            log.error("ClickHouse count query failed. sql={}", query.countSql(), ex);
            throw new BadRequestException(CLICKHOUSE_QUERY_FAILED, CLICKHOUSE_QUERY_FAILED_ERROR_MESSAGE);
        }
    }
}
