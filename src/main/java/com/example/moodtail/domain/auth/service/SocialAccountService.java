package com.example.moodtail.domain.auth.service;

import com.example.moodtail.domain.auth.entity.SocialAccount;
import com.example.moodtail.domain.auth.repository.SocialAccountRepository;
import com.example.moodtail.domain.auth.service.TermAgreementService.Consent;
import com.example.moodtail.domain.term.entity.Term;
import com.example.moodtail.domain.user.entity.User;
import com.example.moodtail.domain.user.entity.UserRole;
import com.example.moodtail.domain.user.repository.UserRepository;
import com.example.moodtail.global.auth.config.AuthProperties;
import com.example.moodtail.global.auth.model.SocialProvider;
import com.example.moodtail.global.auth.model.SocialUserProfile;
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
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SocialAccountService {

    public record SocialLoginUser(
            Long userId,
            UserRole role,
            String nickname,
            SocialProvider provider,
            String socialEmail,
            boolean isNewUser
    ) {
    }

    private final PlatformTransactionManager transactionManager;
    private final UserRepository userRepository;
    private final SocialAccountRepository socialAccountRepository;
    private final IdentityLockManager identityLockManager;
    private final GuestDataMergeService guestDataMergeService;
    private final TermAgreementService termAgreementService;
    private final AuthProperties authProperties;

    public SocialLoginUser authenticate(
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
        return identityLockManager.executeForSocialLogin(
                profile.provider(),
                profile.providerUserId(),
                () -> identityLockManager.executeForGuestUserId(
                        guestUserId,
                        () -> authenticateWithRetry(profile, guestUserId, consents)
                )
        );
    }

    private SocialLoginUser authenticateWithRetry(
            SocialUserProfile profile,
            Long guestUserId,
            List<Consent> consents
    ) {
        TransactionTemplate transactionTemplate = requiresNewTransactionTemplate();
        for (int attempt = 0; attempt < authProperties.concurrency().socialRegistrationMaxAttempts(); attempt++) {
            try {
                SocialLoginUser result = transactionTemplate.execute(status -> {
                    Optional<SocialLoginUser> existing = loginInTransaction(profile, guestUserId);
                    return existing.orElseGet(
                            () -> registerInTransaction(profile, guestUserId, consents)
                    );
                });
                if (result != null) {
                    return result;
                }
            } catch (CannotAcquireLockException | DataIntegrityViolationException ignored) {
                Optional<SocialLoginUser> committedAuthentication = recoverCommittedAuthentication(
                        transactionTemplate,
                        profile,
                        guestUserId
                );
                if (committedAuthentication.isPresent()) {
                    return committedAuthentication.get();
                }
            }
        }
        throw new RestApiException(AuthErrorStatus.FAILED_SOCIAL_LOGIN);
    }

    private Optional<SocialLoginUser> recoverCommittedAuthentication(
            TransactionTemplate transactionTemplate,
            SocialUserProfile profile,
            Long guestUserId
    ) {
        try {
            Optional<SocialLoginUser> result = transactionTemplate.execute(
                    status -> loginInTransaction(profile, guestUserId)
            );
            return result == null ? Optional.empty() : result;
        } catch (CannotAcquireLockException | DataIntegrityViolationException ignored) {
            return Optional.empty();
        }
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
            List<Consent> consents
    ) {
        Optional<SocialAccount> existingAccount = socialAccountRepository
                .findByProviderAndProviderUserId(profile.provider(), profile.providerUserId());
        if (existingAccount.isPresent()) {
            return completeExistingRegistration(existingAccount.get(), guestUserId);
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

    private SocialLoginUser completeExistingRegistration(SocialAccount account, Long guestUserId) {
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
