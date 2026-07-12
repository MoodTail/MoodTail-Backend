package com.example.moodtail.domain.user.service;

import com.example.moodtail.domain.user.client.SocialUserProfile;
import com.example.moodtail.domain.term.entity.Term;
import com.example.moodtail.domain.term.entity.TermType;
import com.example.moodtail.domain.term.repository.TermRepository;
import com.example.moodtail.domain.user.entity.SocialAccount;
import com.example.moodtail.domain.user.entity.User;
import com.example.moodtail.domain.user.entity.UserTermAgreement;
import com.example.moodtail.domain.user.enums.SocialProvider;
import com.example.moodtail.domain.user.enums.UserRole;
import com.example.moodtail.domain.user.repository.SocialAccountRepository;
import com.example.moodtail.domain.user.repository.UserRepository;
import com.example.moodtail.domain.user.repository.UserTermAgreementRepository;
import com.example.moodtail.domain.user.support.AuthPropertiesFixtures;
import com.example.moodtail.global.common.exception.RestApiException;
import com.example.moodtail.global.lock.IdentityLockManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.SimpleTransactionStatus;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SocialAccountRegistrationServiceTest {

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
    private IdentityLockManager identityLockManager;

    @Mock
    private GuestDataMergeService guestDataMergeService;

    private SocialAccountRegistrationService service;

    @BeforeEach
    void setUp() {
        service = new SocialAccountRegistrationService(
                transactionManager,
                userRepository,
                socialAccountRepository,
                identityLockManager,
                guestDataMergeService,
                new TermAgreementService(termRepository, userTermAgreementRepository),
                AuthPropertiesFixtures.defaults()
        );
        when(transactionManager.getTransaction(any())).thenReturn(new SimpleTransactionStatus());
        lenient().when(identityLockManager.executeForSocialLogin(any(), anyString(), any()))
                .thenAnswer(invocation -> get(invocation.getArgument(2)));
        lenient().when(identityLockManager.executeForGuestUserId(anyLong(), any()))
                .thenAnswer(invocation -> get(invocation.getArgument(1)));
    }

    @Test
    void registerUpgradesGuestAndStoresRequiredTermAgreement() {
        User guest = guestWithId(2L);
        Term requiredTerm = activeTerm(1L, TermType.SERVICE, true);
        SocialUserProfile profile = kakaoProfile();

        when(socialAccountRepository.findByProviderAndProviderUserId(SocialProvider.KAKAO, "12345"))
                .thenReturn(Optional.empty());
        when(userRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(guest));
        when(termRepository.findByActiveTrueOrderByIdAsc()).thenReturn(List.of(requiredTerm));
        when(socialAccountRepository.saveAndFlush(any(SocialAccount.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        SocialLoginUser result = service.register(
                profile,
                2L,
                List.of(new TermAgreementService.Consent(1L, true))
        );

        assertThat(result.userId()).isEqualTo(2L);
        assertThat(result.role()).isEqualTo(UserRole.USER);
        assertThat(result.socialEmail()).isNull();
        assertThat(result.isNewUser()).isTrue();
        assertThat(guest.getGuestUuid()).isNull();
        assertThat(guest.getRole()).isEqualTo(UserRole.USER);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<UserTermAgreement>> agreementsCaptor = ArgumentCaptor.forClass(List.class);
        verify(userTermAgreementRepository).saveAll(agreementsCaptor.capture());
        assertThat(agreementsCaptor.getValue())
                .singleElement()
                .satisfies(agreement -> {
                    assertThat(agreement.getUser()).isSameAs(guest);
                    assertThat(agreement.getTerm()).isSameAs(requiredTerm);
                });
    }

    @Test
    void loginMergesGuestIntoExistingSocialAccount() {
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
                "ignored"
        );
        when(socialAccountRepository.findByProviderAndProviderUserId(SocialProvider.GOOGLE, "google-id"))
                .thenReturn(Optional.of(account));

        SocialLoginUser result = service.login(profile, 2L);

        assertThat(result.userId()).isEqualTo(99L);
        assertThat(result.isNewUser()).isFalse();
        verify(guestDataMergeService).mergeIntoExistingUser(2L, 99L);
        verify(userTermAgreementRepository, never()).saveAll(any());
    }

    @Test
    void loginRejectsUnregisteredSocialAccount() {
        when(socialAccountRepository.findByProviderAndProviderUserId(SocialProvider.KAKAO, "12345"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.login(kakaoProfile(), 2L))
                .isInstanceOfSatisfying(RestApiException.class, exception ->
                        assertThat(exception.getErrorCode().getCode()).isEqualTo("AUTH023")
                );
    }

    @Test
    void loginRejectsSoftDeletedSocialUser() {
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

        assertThatThrownBy(() -> service.login(kakaoProfile(), 2L))
                .isInstanceOfSatisfying(RestApiException.class, exception ->
                        assertThat(exception.getErrorCode().getCode()).isEqualTo("AUTH020")
                );
    }

    @Test
    void registerRejectsSoftDeletedGuestSession() {
        User deletedGuest = guestWithId(2L);
        deletedGuest.delete();
        when(socialAccountRepository.findByProviderAndProviderUserId(SocialProvider.KAKAO, "12345"))
                .thenReturn(Optional.empty());
        when(userRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(deletedGuest));

        assertThatThrownBy(() -> service.register(
                kakaoProfile(),
                2L,
                List.of(new TermAgreementService.Consent(1L, true))
        )).isInstanceOfSatisfying(RestApiException.class, exception ->
                assertThat(exception.getErrorCode().getCode()).isEqualTo("AUTH019")
        );

        verify(termRepository, never()).findByActiveTrueOrderByIdAsc();
    }

    @Test
    void registerRejectsExistingSocialAccount() {
        User existingUser = guestWithId(99L);
        existingUser.upgradeToUser("기존유저", LocalDateTime.now());
        SocialAccount account = SocialAccount.create(
                existingUser,
                SocialProvider.KAKAO,
                "12345",
                null
        );
        when(socialAccountRepository.findByProviderAndProviderUserId(SocialProvider.KAKAO, "12345"))
                .thenReturn(Optional.of(account));

        assertThatThrownBy(() -> service.register(
                kakaoProfile(),
                2L,
                List.of(new TermAgreementService.Consent(1L, true))
        )).isInstanceOfSatisfying(RestApiException.class, exception ->
                assertThat(exception.getErrorCode().getCode()).isEqualTo("AUTH022")
        );

        verify(userRepository, never()).findByIdForUpdate(2L);
    }

    @Test
    void registerRetryReturnsAlreadyCommittedAccountForSameSignupGuest() {
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
                .thenReturn(Optional.empty(), Optional.of(account));
        when(userRepository.findByIdForUpdate(2L))
                .thenReturn(Optional.of(initialGuest), Optional.of(committedGuest));
        when(termRepository.findByActiveTrueOrderByIdAsc()).thenReturn(List.of(requiredTerm));
        when(socialAccountRepository.saveAndFlush(any(SocialAccount.class)))
                .thenThrow(new DataIntegrityViolationException("concurrent social account insert"));

        SocialLoginUser result = service.register(
                kakaoProfile(),
                2L,
                List.of(new TermAgreementService.Consent(1L, true))
        );

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
                .thenReturn(Optional.empty(), Optional.empty(), Optional.of(committedAccount));
        when(userRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(initialGuest));
        when(termRepository.findByActiveTrueOrderByIdAsc()).thenReturn(List.of(requiredTerm));
        when(socialAccountRepository.saveAndFlush(any(SocialAccount.class)))
                .thenThrow(new DataIntegrityViolationException("concurrent social account insert"));

        SocialLoginUser result = service.authenticateAndComplete(
                kakaoProfile(),
                2L,
                List.of(new TermAgreementService.Consent(1L, true)),
                java.util.function.Function.identity()
        );

        assertThat(result.userId()).isEqualTo(2L);
        assertThat(result.isNewUser()).isFalse();
        verify(guestDataMergeService).mergeIntoExistingUser(2L, 2L);
        verify(userTermAgreementRepository, never()).saveAll(any());
    }

    @Test
    void registerRetryRejectsConcurrentlyCommittedAccountOwnedByDifferentUser() {
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
                .thenReturn(Optional.empty(), Optional.of(committedAccount));
        when(userRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(initialGuest));
        when(termRepository.findByActiveTrueOrderByIdAsc()).thenReturn(List.of(requiredTerm));
        when(socialAccountRepository.saveAndFlush(any(SocialAccount.class)))
                .thenThrow(new DataIntegrityViolationException("concurrent social account insert"));

        assertThatThrownBy(() -> service.register(
                kakaoProfile(),
                2L,
                List.of(new TermAgreementService.Consent(1L, true))
        )).isInstanceOfSatisfying(RestApiException.class, exception ->
                assertThat(exception.getErrorCode().getCode()).isEqualTo("AUTH022")
        );

        verify(userTermAgreementRepository, never()).saveAll(any());
    }

    @Test
    void registerRejectsMissingRequiredAgreementWithoutUpgradingGuest() {
        User guest = guestWithId(2L);
        Term requiredTerm = activeTerm(1L, TermType.PRIVACY, true);
        when(socialAccountRepository.findByProviderAndProviderUserId(SocialProvider.KAKAO, "12345"))
                .thenReturn(Optional.empty());
        when(userRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(guest));
        when(termRepository.findByActiveTrueOrderByIdAsc()).thenReturn(List.of(requiredTerm));

        assertThatThrownBy(() -> service.register(
                kakaoProfile(),
                2L,
                List.of(new TermAgreementService.Consent(1L, false))
        )).isInstanceOfSatisfying(RestApiException.class, exception ->
                assertThat(exception.getErrorCode().getCode()).isEqualTo("AUTH024")
        );

        assertThat(guest.isGuest()).isTrue();
        verify(socialAccountRepository, never()).saveAndFlush(any());
        verify(userTermAgreementRepository, never()).saveAll(any());
    }

    @Test
    void registerRejectsDuplicateAgreementIds() {
        User guest = guestWithId(2L);
        Term requiredTerm = activeTerm(1L, TermType.SERVICE, true);
        when(socialAccountRepository.findByProviderAndProviderUserId(SocialProvider.KAKAO, "12345"))
                .thenReturn(Optional.empty());
        when(userRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(guest));
        when(termRepository.findByActiveTrueOrderByIdAsc()).thenReturn(List.of(requiredTerm));

        assertThatThrownBy(() -> service.register(
                kakaoProfile(),
                2L,
                List.of(
                        new TermAgreementService.Consent(1L, true),
                        new TermAgreementService.Consent(1L, true)
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

    @SuppressWarnings("unchecked")
    private <T> T get(Object supplier) {
        return ((Supplier<T>) supplier).get();
    }
}
