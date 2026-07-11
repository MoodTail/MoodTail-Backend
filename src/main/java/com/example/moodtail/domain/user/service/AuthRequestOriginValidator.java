package com.example.moodtail.domain.user.service;

import com.example.moodtail.global.common.exception.RestApiException;
import com.example.moodtail.global.common.exception.code.status.AuthErrorStatus;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Component
public class AuthRequestOriginValidator {

    private final Set<String> allowedOrigins;
    private final String refreshCookieName;

    public AuthRequestOriginValidator(
            @Value("${cors.allowed-origins}") List<String> allowedOrigins,
            @Value("${auth.refresh-cookie.name}") String refreshCookieName
    ) {
        this.allowedOrigins = new HashSet<>(allowedOrigins);
        this.refreshCookieName = refreshCookieName;
    }

    public void validateCookieAuthenticatedRequest(HttpServletRequest request) {
        if (!hasRefreshCookie(request)) {
            return;
        }

        String origin = request.getHeader("Origin");
        if (StringUtils.hasText(origin)) {
            if ("null".equalsIgnoreCase(origin)
                    || (!allowedOrigins.contains(origin) && !origin.equals(resolveRequestOrigin(request)))) {
                throw new RestApiException(AuthErrorStatus.UNTRUSTED_AUTH_ORIGIN);
            }
            return;
        }

        String fetchSite = request.getHeader("Sec-Fetch-Site");
        if (StringUtils.hasText(fetchSite) && "cross-site".equals(fetchSite.toLowerCase(Locale.ROOT))) {
            throw new RestApiException(AuthErrorStatus.UNTRUSTED_AUTH_ORIGIN);
        }
    }

    private boolean hasRefreshCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        return cookies != null && Arrays.stream(cookies)
                .anyMatch(cookie -> refreshCookieName.equals(cookie.getName())
                        && StringUtils.hasText(cookie.getValue()));
    }

    private String resolveRequestOrigin(HttpServletRequest request) {
        String scheme = request.getScheme();
        int port = request.getServerPort();
        boolean defaultPort = ("http".equalsIgnoreCase(scheme) && port == 80)
                || ("https".equalsIgnoreCase(scheme) && port == 443);
        return scheme + "://" + request.getServerName() + (defaultPort ? "" : ":" + port);
    }
}
