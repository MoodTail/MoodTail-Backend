package com.example.moodtail.domain.user.service;

import com.example.moodtail.global.common.exception.RestApiException;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuthRequestOriginValidatorTest {

    private final AuthRequestOriginValidator validator = new AuthRequestOriginValidator(
            List.of("https://app.moodtail.example"),
            "refreshToken"
    );

    @Test
    void allowsConfiguredFrontendOriginForCookieAuthenticatedRequest() {
        MockHttpServletRequest request = cookieRequest();
        request.addHeader("Origin", "https://app.moodtail.example");
        request.addHeader("Sec-Fetch-Site", "cross-site");

        assertThatCode(() -> validator.validateCookieAuthenticatedRequest(request)).doesNotThrowAnyException();
    }

    @Test
    void rejectsCrossSiteRefreshCookieRequest() {
        MockHttpServletRequest request = cookieRequest();
        request.addHeader("Origin", "https://attacker.example");
        request.addHeader("Sec-Fetch-Site", "cross-site");

        assertThatThrownBy(() -> validator.validateCookieAuthenticatedRequest(request))
                .isInstanceOfSatisfying(RestApiException.class, exception ->
                        assertThat(exception.getErrorCode().getCode()).isEqualTo("AUTH033")
                );
    }

    @Test
    void rejectsCookieRequestWhenBrowserOriginMetadataIsMissing() {
        MockHttpServletRequest request = cookieRequest();

        assertThatThrownBy(() -> validator.validateCookieAuthenticatedRequest(request))
                .isInstanceOfSatisfying(RestApiException.class, exception ->
                        assertThat(exception.getErrorCode().getCode()).isEqualTo("AUTH033")
                );
    }

    @Test
    void allowsSameOriginFetchMetadataWhenOriginHeaderIsMissing() {
        MockHttpServletRequest request = cookieRequest();
        request.addHeader("Sec-Fetch-Site", "same-origin");

        assertThatCode(() -> validator.validateCookieAuthenticatedRequest(request)).doesNotThrowAnyException();
    }

    @Test
    void sameSiteMetadataAloneDoesNotBypassOriginValidation() {
        MockHttpServletRequest request = cookieRequest();
        request.addHeader("Sec-Fetch-Site", "same-site");

        assertThatThrownBy(() -> validator.validateCookieAuthenticatedRequest(request))
                .isInstanceOf(RestApiException.class);
    }

    @Test
    void ignoresOriginForBearerOnlyRequestWithoutRefreshCookie() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Origin", "https://attacker.example");
        request.addHeader("Sec-Fetch-Site", "cross-site");

        assertThatCode(() -> validator.validateCookieAuthenticatedRequest(request)).doesNotThrowAnyException();
    }

    private MockHttpServletRequest cookieRequest() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie("refreshToken", "refresh-token"));
        return request;
    }
}
