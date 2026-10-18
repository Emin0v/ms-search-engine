package com.company.searchengine.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.company.searchengine.exception.BadRequestException;
import com.company.searchengine.model.dto.BuiltQueryDto;
import com.company.searchengine.model.dto.DomainDefinitionDto;
import com.company.searchengine.model.dto.FieldDefinitionDto;
import com.company.searchengine.model.dto.request.SearchFilterRequest;
import com.company.searchengine.model.dto.request.SearchQueryRequest;
import com.company.searchengine.model.enums.SearchFieldType;
import com.company.searchengine.model.enums.SearchOperator;
import com.company.searchengine.model.enums.SortDirection;
import jakarta.validation.Validation;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class SearchContractsTest {

    private final SearchQueryBuilderService builder = new SearchQueryBuilderService();
    private final SearchValidationService validation = new SearchValidationService();
    private final DomainDefinitionDto domain = new DomainDefinitionDto(
            "contracts", "silver.contracts", "organization_id", "id", SortDirection.ASC);
    private final Map<String, FieldDefinitionDto> fields = Map.of(
            "id", field("id", SearchFieldType.NUMBER, Set.of(SearchOperator.EQUALS, SearchOperator.BETWEEN)),
            "name", field("name", SearchFieldType.TEXT, Set.of(SearchOperator.EQUALS, SearchOperator.CONTAINS)));

    @Test
    void untrustedValuesStayInParametersForBothDataAndCountQueries() {
        String value = "x' OR 1=1 --";
        BuiltQueryDto query = builder.build(domain, fields, request(
                List.of(new SearchFilterRequest("name", SearchOperator.EQUALS, value)), 0, 20), "tenant-a");
        assertThat(query.sql()).doesNotContain(value, "tenant-a")
                .contains("`organization_id` = :tenantValue", "`name` = :p0", "FROM `silver`.`contracts`");
        assertThat(query.countSql()).contains("`organization_id` = :tenantValue", "`name` = :p0")
                .doesNotContain(value, "ORDER BY", "LIMIT");
        assertThat(query.params().getValue("p0")).isEqualTo(value);
        assertThat(query.params().getValue("tenantValue")).isEqualTo("tenant-a");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = "   ")
    void tenantScopedDomainRejectsMissingIdentity(String tenant) {
        assertThatThrownBy(() -> builder.build(domain, fields, request(List.of(), 0, 20), tenant))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void separateTenantRequestsUseSeparateParameterValues() {
        BuiltQueryDto first = builder.build(domain, fields, request(List.of(), 0, 20), "tenant-a");
        BuiltQueryDto second = builder.build(domain, fields, request(List.of(), 0, 20), "tenant-b");
        assertThat(first.params().getValue("tenantValue")).isEqualTo("tenant-a");
        assertThat(second.params().getValue("tenantValue")).isEqualTo("tenant-b");
    }

    @ParameterizedTest
    @ValueSource(strings = {"contracts; DROP TABLE users", "a.b.c", "silver.", "x`", "x y"})
    void invalidTableMetadataIsRejected(String table) {
        DomainDefinitionDto invalid = new DomainDefinitionDto("contracts", table, null, "id", SortDirection.ASC);
        assertThatThrownBy(() -> validation.validate(invalid, fields, request(List.of(), 0, 20)))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void unknownProjectionFieldIsRejected() {
        SearchQueryRequest request = new SearchQueryRequest(List.of(), List.of("unregistered"), 0, 20, null, null);
        assertThatThrownBy(() -> validation.validate(domain, fields, request))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void operatorMustBeAllowedByFieldMetadata() {
        SearchQueryRequest request = request(
                List.of(new SearchFilterRequest("name", SearchOperator.IN, List.of("a"))), 0, 20);
        assertThatThrownBy(() -> validation.validate(domain, fields, request))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void betweenValuesAreNormalizedAndBoundIndividually() {
        BuiltQueryDto query = builder.build(domain, fields, request(
                List.of(new SearchFilterRequest("id", SearchOperator.BETWEEN, List.of("10", "20"))), 0, 20), "a");
        assertThat(query.sql()).contains("`id` BETWEEN :p0From AND :p0To");
        assertThat(query.params().getValue("p0From")).isEqualTo(new BigDecimal("10"));
        assertThat(query.params().getValue("p0To")).isEqualTo(new BigDecimal("20"));
    }

    @Test
    void paginationAllowsBoundaryAndRejectsOversizedOffsetWithoutOverflow() {
        BuiltQueryDto boundary = builder.build(domain, fields, request(List.of(), 50, 200), "a");
        assertThat(boundary.params().getValue("offset")).isEqualTo(10_000L);
        assertThatThrownBy(() -> builder.build(domain, fields, request(List.of(), 51, 200), "a"))
                .isInstanceOf(BadRequestException.class);
        assertThatThrownBy(() -> builder.build(domain, fields, request(List.of(), Integer.MAX_VALUE, 200), "a"))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void beanValidationRejectsMalformedFilterShapesAndDuplicateColumns() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var validator = factory.getValidator();
            assertThat(validator.validate(new SearchFilterRequest("id", SearchOperator.BETWEEN, List.of(1))))
                    .isNotEmpty();
            assertThat(validator.validate(new SearchFilterRequest("id", SearchOperator.IN, List.of())))
                    .isNotEmpty();
            assertThat(validator.validate(new SearchQueryRequest(List.of(), List.of("id", "id"), 0, 20, null, null)))
                    .isNotEmpty();
            assertThat(validator.validate(request(List.of(), 0, 0))).isNotEmpty();
            assertThat(validator.validate(request(List.of(), 0, 20))).isEmpty();
        }
    }

    private SearchQueryRequest request(List<SearchFilterRequest> filters, Integer page, Integer size) {
        return new SearchQueryRequest(filters, List.of("id", "name"), page, size, "id", SortDirection.ASC);
    }

    private FieldDefinitionDto field(String name, SearchFieldType type, Set<SearchOperator> operators) {
        return new FieldDefinitionDto(name, name, name, type,
                true, true, false, true, false, operators, 0);
    }
}
