package com.example.moodtail.global.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

@ConfigurationProperties(prefix = "auth")
public record AuthProperties(
        OAuth oauth,
        RefreshCookie refreshCookie,
        GuestLogin guestLogin,
        RedisKeys redis
) {

    public AuthProperties {
        if (oauth == null || refreshCookie == null || guestLogin == null || redis == null) {
            throw new IllegalArgumentException("All auth configuration groups are required");
        }
    }

    public record OAuth(
            long stateExpirationMillis,
            long signupTokenExpirationMillis,
            long connectTimeoutMillis,
            long readTimeoutMillis,
            RateLimit stateRateLimit,
            Provider kakao,
            Provider google
    ) {
        public OAuth {
            requirePositive(stateExpirationMillis, "auth.oauth.state-expiration-millis");
            requirePositive(signupTokenExpirationMillis, "auth.oauth.signup-token-expiration-millis");
            requirePositiveIntRange(connectTimeoutMillis, "auth.oauth.connect-timeout-millis");
            requirePositiveIntRange(readTimeoutMillis, "auth.oauth.read-timeout-millis");
            if (stateRateLimit == null || kakao == null || google == null) {
                throw new IllegalArgumentException("OAuth provider configuration is required");
            }
        }
    }

    public record Provider(
            boolean enabled,
            String clientId,
            String clientSecret,
            String redirectUri,
            String tokenUri,
            String userInfoUri
    ) {
        public Provider {
            requireText(tokenUri, "OAuth token URI");
            requireText(userInfoUri, "OAuth user-info URI");
            requireAbsoluteHttpsUri(tokenUri, "OAuth token URI");
            requireAbsoluteHttpsUri(userInfoUri, "OAuth user-info URI");
            if (StringUtils.hasText(redirectUri)) {
                requireAbsoluteHttpUri(redirectUri, "OAuth redirect URI");
            }
            if (enabled) {
                requireText(clientId, "Enabled OAuth provider client ID");
                requireText(redirectUri, "Enabled OAuth provider redirect URI");
            }
        }
    }

    public record RefreshCookie(
            String name,
            String domain,
            boolean secure,
            String sameSite
    ) {
        public RefreshCookie {
            requireText(name, "Refresh-token cookie name");
            requireText(sameSite, "Refresh-token cookie SameSite");
            if (!sameSite.equalsIgnoreCase("Lax")
                    && !sameSite.equalsIgnoreCase("Strict")
                    && !sameSite.equalsIgnoreCase("None")) {
                throw new IllegalArgumentException("Refresh-token cookie SameSite must be Lax, Strict, or None");
            }
            if (sameSite.equalsIgnoreCase("None") && !secure) {
                throw new IllegalArgumentException("SameSite=None requires a secure refresh-token cookie");
            }
        }
    }

    public record GuestLogin(
            String defaultNickname,
            RateLimit uuidRateLimit,
            RateLimit clientRateLimit
    ) {
        public GuestLogin {
            requireText(defaultNickname, "Guest default nickname");
            if (defaultNickname.codePointCount(0, defaultNickname.length()) > 50) {
                throw new IllegalArgumentException("Guest default nickname must be at most 50 characters");
            }
            if (uuidRateLimit == null || clientRateLimit == null) {
                throw new IllegalArgumentException("Guest-login rate-limit configuration is required");
            }
        }
    }

    public record RateLimit(
            int maxAttempts,
            long windowMillis
    ) {
        public RateLimit {
            if (maxAttempts < 1) {
                throw new IllegalArgumentException("Rate-limit max attempts must be positive");
            }
            requirePositive(windowMillis, "Rate-limit window");
        }
    }

    public record RedisKeys(String keyPrefix) {
        public RedisKeys {
            requireText(keyPrefix, "Auth Redis key prefix");
            if (keyPrefix.length() > 100 || keyPrefix.chars().anyMatch(Character::isWhitespace)) {
                throw new IllegalArgumentException("Auth Redis key prefix must be at most 100 non-space characters");
            }
        }
    }

    private static void requireText(String value, String name) {
        if (!StringUtils.hasText(value)) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
    }

    private static void requirePositive(long value, String name) {
        if (value < 1) {
            throw new IllegalArgumentException(name + " must be positive");
        }
    }

    private static void requirePositiveIntRange(long value, String name) {
        requirePositive(value, name);
        if (value > Integer.MAX_VALUE) {
            throw new IllegalArgumentException(name + " must be less than or equal to " + Integer.MAX_VALUE);
        }
    }

    private static void requireAbsoluteHttpUri(String value, String name) {
        try {
            URI uri = new URI(value);
            if (!uri.isAbsolute() || (!"http".equalsIgnoreCase(uri.getScheme())
                    && !"https".equalsIgnoreCase(uri.getScheme()))
                    || !StringUtils.hasText(uri.getHost())
                    || uri.getUserInfo() != null
                    || uri.getFragment() != null) {
                throw new IllegalArgumentException(name + " must be an absolute HTTP(S) URI");
            }
            if ("http".equalsIgnoreCase(uri.getScheme()) && !isLoopbackHost(uri.getHost())) {
                throw new IllegalArgumentException(name + " must use HTTPS outside a local environment");
            }
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(name + " is invalid", e);
        }
    }

    private static void requireAbsoluteHttpsUri(String value, String name) {
        try {
            URI uri = new URI(value);
            if (!uri.isAbsolute()
                    || !"https".equalsIgnoreCase(uri.getScheme())
                    || !StringUtils.hasText(uri.getHost())
                    || uri.getUserInfo() != null
                    || uri.getFragment() != null) {
                throw new IllegalArgumentException(name + " must be an absolute HTTPS URI");
            }
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(name + " is invalid", e);
        }
    }

    private static boolean isLoopbackHost(String host) {
        String normalizedHost = host.toLowerCase(Locale.ROOT);
        return "localhost".equals(normalizedHost)
                || normalizedHost.endsWith(".localhost")
                || normalizedHost.startsWith("127.")
                || "::1".equals(normalizedHost);
    }
}
