package com.example.moodtail.domain.auth.service;

import com.example.moodtail.domain.auth.dto.response.PasswordResetVerificationResponse;
import com.example.moodtail.domain.auth.model.PasswordResetAccount;
import com.example.moodtail.global.auth.mail.PasswordResetMailSender;
import com.example.moodtail.global.common.exception.RestApiException;
import com.example.moodtail.global.common.exception.code.status.AuthErrorStatus;
import com.example.moodtail.global.token.repository.redis.RedisRepository;
import com.example.moodtail.support.auth.LocalAuthPropertiesFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    @Mock LocalAccountService localAccountService;
    @Mock PasswordResetMailSender mailSender;
    @Mock RedisRepository redisRepository;

    private PasswordResetService service;

    @BeforeEach
    void setUp() {
        service = new PasswordResetService(
                localAccountService,
                mailSender,
                redisRepository,
                LocalAuthPropertiesFixtures.enabled()
        );
        lenient().when(localAccountService.normalizeEmail(anyString()))
                .thenAnswer(invocation -> invocation.getArgument(0));
        lenient().when(redisRepository.acquirePasswordResetClientSlot(anyString(), anyInt(), any())).thenReturn(true);
        lenient().when(redisRepository.acquirePasswordResetCooldown(anyString(), any())).thenReturn(true);
    }

    @Test
    void requestCodeStoresDigestAndSendsOnlyForExistingLocalAccount() {
        when(localAccountService.findPasswordResetAccount("user@example.com"))
                .thenReturn(Optional.of(new PasswordResetAccount(
                        3L, 2, "user@example.com"
                )));

        service.requestCode("user@example.com", "203.0.113.7");

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
    void malformedVerificationCodeIsRejectedBeforeRedisLookup() {
        assertThatThrownBy(() -> service.verifyCode("user@example.com", "12ab56"))
                .isInstanceOf(com.example.moodtail.global.common.exception.RestApiException.class);

        verify(redisRepository, never()).verifyPasswordResetCode(anyString(), anyString(), anyInt());
    }

    @Test
    void passwordChangeUsesTokenOnceAndDelegatesToLocalAccountService() {
        when(redisRepository.findPasswordResetToken(anyString()))
                .thenReturn(Optional.of(new RedisRepository.PasswordResetTokenSession(3L, 2)));

        service.changePassword("reset-token", "new-password", "new-password");

        ArgumentCaptor<String> tokenKeyCaptor = ArgumentCaptor.forClass(String.class);
        verify(redisRepository).findPasswordResetToken(tokenKeyCaptor.capture());
        assertThat(tokenKeyCaptor.getValue()).hasSize(64).isNotEqualTo("reset-token");
        verify(localAccountService).changePassword(3L, 2, "new-password", "new-password");
        verify(redisRepository).deletePasswordResetToken(tokenKeyCaptor.getValue());
    }

    @Test
    void passwordChangeKeepsResetTokenWhenDatabaseChangeFails() {
        when(redisRepository.findPasswordResetToken(anyString()))
                .thenReturn(Optional.of(new RedisRepository.PasswordResetTokenSession(3L, 2)));
        doThrow(new RestApiException(AuthErrorStatus.AUTH_INFRASTRUCTURE_UNAVAILABLE))
                .when(localAccountService)
                .changePassword(3L, 2, "new-password", "new-password");

        assertThatThrownBy(() -> service.changePassword("reset-token", "new-password", "new-password"))
                .isInstanceOf(RestApiException.class);

        verify(redisRepository, never()).deletePasswordResetToken(anyString());
    }

    @Test
    void blankResetTokenIsRejectedBeforeRedisLookup() {
        assertThatThrownBy(() -> service.changePassword(" ", "new-password", "new-password"))
                .isInstanceOf(com.example.moodtail.global.common.exception.RestApiException.class);

        verify(redisRepository, never()).findPasswordResetToken(anyString());
    }

    @Test
    void rejectedClientRateLimitDoesNotConsumeEmailCooldown() {
        when(redisRepository.acquirePasswordResetClientSlot(anyString(), anyInt(), any())).thenReturn(false);

        assertThatThrownBy(() -> service.requestCode("user@example.com", "203.0.113.7"))
                .isInstanceOf(com.example.moodtail.global.common.exception.RestApiException.class);

        verify(redisRepository, never()).acquirePasswordResetCooldown(anyString(), any());
        verify(localAccountService, never()).findPasswordResetAccount(anyString());
    }

    @Test
    void appliesRateLimitToUnknownBucketWhenClientAddressIsMissing() {
        when(localAccountService.findPasswordResetAccount("user@example.com"))
                .thenReturn(Optional.empty());

        service.requestCode("user@example.com", null);

        ArgumentCaptor<String> fingerprint = ArgumentCaptor.forClass(String.class);
        verify(redisRepository).acquirePasswordResetClientSlot(
                fingerprint.capture(),
                anyInt(),
                any()
        );
        assertThat(fingerprint.getValue()).matches("[0-9a-f]{64}");
    }

    @Test
    void mailFailureReleasesCodeAndEmailCooldownForRetry() {
        when(localAccountService.findPasswordResetAccount("user@example.com"))
                .thenReturn(Optional.of(new PasswordResetAccount(
                        3L, 2, "user@example.com"
                )));
        doThrow(new IllegalStateException("SMTP unavailable"))
                .when(mailSender).sendCode(eq("user@example.com"), anyString());

        assertThatThrownBy(() -> service.requestCode("user@example.com", "203.0.113.7"))
                .isInstanceOf(IllegalStateException.class);

        verify(redisRepository).deletePasswordResetCode(anyString());
        verify(redisRepository).deletePasswordResetCooldown(anyString());
    }

}
