package com.company.searchengine.service;

import static com.company.searchengine.model.constant.SearchConstants.Pagination.DEFAULT_PAGE;
import static com.company.searchengine.model.constant.SearchConstants.Pagination.DEFAULT_SIZE;
import static com.company.searchengine.model.constant.SearchConstants.Pagination.MAX_SIZE;

import com.company.searchengine.client.clickhouse.ClickHouseClient;
import com.company.searchengine.model.dto.BuiltQueryDto;
import com.company.searchengine.model.dto.DomainDefinitionDto;
import com.company.searchengine.model.dto.FieldDefinitionDto;
import com.company.searchengine.model.dto.request.SearchQueryRequest;
import com.company.searchengine.model.dto.response.PageResponse;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SearchQueryService {

    private final SearchDomainRegistryService searchDomainRegistryService;
    private final SearchFieldRegistryService searchFieldRegistryService;
    private final SearchValidationService searchValidationService;
    private final SearchQueryBuilderService searchQueryBuilderService;
    private final ClickHouseClient clickHouseClient;

    public PageResponse<Map<String, Object>> search(String domainCode,
                                                    SearchQueryRequest request,
                                                    String organizationId) {
        DomainDefinitionDto domain = searchDomainRegistryService.getDomain(domainCode);
        Map<String, FieldDefinitionDto> fieldMap = searchFieldRegistryService.getFieldMap(domainCode);

        searchValidationService.validate(domain, fieldMap, request);

        BuiltQueryDto query = searchQueryBuilderService.build(domain, fieldMap, request, organizationId);

        List<Map<String, Object>> content = clickHouseClient.search(query);
        long totalElements = clickHouseClient.count(query);

        int page = resolvePage(request.page());
        int size = resolveSize(request.size());

        return new PageResponse<>(
                content,
                page,
                size,
                totalElements,
                calculateTotalPages(totalElements, size),
                isLastPage(page, size, totalElements)
        );
    }

    private int resolvePage(Integer page) {
        return page == null ? DEFAULT_PAGE : Math.max(page, DEFAULT_PAGE);
    }

    private int resolveSize(Integer size) {
        return size == null ? DEFAULT_SIZE : Math.min(size, MAX_SIZE);
    }

    private int calculateTotalPages(long totalElements, int size) {
        if (totalElements == 0) {
            return 0;
        }

        return (int) Math.ceil((double) totalElements / size);
    }

    private boolean isLastPage(int page, int size, long totalElements) {
        return page + 1 >= calculateTotalPages(totalElements, size);
    }
}
