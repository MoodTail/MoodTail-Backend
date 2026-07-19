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

    private static final int REGISTRATION_MAX_ATTEMPTS = 3;

    private final PlatformTransactionManager transactionManager;
    private final UserRepository userRepository;
    private final SocialAccountRepository socialAccountRepository;
    private final TermAgreementService termAgreementService;
    private final TokenSessionService tokenSessionService;

    public SocialAuthenticationResult authenticate(
            SocialUserProfile profile,
            Long guestUserId,
            List<Consent> consents
    ) {
        if (guestUserId == null) {
            throw new RestApiException(AuthErrorStatus.INVALID_GUEST_SESSION);
        }
        if (profile == null || profile.provider() == null || !StringUtils.hasText(profile.providerUserId())) {
            throw new RestApiException(AuthErrorStatus.INVALID_SOCIAL_LOGIN);
        }
        SocialLoginUser authenticatedUser = authenticateInTransaction(profile, guestUserId, consents);
        return completeAuthentication(authenticatedUser, guestUserId);
    }

    private SocialLoginUser authenticateInTransaction(
            SocialUserProfile profile,
            Long guestUserId,
            List<Consent> consents
    ) {
        TransactionTemplate transactionTemplate = requiresNewTransactionTemplate();
        for (int attempt = 0; attempt < REGISTRATION_MAX_ATTEMPTS; attempt++) {
            try {
                SocialLoginUser result = transactionTemplate.execute(status ->
                        loginInTransaction(profile).orElseGet(
                                () -> registerInTransaction(profile, guestUserId, consents)
                        )
                );
                if (result == null) {
                    throw new RestApiException(AuthErrorStatus.AUTH_INFRASTRUCTURE_UNAVAILABLE);
                }
                return result;
            } catch (DataIntegrityViolationException exception) {
                Optional<SocialLoginUser> committedAuthentication =
                        recoverCommittedRegistration(transactionTemplate, profile);
                if (committedAuthentication.isPresent()) {
                    return committedAuthentication.get();
                }
                throw exception;
            } catch (CannotAcquireLockException exception) {
                if (attempt + 1 == REGISTRATION_MAX_ATTEMPTS) {
                    throw exception;
                }
            }
        }
        throw new IllegalStateException("Social authentication retry loop completed unexpectedly");
    }

    private Optional<SocialLoginUser> recoverCommittedRegistration(
            TransactionTemplate transactionTemplate,
            SocialUserProfile profile
    ) {
        try {
            Optional<SocialLoginUser> result = transactionTemplate.execute(
                    status -> loginInTransaction(profile)
            );
            return result == null ? Optional.empty() : result;
        } catch (CannotAcquireLockException | DataIntegrityViolationException ignored) {
            return Optional.empty();
        }
    }

    private Optional<SocialLoginUser> loginInTransaction(SocialUserProfile profile) {
        Optional<SocialAccount> socialAccountOptional = socialAccountRepository
                .findByProviderAndProviderUserId(profile.provider(), profile.providerUserId());
        if (socialAccountOptional.isEmpty()) {
            return Optional.empty();
        }
        SocialAccount socialAccount = socialAccountOptional.get();
        User user = socialAccount.getUser();
        validateActive(user);
        user.updateLastAccessedAt(LocalDateTime.now());
        return Optional.of(createSocialLoginUser(user, socialAccount, false));
    }

    private SocialLoginUser registerInTransaction(
            SocialUserProfile profile,
            Long guestUserId,
            List<Consent> consents
    ) {
        Optional<SocialAccount> existingAccount = socialAccountRepository
                .findByProviderAndProviderUserId(profile.provider(), profile.providerUserId());
        if (existingAccount.isPresent()) {
            SocialAccount account = existingAccount.get();
            User user = account.getUser();
            validateActive(user);
            user.updateLastAccessedAt(LocalDateTime.now());
            return createSocialLoginUser(user, account, false);
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

}
