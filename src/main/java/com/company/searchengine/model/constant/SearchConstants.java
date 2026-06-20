package com.company.searchengine.model.constant;

import java.util.regex.Pattern;

public final class SearchConstants {

    private SearchConstants() {
    }

    public static final class Pagination {
        public static final int DEFAULT_PAGE = 0;
        public static final int DEFAULT_SIZE = 20;
        public static final int MAX_SIZE = 200;
        public static final long MAX_OFFSET = 10_000L;

        private Pagination() {
        }
    }

    public static final class Validation {
        public static final int MAX_FILTERS = 20;
        public static final int MAX_COLUMNS = 80;
        public static final int MAX_IN_VALUES = 200;
        public static final int MAX_TEXT_VALUE_LENGTH = 512;

        public static final Pattern SIMPLE_SQL_IDENTIFIER_PATTERN =
                Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_]*$");

        private Validation() {
        }
    }

    public static final class Sql {
        public static final String SELECT = "SELECT ";
        public static final String SELECT_COUNT = "SELECT count() ";
        public static final String FROM = " FROM ";
        public static final String WHERE_TRUE = " WHERE 1 = 1";
        public static final String AND = " AND ";
        public static final String ORDER_BY = " ORDER BY ";
        public static final String LIMIT_OFFSET = " LIMIT :limit OFFSET :offset";

        public static final String PARAM_LIMIT = "limit";
        public static final String PARAM_OFFSET = "offset";
        public static final String PARAM_TENANT_VALUE = "tenantValue";

        public static final String DEFAULT_TIE_BREAKER_FIELD = "id";

        private Sql() {
        }
    }
}
