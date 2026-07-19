package com.example.moodtail.domain.auth.service;

import com.example.moodtail.domain.auth.entity.SocialAccount;
import com.example.moodtail.domain.auth.model.Consent;
import com.example.moodtail.domain.auth.model.SocialLoginUser;
import com.example.moodtail.domain.auth.repository.SocialAccountRepository;
import com.example.moodtail.domain.term.entity.Term;
import com.example.moodtail.domain.term.entity.TermType;
import com.example.moodtail.domain.term.repository.TermRepository;
import com.example.moodtail.domain.user.entity.User;
import com.example.moodtail.domain.user.entity.UserRole;
import com.example.moodtail.domain.user.entity.UserTermAgreement;
import com.example.moodtail.domain.user.repository.UserRepository;
import com.example.moodtail.domain.user.repository.UserTermAgreementRepository;
import com.example.moodtail.global.auth.model.SocialProvider;
import com.example.moodtail.global.auth.model.SocialUserProfile;
import com.example.moodtail.global.common.exception.RestApiException;
import com.example.moodtail.global.common.exception.code.status.AuthErrorStatus;
import com.example.moodtail.global.config.security.jwt.TokenInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.SimpleTransactionStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SocialAccountServiceTest {

    @Mock
    private PlatformTransactionManager transactionManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SocialAccountRepository socialAccountRepository;

    @Mock
    private TermRepository termRepository;

    @Mock
    private UserTermAgreementRepository userTermAgreementRepository;

    @Mock
    private TokenSessionService tokenSessionService;

    private SocialAccountService service;

    @BeforeEach
    void setUp() {
        service = new SocialAccountService(
                transactionManager,
                userRepository,
                socialAccountRepository,
                new TermAgreementService(termRepository, userTermAgreementRepository),
                tokenSessionService
        );
        lenient().when(transactionManager.getTransaction(any())).thenReturn(new SimpleTransactionStatus());
        lenient().when(tokenSessionService.issueSessionReplacingGuest(anyLong(), any(), anyLong()))
                .thenReturn(new TokenInfo("access", "refresh"));
    }

    @Test
    void newAuthenticationUpgradesGuestAndStoresRequiredTermAgreement() {
        User guest = guestWithId(2L);
        Term requiredTerm = activeTerm(1L, TermType.SERVICE, true);
        SocialUserProfile profile = kakaoProfile();

        when(socialAccountRepository.findByProviderAndProviderUserId(SocialProvider.KAKAO, "12345"))
                .thenReturn(Optional.empty());
        when(userRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(guest));
        when(termRepository.findByActiveTrueOrderByIdAsc()).thenReturn(List.of(requiredTerm));
        when(socialAccountRepository.saveAndFlush(any(SocialAccount.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        SocialLoginUser result = service.authenticate(
                profile,
                2L,
                List.of(new Consent(1L, true))
        ).user();

        assertThat(result.userId()).isEqualTo(2L);
        assertThat(result.role()).isEqualTo(UserRole.USER);
        assertThat(result.socialEmail()).isNull();
        InOrder order = inOrder(transactionManager, tokenSessionService);
        order.verify(transactionManager).commit(any());
        order.verify(tokenSessionService).issueSessionReplacingGuest(2L, UserRole.USER, 2L);
        assertThat(result.isNewUser()).isTrue();
        assertThat(guest.getGuestUuid()).isNull();
        assertThat(guest.getRole()).isEqualTo(UserRole.USER);

        verify(userTermAgreementRepository).saveAll(argThat(agreements -> {
            assertThat(agreements)
                    .singleElement()
                    .satisfies(agreement -> {
                        assertThat(agreement.getUser()).isSameAs(guest);
                        assertThat(agreement.getTerm()).isSameAs(requiredTerm);
                    });
            return true;
        }));
    }

    @Test
    void newAuthenticationReportsCommittedAccountWhenSessionIssuanceFails() {
        User guest = guestWithId(2L);
        Term requiredTerm = activeTerm(1L, TermType.SERVICE, true);
        when(socialAccountRepository.findByProviderAndProviderUserId(SocialProvider.KAKAO, "12345"))
                .thenReturn(Optional.empty());
        when(userRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(guest));
        when(termRepository.findByActiveTrueOrderByIdAsc()).thenReturn(List.of(requiredTerm));
        when(socialAccountRepository.saveAndFlush(any(SocialAccount.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(tokenSessionService.issueSessionReplacingGuest(2L, UserRole.USER, 2L))
                .thenThrow(new RestApiException(AuthErrorStatus.AUTH_INFRASTRUCTURE_UNAVAILABLE));

        assertThatThrownBy(() -> service.authenticate(
                kakaoProfile(),
                2L,
                List.of(new Consent(1L, true))
        )).isInstanceOfSatisfying(RestApiException.class, exception ->
                assertThat(exception.getErrorCode().getCode()).isEqualTo("AUTH041")
        );

        assertThat(guest.getRole()).isEqualTo(UserRole.USER);
        InOrder order = inOrder(transactionManager, tokenSessionService);
        order.verify(transactionManager).commit(any());
        order.verify(tokenSessionService).issueSessionReplacingGuest(2L, UserRole.USER, 2L);
    }

    @Test
    void authenticationRetriesOneTransientLockFailure() {
        User existingUser = guestWithId(99L);
        existingUser.upgradeToUser("기존유저", LocalDateTime.now());
        SocialAccount account = SocialAccount.create(
                existingUser,
                SocialProvider.KAKAO,
                "12345",
                null
        );
        when(socialAccountRepository.findByProviderAndProviderUserId(SocialProvider.KAKAO, "12345"))
                .thenThrow(new CannotAcquireLockException("transient lock failure"))
                .thenReturn(Optional.of(account));

        SocialLoginUser result = service.authenticate(
                kakaoProfile(),
                2L,
                null
        ).user();

        assertThat(result.userId()).isEqualTo(99L);
        verify(socialAccountRepository, times(2))
                .findByProviderAndProviderUserId(SocialProvider.KAKAO, "12345");
    }

    @Test
    void authenticationStopsAfterThreeLockFailures() {
        CannotAcquireLockException lockFailure =
                new CannotAcquireLockException("persistent lock failure");
        when(socialAccountRepository.findByProviderAndProviderUserId(SocialProvider.KAKAO, "12345"))
                .thenThrow(lockFailure);

        assertThatThrownBy(() -> service.authenticate(
                kakaoProfile(),
                2L,
                null
        )).isSameAs(lockFailure);

        verify(socialAccountRepository, times(3))
                .findByProviderAndProviderUserId(SocialProvider.KAKAO, "12345");
        verifyNoInteractions(userRepository, termRepository, userTermAgreementRepository);
    }

    @Test
    void existingAuthenticationSwitchesToTheExistingSocialAccountWithoutMergingGuestData() {
        User existingUser = guestWithId(99L);
        existingUser.upgradeToUser("기존유저", LocalDateTime.now());
        SocialAccount account = SocialAccount.create(
                existingUser,
                SocialProvider.GOOGLE,
                "google-id",
                "user@example.com"
        );
        SocialUserProfile profile = new SocialUserProfile(
                SocialProvider.GOOGLE,
                "google-id",
                "user@example.com",
                "provider-nickname-is-longer-than-signup-policy"
        );
        when(socialAccountRepository.findByProviderAndProviderUserId(SocialProvider.GOOGLE, "google-id"))
                .thenReturn(Optional.of(account));
        when(tokenSessionService.issueSessionReplacingGuest(99L, UserRole.USER, 2L))
                .thenReturn(new TokenInfo("access", "refresh"));

        SocialLoginUser result = service.authenticate(
                profile,
                2L,
                List.of()
        ).user();

        assertThat(result.userId()).isEqualTo(99L);
        assertThat(result.isNewUser()).isFalse();
        InOrder order = inOrder(transactionManager, tokenSessionService);
        order.verify(transactionManager).commit(any());
        order.verify(tokenSessionService).issueSessionReplacingGuest(99L, UserRole.USER, 2L);
        verify(userTermAgreementRepository, never()).saveAll(any());
    }

    @Test
    void newAuthenticationRejectsInvalidProviderNicknameBeforeUpgradingGuest() {
        User guest = guestWithId(2L);
        SocialUserProfile profile = new SocialUserProfile(
                SocialProvider.KAKAO,
                "12345",
                "user@example.com",
                "provider-nickname-is-longer-than-signup-policy"
        );
        when(socialAccountRepository.findByProviderAndProviderUserId(SocialProvider.KAKAO, "12345"))
                .thenReturn(Optional.empty());
        when(userRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(guest));

        assertThatThrownBy(() -> service.authenticate(
                profile,
                2L,
                List.of(new Consent(1L, true))
        )).isInstanceOf(RestApiException.class);

        assertThat(guest.isGuest()).isTrue();
        verify(termRepository, never()).findByActiveTrueOrderByIdAsc();
        verify(socialAccountRepository, never()).saveAndFlush(any());
    }

    @Test
    void authenticationRejectsSoftDeletedSocialUser() {
        User deletedUser = guestWithId(99L);
        deletedUser.upgradeToUser("탈퇴회원", LocalDateTime.now());
        deletedUser.delete();
        SocialAccount account = SocialAccount.create(
                deletedUser,
                SocialProvider.KAKAO,
                "12345",
                null
        );
        when(socialAccountRepository.findByProviderAndProviderUserId(SocialProvider.KAKAO, "12345"))
                .thenReturn(Optional.of(account));

        assertThatThrownBy(() -> service.authenticate(
                kakaoProfile(),
                2L,
                List.of()
        ))
                .isInstanceOfSatisfying(RestApiException.class, exception ->
                        assertThat(exception.getErrorCode().getCode()).isEqualTo("AUTH020")
                );
    }

    @Test
    void authenticationRejectsMissingGuestBeforeQueryingAccounts() {
        assertThatThrownBy(() -> service.authenticate(
                kakaoProfile(),
                null,
                List.of()
        )).isInstanceOfSatisfying(RestApiException.class, exception ->
                assertThat(exception.getErrorCode().getCode()).isEqualTo("AUTH019")
        );

        verify(socialAccountRepository, never()).findByProviderAndProviderUserId(any(), anyString());
    }

    @Test
    void authenticationRejectsSoftDeletedGuestSession() {
        User deletedGuest = guestWithId(2L);
        deletedGuest.delete();
        when(socialAccountRepository.findByProviderAndProviderUserId(SocialProvider.KAKAO, "12345"))
                .thenReturn(Optional.empty());
        when(userRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(deletedGuest));

        assertThatThrownBy(() -> service.authenticate(
                kakaoProfile(),
                2L,
                List.of(new Consent(1L, true))
        )).isInstanceOfSatisfying(RestApiException.class, exception ->
                assertThat(exception.getErrorCode().getCode()).isEqualTo("AUTH019")
        );

        verify(termRepository, never()).findByActiveTrueOrderByIdAsc();
    }

    @Test
    void authenticationRetryReturnsAlreadyCommittedAccountForSameSignupGuest() {
        User initialGuest = guestWithId(2L);
        User committedGuest = guestWithId(2L);
        committedGuest.upgradeToUser("가입완료", LocalDateTime.now());
        Term requiredTerm = activeTerm(1L, TermType.SERVICE, true);
        SocialAccount account = SocialAccount.create(
                committedGuest,
                SocialProvider.KAKAO,
                "12345",
                null
        );
        when(socialAccountRepository.findByProviderAndProviderUserId(SocialProvider.KAKAO, "12345"))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(account));
        when(userRepository.findByIdForUpdate(2L))
                .thenReturn(Optional.of(initialGuest))
                .thenReturn(Optional.of(committedGuest));
        when(termRepository.findByActiveTrueOrderByIdAsc()).thenReturn(List.of(requiredTerm));
        when(socialAccountRepository.saveAndFlush(any(SocialAccount.class)))
                .thenThrow(new DataIntegrityViolationException("concurrent social account insert"));

        SocialLoginUser result = service.authenticate(
                kakaoProfile(),
                2L,
                List.of(new Consent(1L, true))
        ).user();

        assertThat(result.userId()).isEqualTo(2L);
        assertThat(result.isNewUser()).isFalse();
        verify(termRepository).findByActiveTrueOrderByIdAsc();
        verify(userTermAgreementRepository, never()).saveAll(any());
    }

    @Test
    void unifiedAuthenticationRecoversCommittedAccountImmediatelyAfterConcurrentSignup() {
        User initialGuest = guestWithId(2L);
        User committedUser = guestWithId(2L);
        committedUser.upgradeToUser("가입완료", LocalDateTime.now());
        Term requiredTerm = activeTerm(1L, TermType.SERVICE, true);
        SocialAccount committedAccount = SocialAccount.create(
                committedUser,
                SocialProvider.KAKAO,
                "12345",
                null
        );
        when(socialAccountRepository.findByProviderAndProviderUserId(SocialProvider.KAKAO, "12345"))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(committedAccount));
        when(userRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(initialGuest));
        when(termRepository.findByActiveTrueOrderByIdAsc()).thenReturn(List.of(requiredTerm));
        when(socialAccountRepository.saveAndFlush(any(SocialAccount.class)))
                .thenThrow(new DataIntegrityViolationException("concurrent social account insert"));

        SocialLoginUser result = service.authenticate(
                kakaoProfile(),
                2L,
                List.of(new Consent(1L, true))
        ).user();

        assertThat(result.userId()).isEqualTo(2L);
        assertThat(result.isNewUser()).isFalse();
        verify(userTermAgreementRepository, never()).saveAll(any());
    }

    @Test
    void authenticationRetryUsesConcurrentlyCommittedAccountOwnedByExistingUser() {
        User initialGuest = guestWithId(2L);
        User differentUser = guestWithId(99L);
        differentUser.upgradeToUser("다른회원", LocalDateTime.now());
        Term requiredTerm = activeTerm(1L, TermType.SERVICE, true);
        SocialAccount committedAccount = SocialAccount.create(
                differentUser,
                SocialProvider.KAKAO,
                "12345",
                null
        );
        when(socialAccountRepository.findByProviderAndProviderUserId(SocialProvider.KAKAO, "12345"))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(committedAccount));
        when(userRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(initialGuest));
        when(termRepository.findByActiveTrueOrderByIdAsc()).thenReturn(List.of(requiredTerm));
        when(socialAccountRepository.saveAndFlush(any(SocialAccount.class)))
                .thenThrow(new DataIntegrityViolationException("concurrent social account insert"));

        SocialLoginUser result = service.authenticate(
                kakaoProfile(),
                2L,
                List.of(new Consent(1L, true))
        ).user();

        assertThat(result.userId()).isEqualTo(99L);
        assertThat(result.isNewUser()).isFalse();
        verify(userTermAgreementRepository, never()).saveAll(any());
    }

    @Test
    void unrelatedRegistrationIntegrityFailureIsNotHidden() {
        User guest = guestWithId(2L);
        Term requiredTerm = activeTerm(1L, TermType.SERVICE, true);
        DataIntegrityViolationException databaseFailure =
                new DataIntegrityViolationException("unrelated foreign key failure");
        when(socialAccountRepository.findByProviderAndProviderUserId(SocialProvider.KAKAO, "12345"))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.empty())
                .thenReturn(Optional.empty());
        when(userRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(guest));
        when(termRepository.findByActiveTrueOrderByIdAsc()).thenReturn(List.of(requiredTerm));
        when(socialAccountRepository.saveAndFlush(any(SocialAccount.class)))
                .thenThrow(databaseFailure);

        assertThatThrownBy(() -> service.authenticate(
                kakaoProfile(),
                2L,
                List.of(new Consent(1L, true))
        )).isSameAs(databaseFailure);
    }

    @Test
    void authenticationRejectsMissingRequiredAgreementWithoutUpgradingGuest() {
        User guest = guestWithId(2L);
        Term requiredTerm = activeTerm(1L, TermType.PRIVACY, true);
        when(socialAccountRepository.findByProviderAndProviderUserId(SocialProvider.KAKAO, "12345"))
                .thenReturn(Optional.empty());
        when(userRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(guest));
        when(termRepository.findByActiveTrueOrderByIdAsc()).thenReturn(List.of(requiredTerm));

        assertThatThrownBy(() -> service.authenticate(
                kakaoProfile(),
                2L,
                List.of(new Consent(1L, false))
        )).isInstanceOfSatisfying(RestApiException.class, exception ->
                assertThat(exception.getErrorCode().getCode()).isEqualTo("AUTH024")
        );

        assertThat(guest.isGuest()).isTrue();
        verify(socialAccountRepository, never()).saveAndFlush(any());
        verify(userTermAgreementRepository, never()).saveAll(any());
    }

    @Test
    void authenticationRejectsDuplicateAgreementIds() {
        User guest = guestWithId(2L);
        Term requiredTerm = activeTerm(1L, TermType.SERVICE, true);
        when(socialAccountRepository.findByProviderAndProviderUserId(SocialProvider.KAKAO, "12345"))
                .thenReturn(Optional.empty());
        when(userRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(guest));
        when(termRepository.findByActiveTrueOrderByIdAsc()).thenReturn(List.of(requiredTerm));

        assertThatThrownBy(() -> service.authenticate(
                kakaoProfile(),
                2L,
                List.of(
                        new Consent(1L, true),
                        new Consent(1L, true)
                )
        )).isInstanceOfSatisfying(RestApiException.class, exception ->
                assertThat(exception.getErrorCode().getCode()).isEqualTo("AUTH026")
        );

        assertThat(guest.isGuest()).isTrue();
        verify(socialAccountRepository, never()).saveAndFlush(any());
    }

    private SocialUserProfile kakaoProfile() {
        return new SocialUserProfile(SocialProvider.KAKAO, "12345", null, "카카오유저");
    }

    private Term activeTerm(Long id, TermType termType, boolean required) {
        Term term = Term.builder()
                .termType(termType)
                .title("약관")
                .content("약관 본문")
                .required(required)
                .version("1.0.0")
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();
        ReflectionTestUtils.setField(term, "id", id);
        return term;
    }

    private User guestWithId(Long id) {
        User user = User.createGuest(
                "b8e2b515-76f0-4a6b-a94f-8a85f6b5bc7d",
                "게스트",
                LocalDateTime.now()
        );
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

}
