package com.example.moodtail.domain.user.service;

import com.example.moodtail.domain.user.dto.response.PasswordResetVerificationResponse;
import com.example.moodtail.domain.user.support.LocalAuthPropertiesFixtures;
import com.example.moodtail.global.lock.IdentityLockManager;
import com.example.moodtail.global.token.repository.redis.RedisRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.Optional;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    @Mock LocalAccountService localAccountService;
    @Mock PasswordResetMailSender mailSender;
    @Mock RedisRepository redisRepository;
    @Mock IdentityLockManager identityLockManager;

    private PasswordResetService service;

    @BeforeEach
    void setUp() {
        service = new PasswordResetService(
                localAccountService,
                mailSender,
                redisRepository,
                identityLockManager,
                LocalAuthPropertiesFixtures.enabled()
        );
        lenient().when(localAccountService.normalizeEmail(anyString()))
                .thenAnswer(invocation -> invocation.getArgument(0));
        lenient().when(redisRepository.acquirePasswordResetClientSlot(anyString(), anyInt(), any())).thenReturn(true);
        lenient().when(redisRepository.acquirePasswordResetCooldown(anyString(), any())).thenReturn(true);
        lenient().when(identityLockManager.executeForPasswordResetToken(anyString(), any()))
                .thenAnswer(invocation -> get(invocation.getArgument(1)));
    }

    @Test
    void requestCodeStoresDigestAndSendsOnlyForExistingLocalAccount() {
        when(localAccountService.findPasswordResetAccount("user@example.com"))
                .thenReturn(Optional.of(new LocalAccountService.PasswordResetAccount(
                        3L, 9L, 2, "user@example.com"
                )));

        service.requestCode("user@example.com", new MockHttpServletRequest());

        verify(redisRepository).savePasswordResetCode(anyString(), eq(3L), eq(2), anyString(), any());
        verify(mailSender).sendCode(eq("user@example.com"), anyString());
    }

    @Test
    void verifiedCodeReturnsOpaqueOneTimeResetToken() {
        when(redisRepository.verifyPasswordResetCode(anyString(), anyString(), eq(5)))
                .thenReturn(Optional.of(new RedisRepository.PasswordResetTokenSession(3L, 2)));

        PasswordResetVerificationResponse result = service.verifyCode("user@example.com", "123456");

        assertThat(result.resetToken()).isNotBlank();
        assertThat(result.expiresInSeconds()).isEqualTo(600L);
        ArgumentCaptor<String> tokenKeyCaptor = ArgumentCaptor.forClass(String.class);
        verify(redisRepository).savePasswordResetToken(tokenKeyCaptor.capture(), any(), any());
        assertThat(tokenKeyCaptor.getValue())
                .hasSize(64)
                .isNotEqualTo(result.resetToken());
    }

    @Test
    void passwordChangeConsumesTokenAndRevokesRefreshSession() {
        when(redisRepository.consumePasswordResetToken(anyString()))
                .thenReturn(Optional.of(new RedisRepository.PasswordResetTokenSession(3L, 2)));
        org.mockito.Mockito.doAnswer(invocation -> {
            java.util.function.Consumer<LocalAccountService.PasswordResetAccount> completion = invocation.getArgument(4);
            completion.accept(new LocalAccountService.PasswordResetAccount(3L, 9L, 3, "user@example.com"));
            return null;
        }).when(localAccountService).changePassword(eq(3L), eq(2), anyString(), anyString(), any());

        service.changePassword("reset-token", "new-password", "new-password");

        ArgumentCaptor<String> tokenKeyCaptor = ArgumentCaptor.forClass(String.class);
        verify(redisRepository).consumePasswordResetToken(tokenKeyCaptor.capture());
        assertThat(tokenKeyCaptor.getValue()).hasSize(64).isNotEqualTo("reset-token");
        verify(localAccountService).changePassword(eq(3L), eq(2), eq("new-password"), eq("new-password"), any());
        verify(redisRepository).deleteRefreshJti(9L);
    }

    @Test
    void rejectedClientRateLimitDoesNotConsumeEmailCooldown() {
        when(redisRepository.acquirePasswordResetClientSlot(anyString(), anyInt(), any())).thenReturn(false);

        assertThatThrownBy(() -> service.requestCode("user@example.com", new MockHttpServletRequest()))
                .isInstanceOf(com.example.moodtail.global.common.exception.RestApiException.class);

        verify(redisRepository, never()).acquirePasswordResetCooldown(anyString(), any());
        verify(localAccountService, never()).findPasswordResetAccount(anyString());
    }

    @Test
    void mailFailureReleasesCodeAndEmailCooldownForRetry() {
        when(localAccountService.findPasswordResetAccount("user@example.com"))
                .thenReturn(Optional.of(new LocalAccountService.PasswordResetAccount(
                        3L, 9L, 2, "user@example.com"
                )));
        doThrow(new IllegalStateException("SMTP unavailable"))
                .when(mailSender).sendCode(eq("user@example.com"), anyString());

        assertThatThrownBy(() -> service.requestCode("user@example.com", new MockHttpServletRequest()))
                .isInstanceOf(IllegalStateException.class);

        verify(redisRepository).deletePasswordResetCode(anyString());
        verify(redisRepository).deletePasswordResetCooldown(anyString());
    }

    private <T> T get(Supplier<T> supplier) {
        return supplier.get();
    }
}
