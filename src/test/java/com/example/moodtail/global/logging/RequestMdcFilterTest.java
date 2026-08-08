package com.example.moodtail.global.logging;

import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RequestMdcFilterTest {

    private final RequestMdcFilter filter = new RequestMdcFilter();

    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    @Test
    void reusesSafeRequestIdAndExposesItToResponseAndMdc() throws Exception {
        MockHttpServletRequest request = request();
        request.addHeader(RequestMdcFilter.REQUEST_ID_HEADER, "frontend-request-123");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<String> requestIdInChain = new AtomicReference<>();

        filter.doFilter(request, response, (ignoredRequest, ignoredResponse) ->
                requestIdInChain.set(MDC.get(RequestMdcFilter.REQUEST_ID_MDC_KEY))
        );

        assertThat(requestIdInChain.get()).isEqualTo("frontend-request-123");
        assertThat(response.getHeader(RequestMdcFilter.REQUEST_ID_HEADER)).isEqualTo("frontend-request-123");
        assertMdcCleared();
    }

    @Test
    void replacesUnsafeRequestIdWithServerGeneratedUuid() throws Exception {
        MockHttpServletRequest request = request();
        request.addHeader(RequestMdcFilter.REQUEST_ID_HEADER, "unsafe request id");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<String> requestIdInChain = new AtomicReference<>();

        filter.doFilter(request, response, (ignoredRequest, ignoredResponse) ->
                requestIdInChain.set(MDC.get(RequestMdcFilter.REQUEST_ID_MDC_KEY))
        );

        assertThat(requestIdInChain.get())
                .matches("^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$");
        assertThat(response.getHeader(RequestMdcFilter.REQUEST_ID_HEADER)).isEqualTo(requestIdInChain.get());
        assertMdcCleared();
    }

    @Test
    void clearsMdcWhenDownstreamFilterFails() {
        MockHttpServletRequest request = request();
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertThatThrownBy(() -> filter.doFilter(request, response, (ignoredRequest, ignoredResponse) -> {
            throw new ServletException("failure");
        })).isInstanceOf(ServletException.class);

        assertMdcCleared();
    }

    private MockHttpServletRequest request() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/cocktails");
        request.setRequestURI("/api/v1/cocktails");
        return request;
    }

    private void assertMdcCleared() {
        assertThat(MDC.get(RequestMdcFilter.REQUEST_ID_MDC_KEY)).isNull();
        assertThat(MDC.get(RequestMdcFilter.REQUEST_METHOD_MDC_KEY)).isNull();
        assertThat(MDC.get(RequestMdcFilter.REQUEST_URI_MDC_KEY)).isNull();
    }
}
