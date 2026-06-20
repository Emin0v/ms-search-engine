package com.company.searchengine.model.dto.request;

import com.company.searchengine.exception.validation.ValidSearchFilter;
import com.company.searchengine.model.enums.SearchOperator;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@ValidSearchFilter
public record SearchFilterRequest(
        @NotBlank String field,
        @NotNull SearchOperator operator,
        @NotNull Object value
) {
}
