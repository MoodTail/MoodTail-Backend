package com.example.moodtail.global.logging;

import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.zalando.logbook.CorrelationId;
import org.zalando.logbook.RequestFilter;
import org.zalando.logbook.core.RequestFilters;

import java.util.UUID;

@Configuration
public class LogbookConfig {

    private static final String OMITTED_BODY = "<omitted>";

    @Bean
    public CorrelationId correlationId() {
        return request -> {
            String requestId = MDC.get(RequestMdcFilter.REQUEST_ID_MDC_KEY);
            return StringUtils.hasText(requestId) ? requestId : UUID.randomUUID().toString();
        };
    }

    @Bean
    public RequestFilter requestFilter() {
        RequestFilter sensitiveBodyFilter = RequestFilters.replaceBody(request ->
                isSensitiveRequest(request.getPath()) ? OMITTED_BODY : null
        );
        return RequestFilter.merge(RequestFilters.defaultValue(), sensitiveBodyFilter);
    }

    private boolean isSensitiveRequest(String path) {
        return path != null
                && (path.equals("/api/v1/auth")
                || path.startsWith("/api/v1/auth/")
                || path.equals("/api/v1/inquiries"));
    }
}
