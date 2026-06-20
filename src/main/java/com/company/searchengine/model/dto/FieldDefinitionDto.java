package com.company.searchengine.model.dto;

import com.company.searchengine.model.enums.SearchFieldType;
import com.company.searchengine.model.enums.SearchOperator;
import java.util.Set;

public record FieldDefinitionDto(
        String requestField,
        String sourceColumn,
        String label,
        SearchFieldType type,
        boolean filterable,
        boolean sortable,
        boolean exportable,
        boolean projectionDefault,
        boolean arrayField,
        Set<SearchOperator> allowedOperators,
        Integer sortOrder
) {
}
