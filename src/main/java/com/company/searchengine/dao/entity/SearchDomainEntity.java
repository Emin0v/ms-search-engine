package com.company.searchengine.dao.entity;

import com.company.searchengine.model.enums.SortDirection;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

@Data
@Entity
@SuperBuilder
@DynamicInsert
@DynamicUpdate
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Table(name = "search_domain")
public class SearchDomainEntity extends BaseEntity {

    @NotBlank
    @Size(max = 100)
    @Column(name = "code", nullable = false, unique = true, length = 100)
    private String code;

    @NotBlank
    @Size(max = 200)
    @Column(name = "clickhouse_table", nullable = false, length = 200)
    private String clickHouseTable;

    @Size(max = 100)
    @Column(name = "tenant_column", length = 100)
    private String tenantColumn;

    @Size(max = 100)
    @Column(name = "default_sort_field", length = 100)
    private String defaultSortField;

    @Enumerated(EnumType.STRING)
    @Column(name = "default_sort_direction", length = 10)
    private SortDirection defaultSortDirection;

    @NotNull
    @Builder.Default
    @Column(name = "active", nullable = false)
    private Boolean active = true;
}
