package com.example.moodtail.domain.user.service;

import com.example.moodtail.domain.user.client.SocialUserProfile;
import com.example.moodtail.domain.user.config.AuthProperties;
import com.example.moodtail.domain.term.entity.Term;
import com.example.moodtail.domain.term.repository.TermRepository;
import com.example.moodtail.domain.user.entity.SocialAccount;
import com.example.moodtail.domain.user.entity.User;
import com.example.moodtail.domain.user.entity.UserTermAgreement;
import com.example.moodtail.domain.user.repository.SocialAccountRepository;
import com.example.moodtail.domain.user.repository.UserRepository;
import com.example.moodtail.domain.user.repository.UserTermAgreementRepository;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SocialAccountRegistrationService {

    private final PlatformTransactionManager transactionManager;
    private final UserRepository userRepository;
    private final SocialAccountRepository socialAccountRepository;
    private final TermRepository termRepository;
    private final UserTermAgreementRepository userTermAgreementRepository;
    private final IdentityLockManager identityLockManager;
    private final AuthProperties authProperties;

    public SocialLoginUser login(SocialUserProfile profile) {
        return identityLockManager.executeForSocialLogin(
                profile.provider(),
                profile.providerUserId(),
                () -> requiresNewTransactionTemplate().execute(status -> loginInTransaction(profile))
        );
    }

    public SocialLoginUser register(
            SocialUserProfile profile,
            Long guestUserId,
            List<TermAgreementConsent> consents
    ) {
        return identityLockManager.executeForSocialLogin(
                profile.provider(),
                profile.providerUserId(),
                () -> identityLockManager.executeForGuestUserId(
                        guestUserId,
                        () -> registerWithRetry(profile, guestUserId, consents)
                )
        );
    }

    private SocialLoginUser registerWithRetry(
            SocialUserProfile profile,
            Long guestUserId,
            List<TermAgreementConsent> consents
    ) {
        TransactionTemplate transactionTemplate = requiresNewTransactionTemplate();

        for (int attempt = 0; attempt < authProperties.concurrency().socialRegistrationMaxAttempts(); attempt++) {
            try {
                SocialLoginUser socialLoginUser = transactionTemplate.execute(
                        status -> registerInTransaction(profile, guestUserId, consents)
                );
                if (socialLoginUser != null) {
                    return socialLoginUser;
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

    private SocialLoginUser loginInTransaction(SocialUserProfile profile) {
        SocialAccount socialAccount = socialAccountRepository
                .findByProviderAndProviderUserId(profile.provider(), profile.providerUserId())
                .orElseThrow(() -> new RestApiException(AuthErrorStatus.SOCIAL_ACCOUNT_NOT_FOUND));
        User user = socialAccount.getUser();
        validateActive(user);
        user.updateLastAccessedAt(LocalDateTime.now());
        return createSocialLoginUser(user, socialAccount, false);
    }

    private SocialLoginUser registerInTransaction(
            SocialUserProfile profile,
            Long guestUserId,
            List<TermAgreementConsent> consents
    ) {
        if (socialAccountRepository
                .findByProviderAndProviderUserId(profile.provider(), profile.providerUserId())
                .isPresent()) {
            throw new RestApiException(AuthErrorStatus.SOCIAL_ACCOUNT_ALREADY_EXISTS);
        }

        User guestUser = userRepository.findByIdForUpdate(guestUserId)
                .orElseThrow(() -> new RestApiException(AuthErrorStatus.INVALID_GUEST_SESSION));
        if (!guestUser.isGuest() || !guestUser.isActive() || guestUser.isDeleted()) {
            throw new RestApiException(AuthErrorStatus.INVALID_GUEST_SESSION);
        }

        List<Term> agreedTerms = validateAgreements(consents);
        LocalDateTime now = LocalDateTime.now();
        guestUser.upgradeToUser(profile.nickname(), now);
        SocialAccount socialAccount = socialAccountRepository.saveAndFlush(SocialAccount.create(
                guestUser,
                profile.provider(),
                profile.providerUserId(),
                profile.email()
        ));
        userTermAgreementRepository.saveAll(agreedTerms.stream()
                .map(term -> UserTermAgreement.create(guestUser, term, now))
                .toList());

        return createSocialLoginUser(guestUser, socialAccount, true);
    }

    private List<Term> validateAgreements(List<TermAgreementConsent> consents) {
        if (consents == null) {
            throw new RestApiException(AuthErrorStatus.INVALID_TERM_AGREEMENT);
        }
        List<Term> activeTerms = termRepository.findByActiveTrueOrderByIdAsc();
        List<Term> requiredTerms = activeTerms.stream().filter(Term::isRequired).toList();
        long activeTermTypeCount = activeTerms.stream().map(Term::getTermType).distinct().count();
        if (requiredTerms.isEmpty() || activeTermTypeCount != activeTerms.size()) {
            throw new RestApiException(AuthErrorStatus.TERMS_CONFIGURATION_ERROR);
        }

        Map<Long, Term> activeTermsById = activeTerms.stream()
                .collect(Collectors.toMap(Term::getId, Function.identity()));
        Map<Long, Boolean> agreementByTermId = new HashMap<>();
        for (TermAgreementConsent consent : consents) {
            if (consent == null || consent.termId() == null
                    || agreementByTermId.putIfAbsent(consent.termId(), consent.agreed()) != null
                    || !activeTermsById.containsKey(consent.termId())) {
                throw new RestApiException(AuthErrorStatus.INVALID_TERM_AGREEMENT);
            }
        }

        boolean missingRequiredAgreement = requiredTerms.stream()
                .anyMatch(term -> !Boolean.TRUE.equals(agreementByTermId.get(term.getId())));
        if (missingRequiredAgreement) {
            throw new RestApiException(AuthErrorStatus.REQUIRED_TERMS_NOT_AGREED);
        }

        return activeTerms.stream()
                .filter(term -> Boolean.TRUE.equals(agreementByTermId.get(term.getId())))
                .toList();
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

    public record TermAgreementConsent(Long termId, boolean agreed) {
    }
}
