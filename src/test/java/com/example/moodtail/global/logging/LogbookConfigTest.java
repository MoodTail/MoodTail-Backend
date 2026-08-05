package com.example.moodtail.global.logging;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.zalando.logbook.CorrelationId;
import org.zalando.logbook.HttpHeaders;
import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.RequestFilter;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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

    @Test
    void omitsAuthenticationRequestBody() throws Exception {
        RequestFilter requestFilter = new LogbookConfig().requestFilter();
        HttpRequest request = request("/api/v1/auth/login/local", "application/json", "secret-body");

        HttpRequest filtered = requestFilter.filter(request);

        assertThat(filtered.getBodyAsString()).isEqualTo("<omitted>");
    }

    @Test
    void omitsInquiryRequestBody() throws Exception {
        RequestFilter requestFilter = new LogbookConfig().requestFilter();
        HttpRequest request = request("/api/v1/inquiries", "application/json", "private-inquiry");

        HttpRequest filtered = requestFilter.filter(request);

        assertThat(filtered.getBodyAsString()).isEqualTo("<omitted>");
    }

    @Test
    void preservesOrdinaryJsonRequestBody() throws Exception {
        RequestFilter requestFilter = new LogbookConfig().requestFilter();
        HttpRequest request = request("/api/v1/tests/results", "application/json", "{\"answer\":1}");

        HttpRequest filtered = requestFilter.filter(request);

        assertThat(filtered.getBodyAsString()).isEqualTo("{\"answer\":1}");
    }

    @Test
    void omitsMultipartBodyByDefault() throws Exception {
        RequestFilter requestFilter = new LogbookConfig().requestFilter();
        HttpRequest request = request(
                "/api/v1/tests/results/share",
                "multipart/form-data; boundary=boundary",
                "binary-thumbnail"
        );

        HttpRequest filtered = requestFilter.filter(request);

        assertThat(filtered.getBodyAsString()).doesNotContain("binary-thumbnail");
    }

    private HttpRequest request(String path, String contentType, String body) throws Exception {
        HttpRequest request = mock(HttpRequest.class);
        when(request.getPath()).thenReturn(path);
        when(request.getHeaders()).thenReturn(HttpHeaders.of("Content-Type", contentType));
        when(request.getBody()).thenReturn(body.getBytes(StandardCharsets.UTF_8));
        when(request.getContentType()).thenCallRealMethod();
        when(request.getBodyAsString()).thenCallRealMethod();
        when(request.getCharset()).thenCallRealMethod();
        return request;
    }
}
