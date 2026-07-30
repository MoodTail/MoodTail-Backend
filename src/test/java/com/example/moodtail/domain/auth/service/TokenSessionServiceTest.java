package com.example.moodtail.domain.auth.service;

import com.example.moodtail.domain.user.entity.UserRole;
import com.example.moodtail.domain.user.repository.UserRepository;
import com.example.moodtail.global.config.security.jwt.JwtProvider;
import com.example.moodtail.global.config.security.jwt.TokenInfo;
import com.example.moodtail.global.token.repository.redis.RedisRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TokenSessionServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PlatformTransactionManager transactionManager;

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private RedisRepository redisRepository;

    private TokenSessionService service;

    @BeforeEach
    void setUp() {
        service = new TokenSessionService(
                userRepository,
                transactionManager,
                jwtProvider,
                redisRepository
        );
    }

    @AfterEach
    void tearDown() {
        TransactionSynchronizationManager.setActualTransactionActive(false);
    }

    @Test
    void sessionIssueStoresRefreshJtiOutsideDatabaseTransaction() {
        when(jwtProvider.generateToken(7L, UserRole.USER))
                .thenReturn(new TokenInfo("access", "refresh"));
        when(jwtProvider.getRefreshTokenClaims("refresh")).thenReturn(refreshClaims("issued-jti"));

        service.issueSession(7L, UserRole.USER);

        verify(redisRepository).saveRefreshJti(7L, "issued-jti");
    }

    @Test
    void sessionIssueIsRejectedWhileDatabaseTransactionIsActive() {
        TransactionSynchronizationManager.setActualTransactionActive(true);

        assertThatThrownBy(() -> service.issueSession(7L, UserRole.USER))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("after the database transaction commits");
    }

    @Test
    void sessionRevocationIsRejectedWhileDatabaseTransactionIsActive() {
        TransactionSynchronizationManager.setActualTransactionActive(true);

        assertThatThrownBy(() -> service.revokeSession(7L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("after the database transaction commits");
    }

    private Claims refreshClaims(String jti) {
        return Jwts.claims().setId(jti);
    }
}
