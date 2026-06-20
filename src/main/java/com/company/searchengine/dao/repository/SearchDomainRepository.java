package com.company.searchengine.dao.repository;

import com.company.searchengine.dao.entity.SearchDomainEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SearchDomainRepository extends JpaRepository<SearchDomainEntity, Long> {

    Optional<SearchDomainEntity> findByCodeAndActiveTrue(String code);
}
