package com.company.searchengine.service;

import static com.company.searchengine.exception.model.constant.ErrorCode.FIELD_NOT_ALLOWED;
import static com.company.searchengine.exception.model.constant.ErrorCode.INVALID_FILTER_VALUE;
import static com.company.searchengine.exception.model.constant.ErrorCode.INVALID_SQL_IDENTIFIER;
import static com.company.searchengine.exception.model.constant.ErrorCode.OPERATOR_NOT_ALLOWED;
import static com.company.searchengine.exception.model.constant.ErrorMessage.DEEP_PAGINATION_NOT_ALLOWED_ERROR_MESSAGE;
import static com.company.searchengine.exception.model.constant.ErrorMessage.FIELD_NOT_CONFIGURED_ERROR_MESSAGE;
import static com.company.searchengine.exception.model.constant.ErrorMessage.INVALID_SQL_IDENTIFIER_ERROR_MESSAGE;
import static com.company.searchengine.exception.model.constant.ErrorMessage.NO_PROJECTION_FIELDS_CONFIGURED_ERROR_MESSAGE;
import static com.company.searchengine.exception.model.constant.ErrorMessage.OPERATOR_NOT_ALLOWED_ERROR_MESSAGE;
import static com.company.searchengine.exception.model.constant.ErrorMessage.TENANT_VALUE_REQUIRED_ERROR_MESSAGE;
import static com.company.searchengine.model.constant.SearchConstants.Pagination.DEFAULT_PAGE;
import static com.company.searchengine.model.constant.SearchConstants.Pagination.DEFAULT_SIZE;
import static com.company.searchengine.model.constant.SearchConstants.Pagination.MAX_OFFSET;
import static com.company.searchengine.model.constant.SearchConstants.Pagination.MAX_SIZE;
import static com.company.searchengine.model.constant.SearchConstants.Sql.AND;
import static com.company.searchengine.model.constant.SearchConstants.Sql.DEFAULT_TIE_BREAKER_FIELD;
import static com.company.searchengine.model.constant.SearchConstants.Sql.FROM;
import static com.company.searchengine.model.constant.SearchConstants.Sql.LIMIT_OFFSET;
import static com.company.searchengine.model.constant.SearchConstants.Sql.ORDER_BY;
import static com.company.searchengine.model.constant.SearchConstants.Sql.PARAM_LIMIT;
import static com.company.searchengine.model.constant.SearchConstants.Sql.PARAM_OFFSET;
import static com.company.searchengine.model.constant.SearchConstants.Sql.PARAM_TENANT_VALUE;
import static com.company.searchengine.model.constant.SearchConstants.Sql.SELECT;
import static com.company.searchengine.model.constant.SearchConstants.Sql.SELECT_COUNT;
import static com.company.searchengine.model.constant.SearchConstants.Sql.WHERE_TRUE;

import com.company.searchengine.exception.BadRequestException;
import com.company.searchengine.model.constant.SearchConstants;
import com.company.searchengine.model.dto.BuiltQueryDto;
import com.company.searchengine.model.dto.DomainDefinitionDto;
import com.company.searchengine.model.dto.FieldDefinitionDto;
import com.company.searchengine.model.dto.request.SearchFilterRequest;
import com.company.searchengine.model.dto.request.SearchQueryRequest;
import com.company.searchengine.model.enums.SearchOperator;
import com.company.searchengine.model.enums.SortDirection;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Service;

@Service
public class SearchQueryBuilderService {

    public BuiltQueryDto build(DomainDefinitionDto domain,
                               Map<String, FieldDefinitionDto> fieldMap,
                               SearchQueryRequest request,
                               String tenantValue) {
        MapSqlParameterSource params = new MapSqlParameterSource();

        StringBuilder fromWhere = new StringBuilder()
                .append(FROM)
                .append(quoteQualifiedIdentifier(domain.clickHouseTable()))
                .append(WHERE_TRUE);

        appendTenantFilter(fromWhere, params, domain, tenantValue);
        appendFilters(fromWhere, params, fieldMap, request.filters());

        String sql = SELECT + buildProjection(request.columns(), fieldMap)
                + fromWhere
                + buildOrderBy(domain, fieldMap, request)
                + buildPagination(params, request);

        String countSql = SELECT_COUNT + fromWhere;

        return new BuiltQueryDto(sql, countSql, params);
    }

    private void appendTenantFilter(StringBuilder where,
                                    MapSqlParameterSource params,
                                    DomainDefinitionDto domain,
                                    String tenantValue) {
        if (isBlank(domain.tenantColumn())) {
            return;
        }

        if (isBlank(tenantValue)) {
            throw BadRequestException.of(INVALID_FILTER_VALUE, TENANT_VALUE_REQUIRED_ERROR_MESSAGE, domain.code());
        }

        where.append(AND)
                .append(quoteSimpleIdentifier(domain.tenantColumn()))
                .append(" = :")
                .append(PARAM_TENANT_VALUE);

        params.addValue(PARAM_TENANT_VALUE, tenantValue);
    }

    private void appendFilters(StringBuilder where,
                               MapSqlParameterSource params,
                               Map<String, FieldDefinitionDto> fieldMap,
                               List<SearchFilterRequest> filters) {
        if (filters == null || filters.isEmpty()) {
            return;
        }

        for (int index = 0; index < filters.size(); index++) {
            SearchFilterRequest filter = filters.get(index);
            FieldDefinitionDto field = requireField(fieldMap, filter.field());
            String param = "p" + index;

            where.append(AND);
            appendFilter(where, params, field, filter, param);
        }
    }

    @SuppressWarnings("MethodLength")
    private void appendFilter(StringBuilder where,
                              MapSqlParameterSource params,
                              FieldDefinitionDto field,
                              SearchFilterRequest filter,
                              String param) {
        String column = quoteSimpleIdentifier(field.sourceColumn());
        SearchOperator operator = filter.operator();

        switch (operator) {
            case EQUALS -> {
                where.append(column).append(" = :").append(param);
                params.addValue(param, normalizeScalarValue(field, filter.value()));
            }
            case NOT_EQUALS -> {
                where.append(column).append(" <> :").append(param);
                params.addValue(param, normalizeScalarValue(field, filter.value()));
            }
            case CONTAINS -> {
                if (field.arrayField()) {
                    where.append("has(")
                            .append(column)
                            .append(", :")
                            .append(param)
                            .append(")");
                    params.addValue(param, normalizeScalarValue(field, filter.value()));
                } else {
                    where.append("positionCaseInsensitive(toString(")
                            .append(column)
                            .append("), :")
                            .append(param)
                            .append(") > 0");
                    params.addValue(param, String.valueOf(filter.value()));
                }
            }
            case IN -> {
                where.append(column).append(" IN (:").append(param).append(")");
                params.addValue(param, normalizeCollectionValue(field, filter.value()));
            }
            case BETWEEN -> {
                List<?> values = (List<?>) filter.value();

                where.append(column)
                        .append(" BETWEEN :")
                        .append(param)
                        .append("From AND :")
                        .append(param)
                        .append("To");

                params.addValue(param + "From", normalizeScalarValue(field, values.get(0)));
                params.addValue(param + "To", normalizeScalarValue(field, values.get(1)));
            }
            case GREATER_THAN -> {
                where.append(column).append(" > :").append(param);
                params.addValue(param, normalizeScalarValue(field, filter.value()));
            }
            case LESS_THAN -> {
                where.append(column).append(" < :").append(param);
                params.addValue(param, normalizeScalarValue(field, filter.value()));
            }
            default -> throw BadRequestException.of(
                    OPERATOR_NOT_ALLOWED, OPERATOR_NOT_ALLOWED_ERROR_MESSAGE, operator);
        }
    }

    private String buildProjection(List<String> requestedColumns, Map<String, FieldDefinitionDto> fieldMap) {
        List<FieldDefinitionDto> fields = requestedColumns == null || requestedColumns.isEmpty()
                ? defaultProjectionFields(fieldMap)
                : requestedColumns.stream()
                  .map(column -> requireField(fieldMap, column))
                  .toList();

        if (fields.isEmpty()) {
            throw new BadRequestException(FIELD_NOT_ALLOWED, NO_PROJECTION_FIELDS_CONFIGURED_ERROR_MESSAGE);
        }

        return fields.stream()
                .map(field -> quoteSimpleIdentifier(field.sourceColumn())
                        + " AS "
                        + quoteSimpleIdentifier(field.requestField()))
                .collect(Collectors.joining(", "));
    }

    private List<FieldDefinitionDto> defaultProjectionFields(Map<String, FieldDefinitionDto> fieldMap) {
        return fieldMap.values()
                .stream()
                .filter(FieldDefinitionDto::projectionDefault)
                .sorted(Comparator.comparing(field -> field.sortOrder() == null
                        ? Integer.MAX_VALUE
                        : field.sortOrder()))
                .toList();
    }

    private String buildOrderBy(DomainDefinitionDto domain,
                                Map<String, FieldDefinitionDto> fieldMap,
                                SearchQueryRequest request) {
        String sortBy = isBlank(request.sortBy()) ? domain.defaultSortField() : request.sortBy();
        SortDirection direction = request.sortDirection() == null
                ? domain.defaultSortDirection()
                : request.sortDirection();

        if (direction == null) {
            direction = SortDirection.DESC;
        }

        FieldDefinitionDto sortField = requireField(fieldMap, sortBy);

        StringBuilder orderBy = new StringBuilder()
                .append(ORDER_BY)
                .append(quoteSimpleIdentifier(sortField.sourceColumn()))
                .append(" ")
                .append(direction.name());

        appendDefaultTieBreaker(orderBy, fieldMap, sortBy, direction);

        return orderBy.toString();
    }

    private void appendDefaultTieBreaker(StringBuilder orderBy,
                                         Map<String, FieldDefinitionDto> fieldMap,
                                         String sortBy,
                                         SortDirection direction) {
        if (DEFAULT_TIE_BREAKER_FIELD.equals(sortBy)) {
            return;
        }

        FieldDefinitionDto idField = fieldMap.get(DEFAULT_TIE_BREAKER_FIELD);

        if (idField == null) {
            return;
        }

        orderBy.append(", ")
                .append(quoteSimpleIdentifier(idField.sourceColumn()))
                .append(" ")
                .append(direction.name());
    }

    private String buildPagination(MapSqlParameterSource params, SearchQueryRequest request) {
        int page = resolvePage(request.page());
        int size = resolveSize(request.size());
        long offset = (long) page * size;

        if (offset > MAX_OFFSET) {
            throw BadRequestException.of(INVALID_FILTER_VALUE, DEEP_PAGINATION_NOT_ALLOWED_ERROR_MESSAGE, MAX_OFFSET);
        }

        params.addValue(PARAM_LIMIT, size);
        params.addValue(PARAM_OFFSET, offset);

        return LIMIT_OFFSET;
    }

    private Object normalizeScalarValue(FieldDefinitionDto field, Object value) {
        return switch (field.type()) {
            case TEXT, SELECT -> String.valueOf(value);
            case NUMBER -> normalizeNumber(value);
            case DATE -> LocalDate.parse(String.valueOf(value));
            case DATE_TIME -> normalizeDateTime(value);
            case BOOLEAN -> normalizeBoolean(value);
        };
    }

    private List<Object> normalizeCollectionValue(FieldDefinitionDto field, Object value) {
        Collection<?> values = (Collection<?>) value;

        return values.stream()
                .map(item -> normalizeScalarValue(field, item))
                .toList();
    }

    private BigDecimal normalizeNumber(Object value) {
        if (value instanceof BigDecimal decimal) {
            return decimal;
        }

        if (value instanceof Number number) {
            return new BigDecimal(number.toString());
        }

        return new BigDecimal(String.valueOf(value));
    }

    private LocalDateTime normalizeDateTime(Object value) {
        String text = String.valueOf(value);

        try {
            return OffsetDateTime.parse(text)
                    .withOffsetSameInstant(ZoneOffset.UTC)
                    .toLocalDateTime();
        } catch (Exception ignored) {
            return LocalDateTime.parse(text);
        }
    }

    private Boolean normalizeBoolean(Object value) {
        if (value instanceof Boolean bool) {
            return bool;
        }

        return Boolean.parseBoolean(String.valueOf(value));
    }

    private FieldDefinitionDto requireField(Map<String, FieldDefinitionDto> fieldMap, String requestField) {
        FieldDefinitionDto field = fieldMap.get(requestField);

        if (field == null) {
            throw BadRequestException.of(FIELD_NOT_ALLOWED, FIELD_NOT_CONFIGURED_ERROR_MESSAGE, requestField);
        }

        return field;
    }

    private String quoteQualifiedIdentifier(String value) {
        if (isBlank(value)) {
            throw BadRequestException.of(INVALID_SQL_IDENTIFIER, INVALID_SQL_IDENTIFIER_ERROR_MESSAGE, value);
        }

        String[] parts = value.split("\\.", -1);

        if (parts.length > 2) {
            throw BadRequestException.of(INVALID_SQL_IDENTIFIER, INVALID_SQL_IDENTIFIER_ERROR_MESSAGE, value);
        }

        return Arrays.stream(parts)
                .map(this::quoteSimpleIdentifier)
                .collect(Collectors.joining("."));
    }

    private String quoteSimpleIdentifier(String value) {
        if (isBlank(value)
                || !SearchConstants.Validation.SIMPLE_SQL_IDENTIFIER_PATTERN.matcher(value).matches()) {
            throw BadRequestException.of(INVALID_SQL_IDENTIFIER, INVALID_SQL_IDENTIFIER_ERROR_MESSAGE, value);
        }

        return "`" + value + "`";
    }

    private int resolvePage(Integer page) {
        return page == null ? DEFAULT_PAGE : Math.max(page, DEFAULT_PAGE);
    }

    private int resolveSize(Integer size) {
        return size == null ? DEFAULT_SIZE : Math.min(size, MAX_SIZE);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
