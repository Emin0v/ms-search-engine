package com.company.searchengine.exception.model.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ErrorCode {

    public static final String CONFLICT = "conflict";
    public static final String BAD_REQUEST = "bad_request";
    public static final String FORBIDDEN = "forbidden";
    public static final String UNAUTHORIZED = "unauthorized";
    public static final String ALREADY_EXIST = "already_exist";
    public static final String DATA_NOT_FOUND = "data_not_found";
    public static final String RESOURCE_MISSING = "resource_missing";
    public static final String PARAMETER_INVALID = "parameter_invalid";
    public static final String REQUEST_INVALID = "request_body_invalid";
    public static final String INTERNAL_SERVER = "unexpected_internal_error";
    public static final String DOMAIN_NOT_FOUND = "domain_not_found";
    public static final String FIELD_NOT_ALLOWED = "field_not_allowed";
    public static final String INVALID_SQL_IDENTIFIER = "invalid_sql_identifier";
    public static final String CLICKHOUSE_QUERY_FAILED = "clickhouse_query_failed";
    public static final String INVALID_FILTER_VALUE = "invalid_filter_value";
    public static final String OPERATOR_NOT_ALLOWED = "operator_not_allowed";
    public static final String SORT_FIELD_NOT_ALLOWED = "sort_field_not_allowed";
    public static final String CONFIGURATION_ERROR = "configuration_error";

}
