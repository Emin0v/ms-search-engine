package com.company.searchengine.controller;

import com.company.searchengine.model.dto.response.SearchFieldResponse;
import com.company.searchengine.service.SearchMetadataService;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
public class SearchMetadataController {

    private final SearchMetadataService searchMetadataService;

    @GetMapping("/{domain}/meta/columns")
    public List<SearchFieldResponse> getColumns(@PathVariable @NotBlank String domain) {
        return searchMetadataService.getColumns(domain);
    }
}