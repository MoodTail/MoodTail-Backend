package com.example.moodtail.domain.auth.service;

import com.example.moodtail.domain.auth.entity.LocalAccount;
import com.example.moodtail.domain.auth.model.Consent;
import com.example.moodtail.domain.auth.model.LocalAuthenticationResult;
import com.example.moodtail.domain.auth.model.LocalAuthUser;
import com.example.moodtail.domain.auth.model.PasswordResetAccount;
import com.example.moodtail.domain.auth.repository.LocalAccountRepository;
import com.example.moodtail.domain.term.entity.Term;
import com.example.moodtail.domain.user.entity.User;
import com.example.moodtail.domain.user.repository.UserRepository;
import com.example.moodtail.domain.user.validator.NicknameValidator;
import com.example.moodtail.global.auth.config.LocalAuthProperties;
import com.example.moodtail.global.common.exception.RestApiException;
import com.example.moodtail.global.common.exception.code.status.AuthErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LocalAccountService {

    private static final String DUMMY_PASSWORD_HASH =
            "{bcrypt}$2a$10$7EqJtq98hPqEX7fNZaFWoO5u7/PPD.Rr1M7gL3N0vJ0w3PjYVJx5K";

    private final PlatformTransactionManager transactionManager;
    private final LocalAccountRepository localAccountRepository;
    private final UserRepository userRepository;
    private final TermAgreementService termAgreementService;
    private final PasswordEncoder passwordEncoder;
    private final LocalAuthProperties properties;
    private final TokenSessionService tokenSessionService;

    public LocalAuthenticationResult signup(
            String email,
            String password,
            String passwordConfirm,
            String nickname,
            List<Consent> consents
    ) {
        String normalizedEmail = normalizeEmail(email);
        String normalizedNickname = normalizeNickname(nickname);
        validatePassword(password, passwordConfirm);
        String passwordHash = passwordEncoder.encode(password);

        LocalAuthUser authenticatedUser;
        try {
            authenticatedUser = requiresNewTransaction().execute(status -> {
                if (localAccountRepository.existsByEmail(normalizedEmail)) {
                    throw new RestApiException(AuthErrorStatus.LOCAL_ACCOUNT_ALREADY_EXISTS);
                }
                List<Term> agreedTerms = termAgreementService.validateAgreements(consents);
                LocalDateTime now = LocalDateTime.now();
                User user = userRepository.save(User.createMember(normalizedNickname, now));
                LocalAccount account = localAccountRepository.saveAndFlush(
                        LocalAccount.create(user, normalizedEmail, passwordHash, now)
                );
                termAgreementService.recordValidatedAgreements(user, agreedTerms, now);
                return LocalAuthUser.from(account);
            });
        } catch (DataIntegrityViolationException exception) {
            if (localAccountRepository.existsByEmail(normalizedEmail)) {
                throw new RestApiException(AuthErrorStatus.LOCAL_ACCOUNT_ALREADY_EXISTS);
            }
            throw exception;
        }
        if (authenticatedUser == null) {
            throw new RestApiException(AuthErrorStatus.AUTH_INFRASTRUCTURE_UNAVAILABLE);
        }
        return completeAuthentication(authenticatedUser);
    }

    public LocalAuthenticationResult login(
            String email,
            String password
    ) {
        String normalizedEmail = normalizeEmail(email);
        validateLoginPasswordInput(password);
        LocalAuthUser authenticatedUser = loginInTransaction(normalizedEmail, password);
        return completeAuthentication(authenticatedUser);
    }

    public void changePassword(
            Long localAccountId,
            int expectedPasswordVersion,
            String password,
            String passwordConfirm
    ) {
        validatePassword(password, passwordConfirm);
        String passwordHash = passwordEncoder.encode(password);
        Long userId = requiresNewTransaction().execute(status -> {
            LocalAccount account = localAccountRepository.findByIdForUpdate(localAccountId)
                    .orElseThrow(() -> new RestApiException(AuthErrorStatus.INVALID_PASSWORD_RESET_TOKEN));
            validateActive(account.getUser());
            if (account.getPasswordVersion() == expectedPasswordVersion) {
                account.changePassword(passwordHash, expectedPasswordVersion, LocalDateTime.now());
            } else if (!isRetriedPasswordChange(account, expectedPasswordVersion, password)) {
                throw new RestApiException(AuthErrorStatus.INVALID_PASSWORD_RESET_TOKEN);
            }
            return account.getUser().getId();
        });
        if (userId == null) {
            throw new RestApiException(AuthErrorStatus.AUTH_INFRASTRUCTURE_UNAVAILABLE);
        }
        try {
            tokenSessionService.revokeSession(userId);
        } catch (DataAccessException exception) {
            throw new RestApiException(AuthErrorStatus.PASSWORD_CHANGED_SESSION_REVOCATION_FAILED);
        } catch (RestApiException exception) {
            if (isAuthInfrastructureFailure(exception)) {
                throw new RestApiException(AuthErrorStatus.PASSWORD_CHANGED_SESSION_REVOCATION_FAILED);
            }
            throw exception;
        }
    }

    public Optional<PasswordResetAccount> findPasswordResetAccount(String email) {
        return localAccountRepository.findByEmail(normalizeEmail(email))
                .filter(account -> account.getUser().isAvailableForAuthentication())
                .map(account -> new PasswordResetAccount(
                        account.getId(),
                        account.getPasswordVersion(),
                        account.getEmail()
                ));
    }

    boolean isNormalizedEmailAvailable(String normalizedEmail) {
        return !localAccountRepository.existsByEmail(normalizedEmail);
    }

    private LocalAuthUser loginInTransaction(
            String normalizedEmail,
            String password
    ) {
        LoginAttempt attempt = requiresNewTransaction().execute(status -> {
            Optional<LocalAccount> accountOptional = localAccountRepository.findByEmailForUpdate(normalizedEmail);
            if (accountOptional.isEmpty()) {
                passwordEncoder.matches(password, DUMMY_PASSWORD_HASH);
                return LoginAttempt.failure(AuthErrorStatus.INVALID_CREDENTIALS);
            }

            LocalAccount account = accountOptional.get();
            User user = account.getUser();
            validateActive(user);
            LocalDateTime now = LocalDateTime.now();
            if (account.isLocked(now)) {
                return LoginAttempt.failure(AuthErrorStatus.INVALID_CREDENTIALS);
            }
            if (!passwordEncoder.matches(password, account.getPasswordHash())) {
                Duration lockDuration = Duration.ofMillis(properties.login().lockDurationMillis());
                account.registerFailedLogin(
                        properties.login().maxFailedAttempts(),
                        now.plus(lockDuration)
                );
                return LoginAttempt.failure(AuthErrorStatus.INVALID_CREDENTIALS);
            }

            account.clearLoginFailures();
            user.updateLastAccessedAt(now);
            LocalAuthUser authenticatedUser = LocalAuthUser.from(account);
            return LoginAttempt.success(authenticatedUser);
        });
        if (attempt == null) {
            throw new RestApiException(AuthErrorStatus.AUTH_INFRASTRUCTURE_UNAVAILABLE);
        }
        if (attempt.errorStatus() != null) {
            throw new RestApiException(attempt.errorStatus());
        }
        return attempt.user();
    }

    private LocalAuthenticationResult completeAuthentication(LocalAuthUser user) {
        try {
            return new LocalAuthenticationResult(
                    user,
                    tokenSessionService.issueSession(user.userId(), user.role())
            );
        } catch (RestApiException exception) {
            if (isAuthInfrastructureFailure(exception)) {
                throw new RestApiException(AuthErrorStatus.AUTH_SESSION_ISSUE_FAILED);
            }
            throw exception;
        }
    }

    private boolean isAuthInfrastructureFailure(RestApiException exception) {
        return AuthErrorStatus.AUTH_INFRASTRUCTURE_UNAVAILABLE.getCode().getCode()
                .equals(exception.getErrorCode().getCode());
    }

    private boolean isRetriedPasswordChange(
            LocalAccount account,
            int expectedPasswordVersion,
            String requestedPassword
    ) {
        return (long) account.getPasswordVersion() == (long) expectedPasswordVersion + 1L
                && passwordEncoder.matches(requestedPassword, account.getPasswordHash());
    }

    private void validateActive(User user) {
        if (!user.isAvailableForAuthentication()) {
            throw new RestApiException(AuthErrorStatus.INACTIVE_USER);
        }
    }

    public String normalizeEmail(String email) {
        if (email == null) {
            throw new RestApiException(AuthErrorStatus.INVALID_CREDENTIALS);
        }
        String normalized = Normalizer.normalize(email.trim(), Normalizer.Form.NFKC).toLowerCase(Locale.ROOT);
        if (normalized.isBlank() || normalized.length() > 320) {
            throw new RestApiException(AuthErrorStatus.INVALID_CREDENTIALS);
        }
        return normalized;
    }

    private String normalizeNickname(String nickname) {
        return NicknameValidator.normalize(nickname);
    }

    public void validatePassword(String password, String passwordConfirm) {
        if (password == null || !password.equals(passwordConfirm)) {
            throw new RestApiException(AuthErrorStatus.PASSWORD_CONFIRMATION_MISMATCH);
        }
        int codePointLength = password.codePointCount(0, password.length());
        int byteLength = password.getBytes(StandardCharsets.UTF_8).length;
        if (codePointLength < properties.password().minLength()
                || byteLength > properties.password().maxBytes()
                || password.codePoints().noneMatch(Character::isLetter)
                || password.codePoints().noneMatch(Character::isDigit)) {
            throw new RestApiException(AuthErrorStatus.INVALID_PASSWORD_POLICY);
        }
    }

    private void validateLoginPasswordInput(String password) {
        if (password == null || password.getBytes(StandardCharsets.UTF_8).length > properties.password().maxBytes()) {
            passwordEncoder.matches("invalid-password", DUMMY_PASSWORD_HASH);
            throw new RestApiException(AuthErrorStatus.INVALID_CREDENTIALS);
        }
    }

    private TransactionTemplate requiresNewTransaction() {
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        template.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        return template;
    }

    private record LoginAttempt(LocalAuthUser user, AuthErrorStatus errorStatus) {
        static LoginAttempt success(LocalAuthUser user) {
            return new LoginAttempt(user, null);
        }

        static LoginAttempt failure(AuthErrorStatus status) {
            return new LoginAttempt(null, status);
        }
    }
}
