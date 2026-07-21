package com.example.moodtail.global.auth.config;

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
                    "auth.oauth.state-rate-limit.max-attempts=10",
                    "auth.oauth.state-rate-limit.window-millis=60000",
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
                    "auth.refresh-cookie.domain=.example.com",
                    "auth.refresh-cookie.secure=true",
                    "auth.refresh-cookie.same-site=None",
                    "auth.guest-login.default-nickname=방문자",
                    "auth.guest-login.uuid-rate-limit.max-attempts=10",
                    "auth.guest-login.uuid-rate-limit.window-millis=60000",
                    "auth.guest-login.client-rate-limit.max-attempts=60",
                    "auth.guest-login.client-rate-limit.window-millis=60000",
                    "auth.redis.key-prefix=moodtail:auth:test:",
                    "auth.local.password.min-length=8",
                    "auth.local.password.max-bytes=72",
                    "auth.local.login.max-failed-attempts=5",
                    "auth.local.login.lock-duration-millis=900000",
                    "auth.local.password-reset.enabled=false",
                    "auth.local.password-reset.sender=",
                    "auth.local.password-reset.subject=MoodTail password reset code",
                    "auth.local.password-reset.body-template=Code: %s",
                    "auth.local.password-reset.pepper=",
                    "auth.local.password-reset.code-expiration-millis=300000",
                    "auth.local.password-reset.token-expiration-millis=600000",
                    "auth.local.password-reset.resend-cooldown-millis=60000",
                    "auth.local.password-reset.max-verification-attempts=5",
                    "auth.local.password-reset.client-rate-limit.max-attempts=10",
                    "auth.local.password-reset.client-rate-limit.window-millis=600000"
            );

    @Test
    void bindsDeploymentSpecificAuthSettings() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(AuthProperties.class);
            assertThat(context).hasSingleBean(LocalAuthProperties.class);
            assertThat(context).hasSingleBean(org.springframework.security.crypto.password.PasswordEncoder.class);
            AuthProperties properties = context.getBean(AuthProperties.class);
            assertThat(properties.oauth().kakao().clientId()).isEqualTo("kakao-client-id");
            assertThat(properties.oauth().google().clientId()).isEqualTo("google-client-id");
            assertThat(properties.oauth().google().enabled()).isTrue();
            assertThat(properties.oauth().connectTimeoutMillis()).isEqualTo(3_000L);
            assertThat(properties.oauth().stateRateLimit().maxAttempts()).isEqualTo(10);
            assertThat(properties.oauth().stateRateLimit().windowMillis()).isEqualTo(60_000L);
            assertThat(properties.refreshCookie().domain()).isEqualTo(".example.com");
            assertThat(properties.refreshCookie().sameSite()).isEqualTo("None");
            assertThat(properties.guestLogin().defaultNickname()).isEqualTo("방문자");
            assertThat(properties.redis().keyPrefix()).isEqualTo("moodtail:auth:test:");

            LocalAuthProperties localProperties = context.getBean(LocalAuthProperties.class);
            assertThat(localProperties.password().minLength()).isEqualTo(8);
            assertThat(localProperties.password().maxBytes()).isEqualTo(72);
            assertThat(localProperties.login().maxFailedAttempts()).isEqualTo(5);
            assertThat(localProperties.login().lockDurationMillis()).isEqualTo(900_000L);
            assertThat(localProperties.passwordReset().enabled()).isFalse();
            assertThat(localProperties.passwordReset().codeExpirationMillis()).isEqualTo(300_000L);
            assertThat(localProperties.passwordReset().tokenExpirationMillis()).isEqualTo(600_000L);
            assertThat(localProperties.passwordReset().clientRateLimit().maxAttempts()).isEqualTo(10);
            assertThat(localProperties.passwordReset().clientRateLimit().windowMillis()).isEqualTo(600_000L);
        });
    }
}
