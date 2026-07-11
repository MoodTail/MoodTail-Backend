package com.example.moodtail.domain.user.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class AuthConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(AuthConfiguration.class)
            .withPropertyValues(
                    "auth.oauth.state-expiration-millis=300000",
                    "auth.oauth.connect-timeout-millis=3000",
                    "auth.oauth.read-timeout-millis=5000",
                    "auth.oauth.kakao.client-id=kakao-client-id",
                    "auth.oauth.kakao.enabled=true",
                    "auth.oauth.kakao.client-secret=kakao-client-secret",
                    "auth.oauth.kakao.redirect-uri=https://frontend.example.com/kakao/callback",
                    "auth.oauth.kakao.token-uri=https://kauth.kakao.com/oauth/token",
                    "auth.oauth.kakao.user-info-uri=https://kapi.kakao.com/v2/user/me",
                    "auth.oauth.google.client-id=google-client-id",
                    "auth.oauth.google.enabled=true",
                    "auth.oauth.google.client-secret=google-client-secret",
                    "auth.oauth.google.redirect-uri=https://frontend.example.com/google/callback",
                    "auth.oauth.google.token-uri=https://oauth2.googleapis.com/token",
                    "auth.oauth.google.user-info-uri=https://openidconnect.googleapis.com/v1/userinfo",
                    "auth.refresh-cookie.name=refreshToken",
                    "auth.refresh-cookie.path=/api/v1/auth",
                    "auth.refresh-cookie.domain=.example.com",
                    "auth.refresh-cookie.secure=true",
                    "auth.refresh-cookie.same-site=None",
                    "auth.guest-login.client-ip-header=X-Forwarded-For",
                    "auth.guest-login.default-nickname=방문자",
                    "auth.guest-login.uuid-rate-limit.max-attempts=10",
                    "auth.guest-login.uuid-rate-limit.window-millis=60000",
                    "auth.guest-login.client-rate-limit.max-attempts=60",
                    "auth.guest-login.client-rate-limit.window-millis=60000",
                    "auth.concurrency.lock-ttl-millis=10000",
                    "auth.concurrency.retry-interval-millis=25",
                    "auth.concurrency.acquire-timeout-millis=3000",
                    "auth.concurrency.social-registration-max-attempts=3",
                    "auth.redis.key-prefix=moodtail:auth:test:"
            );

    @Test
    void bindsDeploymentSpecificAuthSettings() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(AuthProperties.class);
            AuthProperties properties = context.getBean(AuthProperties.class);
            assertThat(properties.oauth().kakao().clientId()).isEqualTo("kakao-client-id");
            assertThat(properties.oauth().google().clientId()).isEqualTo("google-client-id");
            assertThat(properties.oauth().google().enabled()).isTrue();
            assertThat(properties.oauth().connectTimeoutMillis()).isEqualTo(3_000L);
            assertThat(properties.refreshCookie().domain()).isEqualTo(".example.com");
            assertThat(properties.refreshCookie().sameSite()).isEqualTo("None");
            assertThat(properties.guestLogin().clientIpHeader()).isEqualTo("X-Forwarded-For");
            assertThat(properties.guestLogin().defaultNickname()).isEqualTo("방문자");
            assertThat(properties.concurrency().socialRegistrationMaxAttempts()).isEqualTo(3);
            assertThat(properties.redis().keyPrefix()).isEqualTo("moodtail:auth:test:");
        });
    }
}
