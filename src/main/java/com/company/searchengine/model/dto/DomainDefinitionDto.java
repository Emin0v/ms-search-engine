package com.company.searchengine.model.dto;

import com.company.searchengine.model.enums.SortDirection;

public record DomainDefinitionDto(
        String code,
        String clickHouseTable,
        String tenantColumn,
        String defaultSortField,
        SortDirection defaultSortDirection
) {
}
