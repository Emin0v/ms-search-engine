package com.company.searchengine.model.dto;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

public record BuiltQueryDto(
        String sql,
        String countSql,
        MapSqlParameterSource params
) {
}
