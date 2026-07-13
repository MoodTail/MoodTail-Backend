package com.example.moodtail.domain.auth.service;

import com.example.moodtail.domain.auth.validator.AuthRequestOriginValidator;
import com.example.moodtail.domain.user.entity.UserRole;
import com.example.moodtail.domain.user.repository.UserRepository;
import com.example.moodtail.global.config.security.jwt.JwtProvider;
import com.example.moodtail.global.config.security.jwt.TokenInfo;
import com.example.moodtail.global.token.repository.redis.RedisRepository;
import com.example.moodtail.support.auth.AuthPropertiesFixtures;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Optional;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TokenSessionServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private RedisRepository redisRepository;

    @Mock
    private AuthRequestOriginValidator authRequestOriginValidator;

    private TokenSessionService service;

    @BeforeEach
    void setUp() {
        service = new TokenSessionService(
                userRepository,
                jwtProvider,
                redisRepository,
                authRequestOriginValidator,
                AuthPropertiesFixtures.defaults()
        );
        ReflectionTestUtils.setField(service, "jwtRefreshExpirationMillis", 1_209_600_000L);
        TransactionSynchronizationManager.setActualTransactionActive(true);
        TransactionSynchronizationManager.initSynchronization();
    }

    @AfterEach
    void tearDown() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
        TransactionSynchronizationManager.setActualTransactionActive(false);
    }

    @Test
    void rollbackRestoresPreviousSessionOnlyWhenIssuedSessionStillOwnsTheKey() {
        when(redisRepository.findRefreshJtiByUserId(7L)).thenReturn(Optional.of("previous-jti"));
        when(jwtProvider.generateToken(7L, UserRole.USER))
                .thenReturn(new TokenInfo("access", "refresh"));
        when(jwtProvider.getRefreshTokenClaims("refresh")).thenReturn(refreshClaims("issued-jti"));

        service.issueSession(7L, UserRole.USER);
        completeRollback();

        verify(redisRepository).replaceRefreshJti(7L, "issued-jti", "previous-jti");
    }

    @Test
    void rollbackDeletesNewSessionOnlyWhenThereWasNoPreviousSession() {
        when(redisRepository.findRefreshJtiByUserId(7L)).thenReturn(Optional.empty());
        when(jwtProvider.generateToken(7L, UserRole.USER))
                .thenReturn(new TokenInfo("access", "refresh"));
        when(jwtProvider.getRefreshTokenClaims("refresh")).thenReturn(refreshClaims("issued-jti"));

        service.issueSession(7L, UserRole.USER);
        completeRollback();

        verify(redisRepository).deleteRefreshJtiIfMatches(7L, "issued-jti");
    }

    @Test
    void rollbackDoesNotOverwriteANewerSessionWhenRestoringARevokedSession() {
        when(redisRepository.findRefreshJtiByUserId(7L)).thenReturn(Optional.of("revoked-jti"));

        service.revokeSessionWithRollback(7L);
        completeRollback();

        verify(redisRepository).saveRefreshJtiIfAbsent(7L, "revoked-jti");
    }

    private Claims refreshClaims(String jti) {
        return Jwts.claims().setId(jti);
    }

    private void completeRollback() {
        TransactionSynchronizationManager.getSynchronizations()
                .forEach(synchronization -> synchronization.afterCompletion(
                        TransactionSynchronization.STATUS_ROLLED_BACK
                ));
    }
}
