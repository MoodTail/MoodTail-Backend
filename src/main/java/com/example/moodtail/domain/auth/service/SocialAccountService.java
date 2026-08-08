package com.example.moodtail.domain.auth.service;

import com.example.moodtail.domain.auth.entity.SocialAccount;
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
    private final TokenSessionService tokenSessionService;

    public Optional<SocialAuthenticationResult> loginExisting(SocialUserProfile profile) {
        validateProfile(profile);
        Optional<SocialLoginUser> authenticatedUser = requiresNewTransactionTemplate()
                .execute(status -> loginInTransaction(profile));
        if (authenticatedUser == null) {
            throw new RestApiException(AuthErrorStatus.AUTH_INFRASTRUCTURE_UNAVAILABLE);
        }
        return authenticatedUser.map(this::completeAuthentication);
    }

    public SocialAuthenticationResult register(
            SocialUserProfile profile,
            List<Term> agreedTerms
    ) {
        validateProfile(profile);
        String nickname = AuthNicknameValidator.normalize(profile.nickname());
        if (agreedTerms == null || agreedTerms.isEmpty()) {
            throw new RestApiException(AuthErrorStatus.INVALID_TERM_AGREEMENT);
        }
        SocialUserProfile normalizedProfile = new SocialUserProfile(
                profile.provider(),
                profile.providerUserId(),
                profile.email(),
                nickname
        );
        SocialLoginUser authenticatedUser = registerWithRecovery(normalizedProfile, agreedTerms);
        return completeAuthentication(authenticatedUser);
    }

    private SocialLoginUser registerWithRecovery(
            SocialUserProfile profile,
            List<Term> agreedTerms
    ) {
        TransactionTemplate transactionTemplate = requiresNewTransactionTemplate();
        try {
            SocialLoginUser result = transactionTemplate.execute(status ->
                    loginInTransaction(profile)
                            .orElseGet(() -> registerInTransaction(profile, agreedTerms))
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
        }
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
            List<Term> agreedTerms
    ) {
        LocalDateTime now = LocalDateTime.now();
        User user = userRepository.save(User.createMember(profile.nickname(), now));
        SocialAccount socialAccount = socialAccountRepository.saveAndFlush(SocialAccount.create(
                user,
                profile.provider(),
                profile.providerUserId(),
                profile.email()
        ));
        termAgreementService.recordValidatedAgreements(user, agreedTerms, now);

        return createSocialLoginUser(user, socialAccount, true);
    }

    private void validateProfile(SocialUserProfile profile) {
        if (profile == null
                || profile.provider() == null
                || !StringUtils.hasText(profile.providerUserId())
                || !StringUtils.hasText(profile.email())) {
            throw new RestApiException(AuthErrorStatus.INVALID_SOCIAL_LOGIN);
        }
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

    private SocialAuthenticationResult completeAuthentication(SocialLoginUser user) {
        try {
            return new SocialAuthenticationResult(
                    user,
                    tokenSessionService.issueSession(user.userId(), user.role())
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
