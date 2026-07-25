package com.example.moodtail.domain.auth.service;

import com.example.moodtail.domain.auth.entity.SocialAccount;
import com.example.moodtail.domain.auth.model.Consent;
import com.example.moodtail.domain.auth.model.SocialAuthenticationResult;
import com.example.moodtail.domain.auth.model.SocialLoginUser;
import com.example.moodtail.domain.auth.repository.SocialAccountRepository;
import com.example.moodtail.domain.auth.validator.AuthNicknameValidator;
import com.example.moodtail.domain.term.entity.Term;
import com.example.moodtail.domain.user.entity.User;
import com.example.moodtail.domain.user.repository.UserRepository;
import com.example.moodtail.global.auth.model.SocialUserProfile;
import com.example.moodtail.global.common.exception.RestApiException;
import com.example.moodtail.global.common.exception.code.status.AuthErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SocialAccountService {

    private final PlatformTransactionManager transactionManager;
    private final UserRepository userRepository;
    private final SocialAccountRepository socialAccountRepository;
    private final TermAgreementService termAgreementService;
    private final GuestDataTransferService guestDataTransferService;
    private final TokenSessionService tokenSessionService;

    public SocialAuthenticationResult authenticate(
            SocialUserProfile profile,
            Long guestUserId,
            List<Consent> consents
    ) {
        if (profile == null || profile.provider() == null || !StringUtils.hasText(profile.providerUserId())) {
            throw new RestApiException(AuthErrorStatus.INVALID_SOCIAL_LOGIN);
        }
        SocialAuthenticationAttempt attempt =
                authenticateInTransaction(profile, guestUserId, consents);
        return completeAuthentication(attempt.user(), attempt.replacedGuestUserId());
    }

    private SocialAuthenticationAttempt authenticateInTransaction(
            SocialUserProfile profile,
            Long guestUserId,
            List<Consent> consents
    ) {
        TransactionTemplate transactionTemplate = requiresNewTransactionTemplate();
        try {
            SocialAuthenticationAttempt result = transactionTemplate.execute(status ->
                    loginInTransaction(profile, guestUserId).orElseGet(
                            () -> new SocialAuthenticationAttempt(
                                    registerInTransaction(profile, guestUserId, consents),
                                    guestUserId
                            )
                    )
            );
            if (result == null) {
                throw new RestApiException(AuthErrorStatus.AUTH_INFRASTRUCTURE_UNAVAILABLE);
            }
            return result;
        } catch (DataIntegrityViolationException exception) {
            Optional<SocialAuthenticationAttempt> committedAuthentication =
                    recoverCommittedRegistration(transactionTemplate, profile, guestUserId);
            if (committedAuthentication.isPresent()) {
                return committedAuthentication.get();
            }
            throw exception;
        }
    }

    private Optional<SocialAuthenticationAttempt> recoverCommittedRegistration(
            TransactionTemplate transactionTemplate,
            SocialUserProfile profile,
            Long guestUserId
    ) {
        try {
            Optional<SocialAuthenticationAttempt> result = transactionTemplate.execute(
                    status -> loginInTransaction(profile, guestUserId)
            );
            return result == null ? Optional.empty() : result;
        } catch (CannotAcquireLockException | DataIntegrityViolationException ignored) {
            return Optional.empty();
        }
    }

    private Optional<SocialAuthenticationAttempt> loginInTransaction(
            SocialUserProfile profile,
            Long guestUserId
    ) {
        Optional<SocialAccount> socialAccountOptional = socialAccountRepository
                .findByProviderAndProviderUserId(profile.provider(), profile.providerUserId());
        if (socialAccountOptional.isEmpty()) {
            return Optional.empty();
        }
        SocialAccount socialAccount = socialAccountOptional.get();
        User user = socialAccount.getUser();
        validateActive(user);
        boolean transferredGuest = guestDataTransferService
                .transferToExistingUserIfActiveGuest(guestUserId, user.getId());
        user.updateLastAccessedAt(LocalDateTime.now());
        return Optional.of(new SocialAuthenticationAttempt(
                createSocialLoginUser(user, socialAccount, false),
                transferredGuest ? guestUserId : null
        ));
    }

    private SocialLoginUser registerInTransaction(
            SocialUserProfile profile,
            Long guestUserId,
            List<Consent> consents
    ) {
        if (guestUserId == null) {
            throw new RestApiException(AuthErrorStatus.INVALID_GUEST_SESSION);
        }
        User guestUser = userRepository.findByIdForUpdate(guestUserId)
                .orElseThrow(() -> new RestApiException(AuthErrorStatus.INVALID_GUEST_SESSION));
        if (!guestUser.isGuest() || !guestUser.isAvailableForAuthentication()) {
            throw new RestApiException(AuthErrorStatus.INVALID_GUEST_SESSION);
        }

        String nickname = AuthNicknameValidator.normalize(profile.nickname());
        List<Term> agreedTerms = termAgreementService.validateAgreements(consents);
        LocalDateTime now = LocalDateTime.now();
        guestUser.upgradeToUser(nickname, now);
        SocialAccount socialAccount = socialAccountRepository.saveAndFlush(SocialAccount.create(
                guestUser,
                profile.provider(),
                profile.providerUserId(),
                profile.email()
        ));
        termAgreementService.recordValidatedAgreements(guestUser, agreedTerms, now);

        return createSocialLoginUser(guestUser, socialAccount, true);
    }

    private void validateActive(User user) {
        if (!user.isAvailableForAuthentication()) {
            throw new RestApiException(AuthErrorStatus.INACTIVE_USER);
        }
    }

    private TransactionTemplate requiresNewTransactionTemplate() {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        return transactionTemplate;
    }

    private SocialLoginUser createSocialLoginUser(
            User user,
            SocialAccount socialAccount,
            boolean isNewUser
    ) {
        return new SocialLoginUser(
                user.getId(),
                user.getRole(),
                user.getNickname(),
                socialAccount.getProvider(),
                socialAccount.getEmail(),
                isNewUser
        );
    }

    private SocialAuthenticationResult completeAuthentication(SocialLoginUser user, Long guestUserId) {
        try {
            return new SocialAuthenticationResult(
                    user,
                    tokenSessionService.issueSessionReplacingGuest(
                        user.userId(),
                        user.role(),
                        guestUserId
                    )
            );
        } catch (RestApiException exception) {
            if (AuthErrorStatus.AUTH_INFRASTRUCTURE_UNAVAILABLE.getCode().getCode()
                    .equals(exception.getErrorCode().getCode())) {
                throw new RestApiException(AuthErrorStatus.AUTH_SESSION_ISSUE_FAILED);
            }
            throw exception;
        }
    }

    private record SocialAuthenticationAttempt(
            SocialLoginUser user,
            Long replacedGuestUserId
    ) {
    }

}
