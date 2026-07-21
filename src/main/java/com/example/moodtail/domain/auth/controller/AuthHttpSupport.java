package com.example.moodtail.domain.auth.controller;

import com.example.moodtail.global.auth.config.AuthProperties;
import com.example.moodtail.global.common.exception.RestApiException;
import com.example.moodtail.global.common.exception.code.status.AuthErrorStatus;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class AuthHttpSupport {

    private static final int MAX_CLIENT_ADDRESS_LENGTH = 128;
    private static final String REFRESH_COOKIE_PATH = "/api/v1/auth";

    private final AuthProperties authProperties;
    private final Set<String> allowedOrigins;

    @Value("${jwt.refreshExpiration}")
    private long refreshExpirationMillis;

    public AuthHttpSupport(
            AuthProperties authProperties,
            @Value("${cors.allowed-origins}") List<String> allowedOrigins
    ) {
        this.authProperties = authProperties;
        this.allowedOrigins = new HashSet<>(allowedOrigins);
    }

    public String clientAddress(HttpServletRequest request) {
        return resolveClientAddress(request);
    }

    public String resolveAccessToken(HttpServletRequest request) {
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(authorization) && authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }
        return null;
    }

    public String resolveRefreshToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        return Arrays.stream(cookies)
                .filter(cookie -> authProperties.refreshCookie().name().equals(cookie.getName()))
                .map(Cookie::getValue)
                .filter(StringUtils::hasText)
                .findFirst()
                .orElse(null);
    }

    public void validateCookieAuthenticatedRequest(HttpServletRequest request) {
        if (!StringUtils.hasText(resolveRefreshToken(request))) {
            return;
        }

        String origin = request.getHeader("Origin");
        if (!StringUtils.hasText(origin)) {
            return;
        }
        if ("null".equalsIgnoreCase(origin)
                || (!allowedOrigins.contains(origin) && !origin.equals(resolveRequestOrigin(request)))) {
            throw new RestApiException(AuthErrorStatus.UNTRUSTED_AUTH_ORIGIN);
        }
    }

    public void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        response.addHeader(
                HttpHeaders.SET_COOKIE,
                createRefreshTokenCookie(
                        refreshToken,
                        Duration.ofMillis(refreshExpirationMillis)
                ).toString()
        );
    }

    public void clearRefreshTokenCookie(HttpServletResponse response) {
        response.addHeader(
                HttpHeaders.SET_COOKIE,
                createRefreshTokenCookie("", Duration.ZERO).toString()
        );
    }

    private String resolveClientAddress(HttpServletRequest request) {
        String remoteAddress = request.getRemoteAddr();
        if (!StringUtils.hasText(remoteAddress) || remoteAddress.length() > MAX_CLIENT_ADDRESS_LENGTH) {
            return "unknown";
        }
        return remoteAddress;
    }

    private String resolveRequestOrigin(HttpServletRequest request) {
        String scheme = request.getScheme();
        int port = request.getServerPort();
        boolean defaultPort = ("http".equalsIgnoreCase(scheme) && port == 80)
                || ("https".equalsIgnoreCase(scheme) && port == 443);
        return scheme + "://" + request.getServerName() + (defaultPort ? "" : ":" + port);
    }

    private ResponseCookie createRefreshTokenCookie(String value, Duration maxAge) {
        AuthProperties.RefreshCookie properties = authProperties.refreshCookie();
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(properties.name(), value)
                .httpOnly(true)
                .secure(properties.secure())
                .sameSite(properties.sameSite())
                .path(REFRESH_COOKIE_PATH)
                .maxAge(maxAge);
        if (StringUtils.hasText(properties.domain())) {
            builder.domain(properties.domain());
        }
        return builder.build();
    }
}
