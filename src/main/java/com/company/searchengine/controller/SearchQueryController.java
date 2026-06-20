package com.company.searchengine.controller;

import static com.company.searchengine.model.constant.ApplicationConstants.HttpHeader.AC_ORGANIZATION_ID;

import com.company.searchengine.model.dto.request.SearchQueryRequest;
import com.company.searchengine.model.dto.response.PageResponse;
import com.company.searchengine.service.SearchQueryService;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/search")
public class SearchQueryController {

    private final SearchQueryService searchQueryService;

    @PostMapping("/{domain}/query")
    public ResponseEntity<PageResponse<Map<String, Object>>> search(
            @PathVariable String domain,
            @RequestHeader(value = AC_ORGANIZATION_ID, required = false) String organizationId,
            @Valid @RequestBody SearchQueryRequest request) {
        return ResponseEntity.ok(searchQueryService.search(domain, request, organizationId));
    }
}
