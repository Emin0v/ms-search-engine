package com.company.searchengine.service;

import static com.company.searchengine.exception.model.constant.ErrorCode.FIELD_NOT_ALLOWED;
import static com.company.searchengine.exception.model.constant.ErrorCode.INVALID_FILTER_VALUE;
import static com.company.searchengine.exception.model.constant.ErrorCode.INVALID_SQL_IDENTIFIER;
import static com.company.searchengine.exception.model.constant.ErrorCode.OPERATOR_NOT_ALLOWED;
import static com.company.searchengine.exception.model.constant.ErrorCode.SORT_FIELD_NOT_ALLOWED;
import static com.company.searchengine.exception.model.constant.ErrorMessage.COLUMN_IS_NOT_ALLOWED_ERROR_MESSAGE;
import static com.company.searchengine.exception.model.constant.ErrorMessage.CONTAINS_OPERATOR_TYPE_ERROR_MESSAGE;
import static com.company.searchengine.exception.model.constant.ErrorMessage.DEFAULT_SORT_FIELD_NOT_CONFIGURED_ERROR_MESSAGE;
import static com.company.searchengine.exception.model.constant.ErrorMessage.FILTERABLE_FIELD_HAS_NO_OPERATORS_ERROR_MESSAGE;
import static com.company.searchengine.exception.model.constant.ErrorMessage.FILTER_FIELD_NOT_ALLOWED_ERROR_MESSAGE;
import static com.company.searchengine.exception.model.constant.ErrorMessage.INVALID_SQL_IDENTIFIER_ERROR_MESSAGE;
import static com.company.searchengine.exception.model.constant.ErrorMessage.NO_FIELDS_CONFIGURED_ERROR_MESSAGE;
import static com.company.searchengine.exception.model.constant.ErrorMessage.ONLY_CONTAINS_SUPPORTED_FOR_ARRAY_ERROR_MESSAGE;
import static com.company.searchengine.exception.model.constant.ErrorMessage.OPERATOR_NOT_ALLOWED_FOR_FIELD_ERROR_MESSAGE;
import static com.company.searchengine.exception.model.constant.ErrorMessage.RANGE_OPERATOR_TYPE_ERROR_MESSAGE;
import static com.company.searchengine.exception.model.constant.ErrorMessage.SORT_FIELD_NOT_ALLOWED_ERROR_MESSAGE;

import com.company.searchengine.exception.BadRequestException;
import com.company.searchengine.model.constant.SearchConstants;
import com.company.searchengine.model.dto.DomainDefinitionDto;
import com.company.searchengine.model.dto.FieldDefinitionDto;
import com.company.searchengine.model.dto.request.SearchFilterRequest;
import com.company.searchengine.model.dto.request.SearchQueryRequest;
import com.company.searchengine.model.enums.SearchFieldType;
import com.company.searchengine.model.enums.SearchOperator;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class SearchValidationService {

    public void validate(DomainDefinitionDto domain,
                         Map<String, FieldDefinitionDto> fieldMap,
                         SearchQueryRequest request) {
        validateDomainMetadata(domain);
        validateFieldMetadata(fieldMap);
        validateColumns(request.columns(), fieldMap);
        validateFilters(request.filters(), fieldMap);
        validateSort(request.sortBy(), domain.defaultSortField(), fieldMap);
    }

    private void validateDomainMetadata(DomainDefinitionDto domain) {
        validateQualifiedSqlIdentifier(domain.clickHouseTable());

        if (!isBlank(domain.tenantColumn())) {
            validateSimpleSqlIdentifier(domain.tenantColumn());
        }
    }

    private void validateFieldMetadata(Map<String, FieldDefinitionDto> fieldMap) {
        if (fieldMap == null || fieldMap.isEmpty()) {
            throw new BadRequestException(
                    FIELD_NOT_ALLOWED,
                    NO_FIELDS_CONFIGURED_ERROR_MESSAGE
            );
        }

        for (FieldDefinitionDto field : fieldMap.values()) {
            validateSimpleSqlIdentifier(field.sourceColumn());
            validateSimpleSqlIdentifier(field.requestField());

            if (field.filterable() && field.allowedOperators().isEmpty()) {
                throw BadRequestException.of(
                        OPERATOR_NOT_ALLOWED, FILTERABLE_FIELD_HAS_NO_OPERATORS_ERROR_MESSAGE, field.requestField());
            }
        }
    }

    private void validateColumns(List<String> columns, Map<String, FieldDefinitionDto> fieldMap) {
        if (columns == null || columns.isEmpty()) {
            return;
        }

        for (String column : columns) {
            if (!fieldMap.containsKey(column)) {
                throw BadRequestException.of(FIELD_NOT_ALLOWED, COLUMN_IS_NOT_ALLOWED_ERROR_MESSAGE, column);
            }
        }
    }

    private void validateFilters(List<SearchFilterRequest> filters, Map<String, FieldDefinitionDto> fieldMap) {
        if (filters == null || filters.isEmpty()) {
            return;
        }

        for (SearchFilterRequest filter : filters) {
            FieldDefinitionDto field = fieldMap.get(filter.field());

            if (field == null || !field.filterable()) {
                throw BadRequestException.of(
                        FIELD_NOT_ALLOWED, FILTER_FIELD_NOT_ALLOWED_ERROR_MESSAGE, filter.field());
            }

            if (!field.allowedOperators().contains(filter.operator())) {
                throw BadRequestException.of(OPERATOR_NOT_ALLOWED, OPERATOR_NOT_ALLOWED_FOR_FIELD_ERROR_MESSAGE,
                        filter.operator(), filter.field());
            }

            validateOperatorWithFieldType(field, filter.operator());
        }
    }

    private void validateSort(String requestedSortBy,
                              String defaultSortField,
                              Map<String, FieldDefinitionDto> fieldMap) {
        String sortBy = isBlank(requestedSortBy) ? defaultSortField : requestedSortBy;

        if (isBlank(sortBy)) {
            throw new BadRequestException(SORT_FIELD_NOT_ALLOWED, DEFAULT_SORT_FIELD_NOT_CONFIGURED_ERROR_MESSAGE);
        }

        FieldDefinitionDto sortField = fieldMap.get(sortBy);

        if (sortField == null || !sortField.sortable()) {
            throw BadRequestException.of(SORT_FIELD_NOT_ALLOWED, SORT_FIELD_NOT_ALLOWED_ERROR_MESSAGE, sortBy);
        }
    }

    private void validateOperatorWithFieldType(FieldDefinitionDto field, SearchOperator operator) {
        if (field.arrayField()) {
            if (operator != SearchOperator.CONTAINS) {
                throw new BadRequestException(INVALID_FILTER_VALUE, ONLY_CONTAINS_SUPPORTED_FOR_ARRAY_ERROR_MESSAGE);
            }
            return;
        }

        SearchFieldType type = field.type();

        if ((operator == SearchOperator.BETWEEN
                || operator == SearchOperator.GREATER_THAN
                || operator == SearchOperator.LESS_THAN)
                && type != SearchFieldType.NUMBER
                && type != SearchFieldType.DATE
                && type != SearchFieldType.DATE_TIME) {
            throw BadRequestException.of(INVALID_FILTER_VALUE, RANGE_OPERATOR_TYPE_ERROR_MESSAGE, operator);
        }

        if (operator == SearchOperator.CONTAINS
                && type != SearchFieldType.TEXT
                && type != SearchFieldType.SELECT) {
            throw new BadRequestException(INVALID_FILTER_VALUE, CONTAINS_OPERATOR_TYPE_ERROR_MESSAGE);
        }
    }

    private void validateQualifiedSqlIdentifier(String value) {
        if (isBlank(value)) {
            throw BadRequestException.of(INVALID_SQL_IDENTIFIER, INVALID_SQL_IDENTIFIER_ERROR_MESSAGE, value);
        }

        String[] parts = value.split("\\.", -1);

        if (parts.length > 2) {
            throw BadRequestException.of(INVALID_SQL_IDENTIFIER, INVALID_SQL_IDENTIFIER_ERROR_MESSAGE, value);
        }

        for (String part : parts) {
            validateSimpleSqlIdentifier(part);
        }
    }

    private void validateSimpleSqlIdentifier(String value) {
        if (isBlank(value) || !SearchConstants.Validation.SIMPLE_SQL_IDENTIFIER_PATTERN.matcher(value).matches()) {
            throw BadRequestException.of(INVALID_SQL_IDENTIFIER, INVALID_SQL_IDENTIFIER_ERROR_MESSAGE, value);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
