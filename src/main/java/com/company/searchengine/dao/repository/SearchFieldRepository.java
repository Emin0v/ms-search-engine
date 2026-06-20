package com.company.searchengine.dao.repository;

import com.company.searchengine.dao.entity.SearchFieldEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SearchFieldRepository extends JpaRepository<SearchFieldEntity, Long> {

    @Query("""
            SELECT field
            FROM SearchFieldEntity field
            JOIN field.domain domain
            WHERE domain.code = :domainCode
              AND domain.active = true
              AND field.active = true
            ORDER BY COALESCE(field.sortOrder, 2147483647), field.id
            """)
    List<SearchFieldEntity> findActiveFieldsByDomainCode(String domainCode);
}
