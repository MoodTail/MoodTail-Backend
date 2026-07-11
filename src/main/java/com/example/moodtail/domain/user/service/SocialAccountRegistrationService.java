package com.example.moodtail.domain.user.service;

import com.example.moodtail.domain.user.client.SocialUserProfile;
import com.example.moodtail.domain.user.config.AuthProperties;
import com.example.moodtail.domain.term.entity.Term;
import com.example.moodtail.domain.user.entity.SocialAccount;
import com.example.moodtail.domain.user.entity.User;
import com.example.moodtail.domain.user.repository.SocialAccountRepository;
import com.example.moodtail.domain.user.repository.UserRepository;
import com.example.moodtail.global.common.exception.RestApiException;
import com.example.moodtail.global.common.exception.code.status.AuthErrorStatus;
import com.example.moodtail.global.lock.IdentityLockManager;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class SocialAccountRegistrationService {

    private final PlatformTransactionManager transactionManager;
    private final UserRepository userRepository;
    private final SocialAccountRepository socialAccountRepository;
    private final IdentityLockManager identityLockManager;
    private final GuestDataMergeService guestDataMergeService;
    private final TermAgreementService termAgreementService;
    private final AuthProperties authProperties;

    public <T> T authenticateAndComplete(
            SocialUserProfile profile,
            Long guestUserId,
            List<TermAgreementService.Consent> consents,
            Function<SocialLoginUser, T> completion
    ) {
        return identityLockManager.executeForSocialLogin(
                profile.provider(),
                profile.providerUserId(),
                () -> identityLockManager.executeForGuestUserId(
                        guestUserId,
                        () -> authenticateWithRetry(profile, guestUserId, consents, completion)
                )
        );
    }

    private <T> T authenticateWithRetry(
            SocialUserProfile profile,
            Long guestUserId,
            List<TermAgreementService.Consent> consents,
            Function<SocialLoginUser, T> completion
    ) {
        TransactionTemplate transactionTemplate = requiresNewTransactionTemplate();
        for (int attempt = 0; attempt < authProperties.concurrency().socialRegistrationMaxAttempts(); attempt++) {
            try {
                T result = transactionTemplate.execute(status -> {
                    Optional<SocialLoginUser> existing = loginInTransaction(profile, guestUserId);
                    SocialLoginUser user = existing.orElseGet(
                            () -> registerInTransaction(profile, guestUserId, consents)
                    );
                    return completion.apply(user);
                });
                if (result != null) {
                    return result;
                }
            } catch (CannotAcquireLockException | DataIntegrityViolationException ignored) {
                // The next attempt re-reads the unique social identity after the competing transaction.
            }
        }
        throw new RestApiException(AuthErrorStatus.FAILED_SOCIAL_LOGIN);
    }

    public SocialLoginUser login(SocialUserProfile profile, Long guestUserId) {
        return loginAndComplete(profile, guestUserId, Function.identity())
                .orElseThrow(() -> new RestApiException(AuthErrorStatus.SOCIAL_ACCOUNT_NOT_FOUND));
    }

    public <T> Optional<T> loginAndComplete(
            SocialUserProfile profile,
            Long guestUserId,
            Function<SocialLoginUser, T> completion
    ) {
        return identityLockManager.executeForSocialLogin(
                profile.provider(),
                profile.providerUserId(),
                () -> identityLockManager.executeForGuestUserId(
                        guestUserId,
                        () -> requiresNewTransactionTemplate().execute(
                                status -> loginInTransaction(profile, guestUserId).map(completion)
                        )
                )
        );
    }

    public SocialLoginUser register(
            SocialUserProfile profile,
            Long guestUserId,
            List<TermAgreementService.Consent> consents
    ) {
        return registerAndComplete(profile, guestUserId, consents, Function.identity());
    }

    public <T> T registerAndComplete(
            SocialUserProfile profile,
            Long guestUserId,
            List<TermAgreementService.Consent> consents,
            Function<SocialLoginUser, T> completion
    ) {
        return identityLockManager.executeForSocialLogin(
                profile.provider(),
                profile.providerUserId(),
                () -> identityLockManager.executeForGuestUserId(
                        guestUserId,
                        () -> registerWithRetry(profile, guestUserId, consents, completion)
                )
        );
    }

    private <T> T registerWithRetry(
            SocialUserProfile profile,
            Long guestUserId,
            List<TermAgreementService.Consent> consents,
            Function<SocialLoginUser, T> completion
    ) {
        TransactionTemplate transactionTemplate = requiresNewTransactionTemplate();

        for (int attempt = 0; attempt < authProperties.concurrency().socialRegistrationMaxAttempts(); attempt++) {
            try {
                T result = transactionTemplate.execute(
                        status -> completion.apply(registerInTransaction(profile, guestUserId, consents))
                );
                if (result != null) {
                    return result;
                }
            } catch (CannotAcquireLockException | DataIntegrityViolationException e) {
                Boolean alreadyRegistered = transactionTemplate.execute(
                        status -> socialAccountRepository
                                .findByProviderAndProviderUserId(profile.provider(), profile.providerUserId())
                                .isPresent()
                );
                if (Boolean.TRUE.equals(alreadyRegistered)) {
                    throw new RestApiException(AuthErrorStatus.SOCIAL_ACCOUNT_ALREADY_EXISTS);
                }
            }
        }

        throw new RestApiException(AuthErrorStatus.FAILED_SOCIAL_LOGIN);
    }

    private Optional<SocialLoginUser> loginInTransaction(SocialUserProfile profile, Long guestUserId) {
        Optional<SocialAccount> socialAccountOptional = socialAccountRepository
                .findByProviderAndProviderUserId(profile.provider(), profile.providerUserId());
        if (socialAccountOptional.isEmpty()) {
            return Optional.empty();
        }
        SocialAccount socialAccount = socialAccountOptional.get();
        User user = socialAccount.getUser();
        validateActive(user);
        guestDataMergeService.mergeIntoExistingUser(guestUserId, user.getId());
        user.updateLastAccessedAt(LocalDateTime.now());
        return Optional.of(createSocialLoginUser(user, socialAccount, false));
    }

    private SocialLoginUser registerInTransaction(
            SocialUserProfile profile,
            Long guestUserId,
            List<TermAgreementService.Consent> consents
    ) {
        Optional<SocialAccount> existingAccount = socialAccountRepository
                .findByProviderAndProviderUserId(profile.provider(), profile.providerUserId());
        if (existingAccount.isPresent()) {
            SocialAccount account = existingAccount.get();
            if (!account.getUser().getId().equals(guestUserId)) {
                throw new RestApiException(AuthErrorStatus.SOCIAL_ACCOUNT_ALREADY_EXISTS);
            }
            User existingUser = userRepository.findByIdForUpdate(guestUserId)
                    .orElseThrow(() -> new RestApiException(AuthErrorStatus.INVALID_GUEST_SESSION));
            validateActive(existingUser);
            if (existingUser.isGuest()) {
                throw new RestApiException(AuthErrorStatus.INVALID_GUEST_SESSION);
            }
            return createSocialLoginUser(existingUser, account, false);
        }

        User guestUser = userRepository.findByIdForUpdate(guestUserId)
                .orElseThrow(() -> new RestApiException(AuthErrorStatus.INVALID_GUEST_SESSION));
        if (!guestUser.isGuest() || !guestUser.isActive() || guestUser.isDeleted()) {
            throw new RestApiException(AuthErrorStatus.INVALID_GUEST_SESSION);
        }

        List<Term> agreedTerms = termAgreementService.validateAgreements(consents);
        LocalDateTime now = LocalDateTime.now();
        guestUser.upgradeToUser(profile.nickname(), now);
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
        if (!user.isActive() || user.isDeleted()) {
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

}
