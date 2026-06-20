package com.company.searchengine.dao.entity;

import com.company.searchengine.model.enums.SearchFieldType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
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
@Table(name = "search_field")
public class SearchFieldEntity extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "domain_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_search_field_domain")
    )
    private SearchDomainEntity domain;

    @NotBlank
    @Size(max = 100)
    @Column(name = "request_field", nullable = false, length = 100)
    private String requestField;

    @NotBlank
    @Size(max = 100)
    @Column(name = "source_column", nullable = false, length = 100)
    private String sourceColumn;

    @NotBlank
    @Size(max = 255)
    @Column(name = "label", nullable = false, length = 255)
    private String label;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 30)
    private SearchFieldType type;

    @NotNull
    @Builder.Default
    @Column(name = "filterable", nullable = false)
    private Boolean filterable = false;

    @NotNull
    @Builder.Default
    @Column(name = "sortable", nullable = false)
    private Boolean sortable = false;

    @NotNull
    @Builder.Default
    @Column(name = "exportable", nullable = false)
    private Boolean exportable = false;

    @NotNull
    @Builder.Default
    @Column(name = "projection_default", nullable = false)
    private Boolean projectionDefault = false;

    @NotNull
    @Builder.Default
    @Column(name = "array_field", nullable = false)
    private Boolean arrayField = false;

    @NotNull
    @Size(max = 500)
    @Column(name = "allowed_operators", nullable = false, length = 500)
    private String allowedOperators;

    @Min(0)
    @Column(name = "sort_order")
    private Integer sortOrder;

    @NotNull
    @Builder.Default
    @Column(name = "active", nullable = false)
    private Boolean active = true;
}
