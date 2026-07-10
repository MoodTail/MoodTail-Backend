package com.example.moodtail.domain.user.service;

import com.example.moodtail.domain.user.client.SocialUserProfile;
import com.example.moodtail.domain.user.entity.SocialAccount;
import com.example.moodtail.domain.user.entity.Term;
import com.example.moodtail.domain.user.entity.User;
import com.example.moodtail.domain.user.entity.UserTermAgreement;
import com.example.moodtail.domain.user.enums.SocialProvider;
import com.example.moodtail.domain.user.enums.TermType;
import com.example.moodtail.domain.user.enums.UserRole;
import com.example.moodtail.domain.user.repository.SocialAccountRepository;
import com.example.moodtail.domain.user.repository.TermRepository;
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

    private SocialAccountRegistrationService service;

    @BeforeEach
    void setUp() {
        service = new SocialAccountRegistrationService(
                transactionManager,
                userRepository,
                socialAccountRepository,
                termRepository,
                userTermAgreementRepository,
                identityLockManager,
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
        when(termRepository.findAllByActiveTrue()).thenReturn(List.of(requiredTerm));
        when(socialAccountRepository.saveAndFlush(any(SocialAccount.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        SocialLoginUser result = service.register(
                profile,
                2L,
                List.of(new SocialAccountRegistrationService.TermAgreementConsent(1L, true))
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
    void loginRejectsUnregisteredSocialAccount() {
        when(socialAccountRepository.findByProviderAndProviderUserId(SocialProvider.KAKAO, "12345"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.login(kakaoProfile()))
                .isInstanceOfSatisfying(RestApiException.class, exception ->
                        assertThat(exception.getErrorCode().getCode()).isEqualTo("AUTH023")
                );
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
                List.of(new SocialAccountRegistrationService.TermAgreementConsent(1L, true))
        )).isInstanceOfSatisfying(RestApiException.class, exception ->
                assertThat(exception.getErrorCode().getCode()).isEqualTo("AUTH022")
        );

        verify(userRepository, never()).findByIdForUpdate(anyLong());
    }

    @Test
    void registerRejectsMissingRequiredAgreementWithoutUpgradingGuest() {
        User guest = guestWithId(2L);
        Term requiredTerm = activeTerm(1L, TermType.PRIVACY, true);
        when(socialAccountRepository.findByProviderAndProviderUserId(SocialProvider.KAKAO, "12345"))
                .thenReturn(Optional.empty());
        when(userRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(guest));
        when(termRepository.findAllByActiveTrue()).thenReturn(List.of(requiredTerm));

        assertThatThrownBy(() -> service.register(
                kakaoProfile(),
                2L,
                List.of(new SocialAccountRegistrationService.TermAgreementConsent(1L, false))
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
        when(termRepository.findAllByActiveTrue()).thenReturn(List.of(requiredTerm));

        assertThatThrownBy(() -> service.register(
                kakaoProfile(),
                2L,
                List.of(
                        new SocialAccountRegistrationService.TermAgreementConsent(1L, true),
                        new SocialAccountRegistrationService.TermAgreementConsent(1L, true)
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
        Term term = Term.create(termType, "약관", "약관 본문", required, "1.0.0", true);
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
