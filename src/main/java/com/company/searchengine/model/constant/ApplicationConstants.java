package com.company.searchengine.model.constant;

import static lombok.AccessLevel.PRIVATE;

import java.util.List;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = PRIVATE)
public final class ApplicationConstants {

    @NoArgsConstructor(access = PRIVATE)
    public static final class HttpAttribute {
        public static final String REQUEST_START_TIME = "request_start_time";
        public static final String BEARER = "Bearer ";
        public static final String BASIC = "Basic ";
        public static final String CONTENT_TYPE = "Content-Type";
        public static final String ACCEPT = "Accept";
        public static final String DEFAULT_LANGUAGE = "en";
        public static final String ELAPSED_TIME = "elapsed_time";
    }

    @NoArgsConstructor(access = PRIVATE)
    public static final class HttpHeader {
        public static final String X_FORWARDED_FOR = "X-Forwarded-For";
        public static final String X_REAL_IP = "X-Real-IP";
        public static final String USER_AGENT = "User-Agent";
        public static final String AUTHORIZATION = "Authorization";
        public static final String SUBDOMAIN_ID = "subdomain-id";

        public static final String AC_REQUEST_ID = "ac-request-id";
        public static final String AC_USER_ID = "ac-user-id";
        public static final String AC_BP_ID = "ac-bp-id";
        public static final String AC_EMPLOYEE_ID = "ac-employee-id";
        public static final String AC_USER_TYPE = "ac-user-type";
        public static final String AC_USER_ROLE = "ac-user-role";
        public static final String AC_EMAIL = "ac-email";
        public static final String AC_USERNAME = "ac-username";
        public static final String AC_DEVICE_ID = "ac-device-id";
        public static final String AC_DEVICE_MODEL = "ac-device-model";
        public static final String AC_APP_VERSION = "ac-app-version";
        public static final String AC_ORIGIN_SERVICE = "ac-origin-service";
        public static final String AC_USER_AGENT = "ac-user-agent";
        public static final String AC_CLIENT_IP = "ac-client-ip";
        public static final String AC_LANGUAGE = "ac-lang";
        public static final String AC_ORGANIZATION_ID = "ac-organization-id";
    }

    @NoArgsConstructor(access = PRIVATE)
    public static class MdcFields {
        public static final String AC_EVENT_ID = "ac-event-id";
        public static final String AC_EVENT_TYPE = "ac-event-type";
        public static final String AC_URI = "ac-uri";
        public static final String AC_ELAPSED_TIME = "ac-elapsed-time";
        public static final String AC_ERROR_HTTP_STATUS = "ac-error-http-status";
        public static final String AC_ERROR_CODE = "ac-error-code";
        public static final String AC_ERROR_TYPE = "ac-error-type";
        public static final String AC_HTTP_MESSAGE_TYPE = "ac-http-message-type";
    }

    @NoArgsConstructor(access = PRIVATE)
    public static class SecretName {
        public static final String JAEGER_URL = "JAEGER_URL";
    }

    @NoArgsConstructor(access = PRIVATE)
    public static class Tracing {
        public static final List<String> DEFAULT_EXCLUDED_JAEGER_PATTERNS =
                List.of("/actuator**/**", "/swagger**/**", "/**/api-docs**/**");
    }

    @NoArgsConstructor(access = PRIVATE)
    public static final class Encoding {
        public static final String BASE64_DATA_FORMAT = "data:%s;base64,%s";
    }

}
