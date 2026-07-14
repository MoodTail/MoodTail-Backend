package com.example.moodtail.domain.auth.service;

import com.example.moodtail.domain.auth.entity.LocalAccount;
import com.example.moodtail.domain.auth.model.Consent;
import com.example.moodtail.domain.auth.model.LocalAuthenticationResult;
import com.example.moodtail.domain.auth.model.LocalAuthUser;
import com.example.moodtail.domain.auth.repository.LocalAccountRepository;
import com.example.moodtail.domain.user.entity.User;
import com.example.moodtail.domain.user.entity.UserRole;
import com.example.moodtail.domain.user.repository.UserRepository;
import com.example.moodtail.global.common.exception.RestApiException;
import com.example.moodtail.global.config.security.jwt.TokenInfo;
import com.example.moodtail.global.lock.IdentityLockManager;
import com.example.moodtail.support.auth.LocalAuthPropertiesFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.SimpleTransactionStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LocalAccountServiceTest {

    @Mock PlatformTransactionManager transactionManager;
    @Mock LocalAccountRepository localAccountRepository;
    @Mock UserRepository userRepository;
    @Mock GuestDataMergeService guestDataMergeService;
    @Mock TermAgreementService termAgreementService;
    @Mock IdentityLockManager identityLockManager;
    @Mock TokenSessionService tokenSessionService;

    private PasswordEncoder passwordEncoder;
    private LocalAccountService service;

    @BeforeEach
    void setUp() {
        passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        service = new LocalAccountService(
                transactionManager,
                localAccountRepository,
                userRepository,
                guestDataMergeService,
                termAgreementService,
                identityLockManager,
                passwordEncoder,
                LocalAuthPropertiesFixtures.enabled(),
                tokenSessionService
        );
        lenient().when(transactionManager.getTransaction(any())).thenReturn(new SimpleTransactionStatus());
        lenient().when(identityLockManager.executeForLocalEmail(anyString(), any()))
                .thenAnswer(invocation -> get(invocation.getArgument(1)));
        lenient().when(identityLockManager.executeForGuestUserId(any(), any()))
                .thenAnswer(invocation -> get(invocation.getArgument(1)));
    }

    @Test
    void signupUpgradesGuestAndCreatesLocalCredential() {
        User guest = guest(2L);
        when(localAccountRepository.existsByEmail("user@example.com")).thenReturn(false);
        when(userRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(guest));
        when(localAccountRepository.saveAndFlush(any(LocalAccount.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(tokenSessionService.issueSessionReplacingGuest(2L, UserRole.USER, 2L))
                .thenReturn(new TokenInfo("access", "refresh"));

        LocalAuthenticationResult authentication = service.signup(
                " User@Example.com ",
                "password123!",
                "password123!",
                "무드테일러",
                List.of(new Consent(1L, true)),
                2L
        );
        LocalAuthUser result = authentication.user();

        assertThat(result.userId()).isEqualTo(2L);
        assertThat(result.email()).isEqualTo("user@example.com");
        assertThat(result.role()).isEqualTo(UserRole.USER);
        assertThat(guest.getGuestUuid()).isNull();
        verify(termAgreementService).recordValidatedAgreements(any(), any(), any());
        InOrder order = inOrder(transactionManager, tokenSessionService);
        order.verify(transactionManager).commit(any());
        order.verify(tokenSessionService).issueSessionReplacingGuest(2L, UserRole.USER, 2L);
    }

    @Test
    void failedLoginIsPersistedBeforeReturningGenericCredentialError() {
        User user = member(9L);
        LocalAccount account = LocalAccount.create(
                user,
                "user@example.com",
                passwordEncoder.encode("correct-password"),
                LocalDateTime.now()
        );
        when(localAccountRepository.findByEmailForUpdate("user@example.com"))
                .thenReturn(Optional.of(account));

        assertThatThrownBy(() -> service.login(
                "user@example.com", "wrong-password", null
        )).isInstanceOfSatisfying(RestApiException.class, exception ->
                assertThat(exception.getErrorCode().getCode()).isEqualTo("AUTH011")
        );

        assertThat(account.getFailedLoginAttempts()).isEqualTo(1);
    }

    @Test
    void unknownEmailReturnsTheSameGenericCredentialError() {
        when(localAccountRepository.findByEmailForUpdate("unknown@example.com"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.login(
                "unknown@example.com", "wrong-password", null
        )).isInstanceOfSatisfying(RestApiException.class, exception ->
                assertThat(exception.getErrorCode().getCode()).isEqualTo("AUTH011")
        );
    }

    @Test
    void loginRejectsPasswordOverBcryptByteLimitAsGenericCredentialError() {
        String oversizedUtf8Password = "가".repeat(25);

        assertThatThrownBy(() -> service.login(
                "user@example.com", oversizedUtf8Password, null
        )).isInstanceOfSatisfying(RestApiException.class, exception ->
                assertThat(exception.getErrorCode().getCode()).isEqualTo("AUTH011")
        );
    }

    @Test
    void signupUniqueConstraintRaceReturnsAccountAlreadyExists() {
        User guest = guest(2L);
        when(localAccountRepository.existsByEmail("user@example.com")).thenReturn(false, true);
        when(termAgreementService.validateAgreements(any())).thenReturn(List.of());
        when(userRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(guest));
        when(localAccountRepository.saveAndFlush(any(LocalAccount.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate email"));

        assertThatThrownBy(() -> service.signup(
                "user@example.com",
                "password123!",
                "password123!",
                "무드테일러",
                List.of(new Consent(1L, true)),
                2L
        )).isInstanceOfSatisfying(RestApiException.class, exception ->
                assertThat(exception.getErrorCode().getCode()).isEqualTo("AUTH034")
        );
    }

    @Test
    void loginMergesCurrentGuestIntoExistingLocalUser() {
        User user = member(9L);
        LocalAccount account = LocalAccount.create(
                user,
                "user@example.com",
                passwordEncoder.encode("correct-password"),
                LocalDateTime.now()
        );
        when(localAccountRepository.findByEmailForUpdate("user@example.com"))
                .thenReturn(Optional.of(account));
        when(tokenSessionService.issueSessionReplacingGuest(9L, UserRole.USER, 2L))
                .thenReturn(new TokenInfo("access", "refresh"));

        LocalAuthUser result = service.login("user@example.com", "correct-password", 2L).user();

        assertThat(result.userId()).isEqualTo(9L);
        verify(guestDataMergeService).mergeIntoExistingUser(2L, 9L);
        InOrder order = inOrder(transactionManager, tokenSessionService);
        order.verify(transactionManager).commit(any());
        order.verify(tokenSessionService).issueSessionReplacingGuest(9L, UserRole.USER, 2L);
    }

    @Test
    void passwordChangeRevokesTheExistingSessionAfterDatabaseCommit() {
        User user = member(9L);
        LocalAccount account = LocalAccount.create(
                user,
                "user@example.com",
                passwordEncoder.encode("old-password"),
                LocalDateTime.now()
        );
        when(localAccountRepository.findByIdForUpdate(3L)).thenReturn(Optional.of(account));

        service.changePassword(3L, 0, "new-password", "new-password");

        assertThat(account.getPasswordVersion()).isEqualTo(1);
        assertThat(passwordEncoder.matches("new-password", account.getPasswordHash())).isTrue();
        InOrder order = inOrder(transactionManager, tokenSessionService);
        order.verify(transactionManager).commit(any());
        order.verify(tokenSessionService).revokeSession(9L);
    }

    @Test
    void passwordChangeCanRetrySessionRevocationAfterDatabaseCommit() {
        User user = member(9L);
        LocalAccount account = LocalAccount.create(
                user,
                "user@example.com",
                passwordEncoder.encode("old-password"),
                LocalDateTime.now()
        );
        when(localAccountRepository.findByIdForUpdate(3L)).thenReturn(Optional.of(account));
        doThrow(new RedisConnectionFailureException("redis unavailable"))
                .doNothing()
                .when(tokenSessionService).revokeSession(9L);

        assertThatThrownBy(() -> service.changePassword(3L, 0, "new-password", "new-password"))
                .isInstanceOf(RedisConnectionFailureException.class);

        assertThat(account.getPasswordVersion()).isEqualTo(1);
        service.changePassword(3L, 0, "new-password", "new-password");

        assertThat(account.getPasswordVersion()).isEqualTo(1);
        assertThat(passwordEncoder.matches("new-password", account.getPasswordHash())).isTrue();
        verify(tokenSessionService, times(2)).revokeSession(9L);
    }

    private User guest(Long id) {
        User user = User.createGuest(UUID.randomUUID().toString(), "게스트", LocalDateTime.now());
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private User member(Long id) {
        User user = User.createMember("사용자", LocalDateTime.now());
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private <T> T get(Supplier<T> supplier) {
        return supplier.get();
    }
}
