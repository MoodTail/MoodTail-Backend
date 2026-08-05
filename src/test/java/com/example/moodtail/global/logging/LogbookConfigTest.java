package com.example.moodtail.global.logging;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.zalando.logbook.CorrelationId;
import org.zalando.logbook.HttpRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class LogbookConfigTest {

    private final CorrelationId correlationId = new LogbookConfig().correlationId();

    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    @Test
    void usesRequestIdFromMdc() {
        MDC.put(RequestMdcFilter.REQUEST_ID_MDC_KEY, "request-123");

        String result = correlationId.generate(mock(HttpRequest.class));

        assertThat(result).isEqualTo("request-123");
    }

    @Test
    void generatesFallbackIdOutsideRequestFilter() {
        String result = correlationId.generate(mock(HttpRequest.class));

        assertThat(result)
                .matches("^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$");
    }
}
