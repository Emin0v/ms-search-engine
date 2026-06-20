package com.company.searchengine.exception.model.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ErrorMessage {

    // CHECKSTYLE:OFF
    // @formatter:off


    public static final String REQUIRED_NULL_ERROR_MESSAGE = "{0} must be null";
    public static final String REQUIRED_NON_NULL_ERROR_MESSAGE = "{0} must not be null";
    public static final String DATA_NOT_FOUND_ERROR_MESSAGE = "Data not found with {0} - {1}";
    public static final String INTERNAL_SERVER_ERROR_MESSAGE = "Unexpected internal server error occurs";
    public static final String INVALID_VALUE_ERROR_MESSAGE = "Invalid value for field: {0}";
    public static final String CONSTRAINT_VIOLATION_ERROR_MESSAGE = "Constraint violation error occurred.";
    public static final String UNSUPPORTED_SEARCH_DOMAIN_ERROR_MESSAGE = "Unsupported search domain: {0}";
    public static final String COLUMN_IS_NOT_ALLOWED_ERROR_MESSAGE = "Column is not allowed: {0}";
    public static final String INVALID_SQL_IDENTIFIER_ERROR_MESSAGE = "Invalid SQL identifier in registry: {0}";
    public static final String NO_FIELDS_CONFIGURED_ERROR_MESSAGE = "No fields configured for domain";
    public static final String FILTERABLE_FIELD_HAS_NO_OPERATORS_ERROR_MESSAGE = "Filterable field has no allowed operators: {0}";
    public static final String FILTER_FIELD_NOT_ALLOWED_ERROR_MESSAGE = "Filter field is not allowed: {0}";
    public static final String OPERATOR_NOT_ALLOWED_ERROR_MESSAGE = "Operator {0} is not allowed.";
    public static final String OPERATOR_NOT_ALLOWED_FOR_FIELD_ERROR_MESSAGE = "Operator {0} is not allowed for field {1}";
    public static final String DEFAULT_SORT_FIELD_NOT_CONFIGURED_ERROR_MESSAGE = "Default sort field is not configured";
    public static final String SORT_FIELD_NOT_ALLOWED_ERROR_MESSAGE = "Sort field is not allowed: {0}";
    public static final String ONLY_CONTAINS_SUPPORTED_FOR_ARRAY_ERROR_MESSAGE = "Only CONTAINS operator is supported for array fields";
    public static final String RANGE_OPERATOR_TYPE_ERROR_MESSAGE = "{0} operator is allowed only for NUMBER, DATE or DATE_TIME fields";
    public static final String CONTAINS_OPERATOR_TYPE_ERROR_MESSAGE = "CONTAINS operator is allowed only for TEXT, SELECT or ARRAY fields";
    public static final String TENANT_VALUE_REQUIRED_ERROR_MESSAGE = "Tenant value is required for domain: {0}";
    public static final String NO_PROJECTION_FIELDS_CONFIGURED_ERROR_MESSAGE = "No projection fields configured. At least one projection_default field is required";
    public static final String DEEP_PAGINATION_NOT_ALLOWED_ERROR_MESSAGE = "Deep pagination is not allowed. Maximum offset is {0}";
    public static final String FIELD_NOT_CONFIGURED_ERROR_MESSAGE = "Field is not configured: {0}";
    public static final String DUPLICATE_ACTIVE_SEARCH_FIELD_ERROR_MESSAGE = "Duplicate active search field configured: {0}";
    public static final String CLICKHOUSE_QUERY_FAILED_ERROR_MESSAGE = "ClickHouse query failed";

    // @formatter:on
    // CHECKSTYLE:ON
}
