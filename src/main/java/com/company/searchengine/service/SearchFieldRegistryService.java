package com.company.searchengine.service;

import static com.company.searchengine.exception.model.constant.ErrorCode.CONFIGURATION_ERROR;
import static com.company.searchengine.exception.model.constant.ErrorMessage.DUPLICATE_ACTIVE_SEARCH_FIELD_ERROR_MESSAGE;

import com.company.searchengine.dao.repository.SearchFieldRepository;
import com.company.searchengine.exception.BadRequestException;
import com.company.searchengine.mapper.SearchMetadataMapper;
import com.company.searchengine.model.dto.FieldDefinitionDto;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SearchFieldRegistryService {

    private final SearchFieldRepository searchFieldRepository;
    private final SearchMetadataMapper searchMetadataMapper;

    public List<FieldDefinitionDto> getFields(String domainCode) {
        return searchFieldRepository.findActiveFieldsByDomainCode(domainCode)
                .stream()
                .map(searchMetadataMapper::toFieldDefinitionDto)
                .toList();
    }

    public Map<String, FieldDefinitionDto> getFieldMap(String domainCode) {
        return getFields(domainCode)
                .stream()
                .collect(Collectors.toMap(
                        FieldDefinitionDto::requestField,
                        field -> field,
                        (left, right) -> {
                            throw BadRequestException.of(CONFIGURATION_ERROR,
                                    DUPLICATE_ACTIVE_SEARCH_FIELD_ERROR_MESSAGE, left.requestField());
                        },
                        LinkedHashMap::new
                ));
    }
}
