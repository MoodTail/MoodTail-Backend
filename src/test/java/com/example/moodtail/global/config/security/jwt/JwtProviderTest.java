package com.example.moodtail.global.config.security.jwt;

import com.example.moodtail.domain.user.entity.UserRole;
import com.example.moodtail.global.common.exception.RestApiException;
import com.example.moodtail.global.token.repository.redis.RedisRepository;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

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
        Claims refreshClaims = jwtProvider.getRefreshTokenClaims(tokenInfo.refreshToken());
        when(redisRepository.findRefreshJtiByUserId(1L)).thenReturn(Optional.of(refreshClaims.getId()));

        Claims accessClaims = jwtProvider.validateAccessTokenAndGetClaims(tokenInfo.accessToken())
                .orElseThrow();

        assertThat(accessClaims.getSubject()).isEqualTo("1");
        assertThat(accessClaims.get("role", String.class)).isEqualTo("USER");
        assertThat(jwtProvider.validateAccessTokenAndGetClaims(tokenInfo.refreshToken())).isEmpty();
    }

    @Test
    void redisFailureDuringSessionCheckFailsClosedWithServiceUnavailable() {
        String accessToken = jwtProvider.generateToken(1L, UserRole.USER).accessToken();
        when(redisRepository.findRefreshJtiByUserId(1L))
                .thenThrow(new RedisConnectionFailureException("redis unavailable"));

        assertThatThrownBy(() -> jwtProvider.validateAccessTokenAndGetClaims(accessToken))
                .isInstanceOfSatisfying(RestApiException.class, exception ->
                        assertThat(exception.getErrorCode().getCode()).isEqualTo("AUTH028")
                );
    }

    @Test
    void accessTokenIsRejectedWhenStoredSessionChanges() {
        TokenInfo firstSession = jwtProvider.generateToken(1L, UserRole.USER);
        Claims firstRefreshClaims = jwtProvider.getRefreshTokenClaims(firstSession.refreshToken());
        TokenInfo secondSession = jwtProvider.generateToken(1L, UserRole.USER);
        Claims secondRefreshClaims = jwtProvider.getRefreshTokenClaims(secondSession.refreshToken());

        when(redisRepository.findRefreshJtiByUserId(1L))
                .thenReturn(Optional.of(secondRefreshClaims.getId()));

        assertThat(jwtProvider.validateAccessTokenAndGetClaims(firstSession.accessToken())).isEmpty();
        assertThat(jwtProvider.validateAccessTokenAndGetClaims(secondSession.accessToken())).isPresent();
        assertThat(firstRefreshClaims.getId()).isNotEqualTo(secondRefreshClaims.getId());
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
