package com.example.moodtail.domain.auth.service;

import com.example.moodtail.domain.auth.entity.SocialAccount;
import com.example.moodtail.domain.auth.model.SocialAuthenticationResult;
import com.example.moodtail.domain.auth.repository.SocialAccountRepository;
import com.example.moodtail.domain.term.entity.Term;
import com.example.moodtail.domain.term.entity.TermType;
import com.example.moodtail.domain.term.repository.TermRepository;
import com.example.moodtail.domain.user.entity.User;
import com.example.moodtail.domain.user.entity.UserRole;
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
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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
        lenient().when(tokenSessionService.issueSessionReplacingGuest(anyLong(), any(), any()))
                .thenReturn(new TokenInfo("access", "refresh"));
    }

    @Test
    void existingSocialAccountLogsInWithoutGuestSession() {
        User existingUser = memberWithId(99L, "기존회원");
        SocialAccount account = SocialAccount.create(
                existingUser,
                SocialProvider.GOOGLE,
                "google-id",
                "user@example.com"
        );
        when(socialAccountRepository.findByProviderAndProviderUserId(
                SocialProvider.GOOGLE,
                "google-id"
        )).thenReturn(Optional.of(account));

        Optional<SocialAuthenticationResult> result = service.loginExisting(
                googleProfile(),
                null
        );

        assertThat(result).isPresent();
        assertThat(result.orElseThrow().user().userId()).isEqualTo(99L);
        verify(tokenSessionService).issueSessionReplacingGuest(99L, UserRole.USER, null);
        verify(userRepository, never()).save(any());
    }

    @Test
    void existingSocialAccountOnlyReplacesGuestSession() {
        User existingUser = memberWithId(99L, "기존회원");
        SocialAccount account = SocialAccount.create(
                existingUser,
                SocialProvider.GOOGLE,
                "google-id",
                "user@example.com"
        );
        when(socialAccountRepository.findByProviderAndProviderUserId(
                SocialProvider.GOOGLE,
                "google-id"
        )).thenReturn(Optional.of(account));

        service.loginExisting(googleProfile(), 2L);

        verify(tokenSessionService).issueSessionReplacingGuest(99L, UserRole.USER, 2L);
        verify(userRepository, never()).findByIdForUpdate(2L);
        verify(userRepository, never()).save(any());
    }

    @Test
    void newSocialSignupCreatesFreshMemberAndStoresAgreements() {
        Term requiredTerm = activeTerm(1L, TermType.SERVICE);
        when(socialAccountRepository.findByProviderAndProviderUserId(
                SocialProvider.GOOGLE,
                "google-id"
        )).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            ReflectionTestUtils.setField(user, "id", 100L);
            return user;
        });
        when(socialAccountRepository.saveAndFlush(any(SocialAccount.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        SocialAuthenticationResult result = service.register(
                googleProfile(),
                2L,
                List.of(requiredTerm)
        );

        assertThat(result.user().userId()).isEqualTo(100L);
        assertThat(result.user().isNewUser()).isTrue();
        assertThat(result.user().nickname()).isEqualTo("새회원");
        verify(userRepository, never()).findByIdForUpdate(2L);
        verify(userTermAgreementRepository).saveAll(any());
        InOrder order = inOrder(transactionManager, tokenSessionService);
        order.verify(transactionManager).commit(any());
        order.verify(tokenSessionService).issueSessionReplacingGuest(100L, UserRole.USER, 2L);
    }

    @Test
    void registrationReportsCommittedAccountWhenSessionIssuanceFails() {
        Term requiredTerm = activeTerm(1L, TermType.SERVICE);
        when(socialAccountRepository.findByProviderAndProviderUserId(
                SocialProvider.GOOGLE,
                "google-id"
        )).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            ReflectionTestUtils.setField(user, "id", 100L);
            return user;
        });
        when(socialAccountRepository.saveAndFlush(any(SocialAccount.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(tokenSessionService.issueSessionReplacingGuest(100L, UserRole.USER, 2L))
                .thenThrow(new RestApiException(AuthErrorStatus.AUTH_INFRASTRUCTURE_UNAVAILABLE));

        assertThatThrownBy(() -> service.register(googleProfile(), 2L, List.of(requiredTerm)))
                .isInstanceOfSatisfying(RestApiException.class, exception ->
                        assertThat(exception.getErrorCode().getCode()).isEqualTo("AUTH041")
                );

        InOrder order = inOrder(transactionManager, tokenSessionService);
        order.verify(transactionManager).commit(any());
        order.verify(tokenSessionService).issueSessionReplacingGuest(100L, UserRole.USER, 2L);
    }

    @Test
    void existingAuthenticationRejectsSoftDeletedUser() {
        User deletedUser = memberWithId(99L, "탈퇴회원");
        deletedUser.delete();
        SocialAccount account = SocialAccount.create(
                deletedUser,
                SocialProvider.GOOGLE,
                "google-id",
                "user@example.com"
        );
        when(socialAccountRepository.findByProviderAndProviderUserId(
                SocialProvider.GOOGLE,
                "google-id"
        )).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> service.loginExisting(googleProfile(), null))
                .isInstanceOfSatisfying(RestApiException.class, exception ->
                        assertThat(exception.getErrorCode().getCode()).isEqualTo("AUTH020")
                );
    }

    @Test
    void registrationRecoversAccountCommittedByConcurrentRequest() {
        Term requiredTerm = activeTerm(1L, TermType.SERVICE);
        User committedUser = memberWithId(99L, "가입완료");
        SocialAccount committedAccount = SocialAccount.create(
                committedUser,
                SocialProvider.GOOGLE,
                "google-id",
                "user@example.com"
        );
        when(socialAccountRepository.findByProviderAndProviderUserId(
                SocialProvider.GOOGLE,
                "google-id"
        )).thenReturn(Optional.empty()).thenReturn(Optional.of(committedAccount));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            ReflectionTestUtils.setField(user, "id", 100L);
            return user;
        });
        when(socialAccountRepository.saveAndFlush(any(SocialAccount.class)))
                .thenThrow(new DataIntegrityViolationException("concurrent insert"));

        SocialAuthenticationResult result = service.register(
                googleProfile(),
                null,
                List.of(requiredTerm)
        );

        assertThat(result.user().userId()).isEqualTo(99L);
        assertThat(result.user().isNewUser()).isFalse();
        verify(tokenSessionService).issueSessionReplacingGuest(99L, UserRole.USER, null);
    }

    @Test
    void unrelatedRegistrationIntegrityFailureIsNotHidden() {
        Term requiredTerm = activeTerm(1L, TermType.SERVICE);
        DataIntegrityViolationException databaseFailure =
                new DataIntegrityViolationException("foreign key failure");
        when(socialAccountRepository.findByProviderAndProviderUserId(
                SocialProvider.GOOGLE,
                "google-id"
        )).thenReturn(Optional.empty()).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            ReflectionTestUtils.setField(user, "id", 100L);
            return user;
        });
        when(socialAccountRepository.saveAndFlush(any(SocialAccount.class)))
                .thenThrow(databaseFailure);

        assertThatThrownBy(() -> service.register(
                googleProfile(),
                null,
                List.of(requiredTerm)
        )).isSameAs(databaseFailure);
    }

    private SocialUserProfile googleProfile() {
        return new SocialUserProfile(
                SocialProvider.GOOGLE,
                "google-id",
                "user@example.com",
                "새회원"
        );
    }

    private Term activeTerm(Long id, TermType termType) {
        Term term = Term.builder()
                .termType(termType)
                .title("약관")
                .content("약관 본문")
                .required(true)
                .version("1.0.0")
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();
        ReflectionTestUtils.setField(term, "id", id);
        return term;
    }

    private User memberWithId(Long id, String nickname) {
        User user = User.createMember(nickname, LocalDateTime.now());
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }
}
