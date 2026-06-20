package com.company.searchengine.model.dto.request;

import com.company.searchengine.exception.validation.ValidSearchQuery;
import com.company.searchengine.model.constant.SearchConstants;
import com.company.searchengine.model.enums.SortDirection;
import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

@ValidSearchQuery
public record SearchQueryRequest(
        @Valid
        @Size(max = SearchConstants.Validation.MAX_FILTERS)
        List<SearchFilterRequest> filters,

        @Size(max = SearchConstants.Validation.MAX_COLUMNS)
        List<@NotBlank String> columns,

        @Min(SearchConstants.Pagination.DEFAULT_PAGE)
        Integer page,

        @JsonAlias("perPage")
        @Min(1)
        @Max(SearchConstants.Pagination.MAX_SIZE)
        Integer size,

        String sortBy,

        @JsonAlias({"sortOrder", "direction"})
        SortDirection sortDirection
) {
}
