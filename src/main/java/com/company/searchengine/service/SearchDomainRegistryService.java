package com.company.searchengine.service;

import static com.company.searchengine.exception.model.constant.ErrorCode.DOMAIN_NOT_FOUND;
import static com.company.searchengine.exception.model.constant.ErrorMessage.UNSUPPORTED_SEARCH_DOMAIN_ERROR_MESSAGE;

import com.company.searchengine.dao.entity.SearchDomainEntity;
import com.company.searchengine.dao.repository.SearchDomainRepository;
import com.company.searchengine.exception.BadRequestException;
import com.company.searchengine.mapper.SearchMetadataMapper;
import com.company.searchengine.model.dto.DomainDefinitionDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SearchDomainRegistryService {

    private final SearchDomainRepository searchDomainRepository;
    private final SearchMetadataMapper searchMetadataMapper;

    @Transactional(readOnly = true)
    public DomainDefinitionDto getDomain(String domainCode) {
        SearchDomainEntity entity = searchDomainRepository.findByCodeAndActiveTrue(domainCode)
                .orElseThrow(() -> BadRequestException.of(
                        DOMAIN_NOT_FOUND,
                        UNSUPPORTED_SEARCH_DOMAIN_ERROR_MESSAGE,
                        domainCode
                ));

        return searchMetadataMapper.toDomainDefinitionDto(entity);
    }
}
