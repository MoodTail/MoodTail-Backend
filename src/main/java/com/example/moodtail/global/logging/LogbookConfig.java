package com.example.moodtail.global.logging;

import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.zalando.logbook.CorrelationId;
import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.RequestFilter;
import org.zalando.logbook.core.Conditions;
import org.zalando.logbook.core.RequestFilters;

import java.util.UUID;
import java.util.function.Predicate;

@Configuration
public class LogbookConfig {

    private static final String OMITTED_BODY = "<omitted>";
    private static final Predicate<HttpRequest> BINARY_BODY = Conditions.contentType(
            "application/octet-stream",
            "application/pdf",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-powerpoint",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation",
            "audio/*",
            "image/*",
            "video/*"
    );
    private static final Predicate<HttpRequest> MULTIPART_BODY = Conditions.contentType("multipart/*");
    private static final Predicate<HttpRequest> STREAM_BODY = Conditions.contentType(
            "application/json-seq",
            "application/x-json-stream",
            "application/stream+json",
            "text/event-stream",
            "application/x-ndjson"
    );

    @Bean
    public CorrelationId correlationId() {
        return request -> {
            String requestId = MDC.get(RequestMdcFilter.REQUEST_ID_MDC_KEY);
            return StringUtils.hasText(requestId) ? requestId : UUID.randomUUID().toString();
        };
    }

    @Bean
    public RequestFilter requestFilter() {
        RequestFilter jsonSafeBodyFilter = RequestFilters.replaceBody(request -> {
            if (isSensitiveRequest(request.getPath())) {
                return asJsonString(OMITTED_BODY);
            }
            if (BINARY_BODY.test(request)) {
                return asJsonString("<binary>");
            }
            if (MULTIPART_BODY.test(request)) {
                return asJsonString("<multipart>");
            }
            if (STREAM_BODY.test(request)) {
                return asJsonString("<stream>");
            }
            return null;
        });
        return jsonSafeBodyFilter;
    }

    private String asJsonString(String value) {
        return "\"" + value + "\"";
    }

    private boolean isSensitiveRequest(String path) {
        return path != null
                && (path.equals("/api/v1/auth")
                || path.startsWith("/api/v1/auth/")
                || path.equals("/api/v1/inquiries"));
    }
}
