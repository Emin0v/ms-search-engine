package com.company.searchengine.service;

import com.company.searchengine.mapper.SearchMetadataMapper;
import com.company.searchengine.model.dto.response.SearchFieldResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SearchMetadataService {

    private final SearchDomainRegistryService searchDomainRegistryService;
    private final SearchFieldRegistryService searchFieldRegistryService;
    private final SearchMetadataMapper searchMetadataMapper;

    public List<SearchFieldResponse> getColumns(String domain) {
        searchDomainRegistryService.getDomain(domain);

        return searchFieldRegistryService.getFields(domain)
                .stream()
                .map(searchMetadataMapper::toSearchFieldResponse)
                .toList();
    }
}
