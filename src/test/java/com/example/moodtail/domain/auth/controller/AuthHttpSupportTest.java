package com.example.moodtail.domain.auth.controller;

import com.example.moodtail.global.common.exception.RestApiException;
import com.example.moodtail.support.auth.AuthPropertiesFixtures;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuthHttpSupportTest {

    private AuthHttpSupport support;

    @BeforeEach
    void setUp() {
        support = new AuthHttpSupport(
                AuthPropertiesFixtures.defaults(),
                List.of("https://app.moodtail.example")
        );
        ReflectionTestUtils.setField(support, "refreshExpirationMillis", 1_209_600_000L);
    }

    @Test
    void usesRemoteAddressWithoutTrustingForwardedHeaders() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("10.0.0.7");
        request.addHeader("X-Forwarded-For", "203.0.113.9");

        assertThat(support.clientAddress(request)).isEqualTo("10.0.0.7");
    }

    @Test
    void extractsBearerAndRefreshCookieValues() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer access-token");
        request.setCookies(new Cookie("refreshToken", "refresh-token"));

        assertThat(support.resolveAccessToken(request)).isEqualTo("access-token");
        assertThat(support.resolveRefreshToken(request)).isEqualTo("refresh-token");
    }

    @Test
    void acceptsConfiguredOriginForCookieAuthenticatedRequest() {
        MockHttpServletRequest request = cookieAuthenticatedRequest();
        request.addHeader(HttpHeaders.ORIGIN, "https://app.moodtail.example");

        support.validateCookieAuthenticatedRequest(request);
    }

    @Test
    void acceptsNativeClientRequestWithoutBrowserOriginMetadata() {
        MockHttpServletRequest request = cookieAuthenticatedRequest();

        support.validateCookieAuthenticatedRequest(request);
    }

    @Test
    void rejectsExplicitlyUntrustedBrowserOrigin() {
        MockHttpServletRequest request = cookieAuthenticatedRequest();
        request.addHeader(HttpHeaders.ORIGIN, "https://attacker.example");

        assertThatThrownBy(() -> support.validateCookieAuthenticatedRequest(request))
                .isInstanceOfSatisfying(RestApiException.class, exception ->
                        assertThat(exception.getErrorCode().getCode()).isEqualTo("AUTH033")
                );
    }

    @Test
    void setsAndClearsOnlyTheAuthScopedRefreshCookie() {
        MockHttpServletResponse response = new MockHttpServletResponse();

        support.setRefreshTokenCookie(response, "refresh-token");
        support.clearRefreshTokenCookie(response);

        assertThat(response.getHeaders(HttpHeaders.SET_COOKIE))
                .hasSize(2)
                .allSatisfy(cookie -> {
                    assertThat(cookie).contains("Path=/api/v1/auth");
                    assertThat(cookie).contains("HttpOnly");
                });
        assertThat(response.getHeaders(HttpHeaders.SET_COOKIE).get(0))
                .contains("refreshToken=refresh-token");
        assertThat(response.getHeaders(HttpHeaders.SET_COOKIE).get(1))
                .contains("refreshToken=")
                .contains("Max-Age=0");
    }

    private MockHttpServletRequest cookieAuthenticatedRequest() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie("refreshToken", "refresh-token"));
        return request;
    }
}
