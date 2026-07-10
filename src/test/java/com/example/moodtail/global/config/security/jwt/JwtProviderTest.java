package com.example.moodtail.global.config.security.jwt;

import com.example.moodtail.domain.user.enums.UserRole;
import com.example.moodtail.global.token.repository.redis.RedisRepository;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtProviderTest {

    private static final String JWT_SECRET = "bW9vZHRhaWwtdGVzdC1zZWNyZXQta2V5LTMyYnl0ZXMhIQ==";

    @Mock
    private RedisRepository redisRepository;

    private JwtProvider jwtProvider;

    @BeforeEach
    void setUp() {
        jwtProvider = new JwtProvider(redisRepository);
        ReflectionTestUtils.setField(jwtProvider, "jwtSecretKey", JWT_SECRET);
        ReflectionTestUtils.setField(jwtProvider, "jwtAccessExpiration", 900_000L);
        ReflectionTestUtils.setField(jwtProvider, "jwtRefreshExpiration", 1_209_600_000L);
        jwtProvider.init();
    }

    @Test
    void generatedTokensAreValidatedByTokenType() {
        TokenInfo tokenInfo = jwtProvider.generateToken(1L, UserRole.USER);
        Claims accessClaims = jwtProvider.getAccessTokenClaims(tokenInfo.accessToken());
        Claims refreshClaims = jwtProvider.getRefreshTokenClaims(tokenInfo.refreshToken());

        when(redisRepository.isJtiBlocked(accessClaims.getId())).thenReturn(false);
        when(redisRepository.isJtiBlocked(refreshClaims.getId())).thenReturn(false);

        assertThat(jwtProvider.validateAccessToken(tokenInfo.accessToken())).isTrue();
        assertThat(jwtProvider.validateRefreshToken(tokenInfo.accessToken())).isFalse();
        assertThat(jwtProvider.validateRefreshToken(tokenInfo.refreshToken())).isTrue();
        assertThat(jwtProvider.validateAccessToken(tokenInfo.refreshToken())).isFalse();
    }

    @Test
    void blacklistedJtiInvalidatesAccessToken() {
        String accessToken = jwtProvider.generateToken(1L, UserRole.USER, TokenType.ACCESS);
        Claims claims = jwtProvider.getAccessTokenClaims(accessToken);

        when(redisRepository.isJtiBlocked(claims.getId())).thenReturn(true);

        assertThat(jwtProvider.validateAccessToken(accessToken)).isFalse();
    }

    @Test
    void initializationRejectsWeakSecret() {
        JwtProvider provider = configuredProvider("d2Vhaw==", 900_000L, 1_209_600_000L);

        assertThatThrownBy(provider::init)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("256 bits");
    }

    @Test
    void initializationRejectsRefreshExpirationNotLongerThanAccessExpiration() {
        JwtProvider provider = configuredProvider(JWT_SECRET, 900_000L, 900_000L);

        assertThatThrownBy(provider::init)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("greater than access expiration");
    }

    private JwtProvider configuredProvider(String secret, long accessExpiration, long refreshExpiration) {
        JwtProvider provider = new JwtProvider(redisRepository);
        ReflectionTestUtils.setField(provider, "jwtSecretKey", secret);
        ReflectionTestUtils.setField(provider, "jwtAccessExpiration", accessExpiration);
        ReflectionTestUtils.setField(provider, "jwtRefreshExpiration", refreshExpiration);
        return provider;
    }
}
