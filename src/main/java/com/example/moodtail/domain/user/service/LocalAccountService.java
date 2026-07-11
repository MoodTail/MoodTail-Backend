package com.example.moodtail.domain.user.service;

import com.example.moodtail.domain.user.config.LocalAuthProperties;
import com.example.moodtail.domain.term.entity.Term;
import com.example.moodtail.domain.user.entity.LocalAccount;
import com.example.moodtail.domain.user.entity.User;
import com.example.moodtail.domain.user.repository.LocalAccountRepository;
import com.example.moodtail.domain.user.repository.UserRepository;
import com.example.moodtail.global.common.exception.RestApiException;
import com.example.moodtail.global.common.exception.code.status.AuthErrorStatus;
import com.example.moodtail.global.common.exception.code.status.GlobalErrorStatus;
import com.example.moodtail.global.lock.IdentityLockManager;
import lombok.RequiredArgsConstructor;
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
import java.util.function.Function;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
public class LocalAccountService {

    private static final String DUMMY_PASSWORD_HASH =
            "{bcrypt}$2a$10$7EqJtq98hPqEX7fNZaFWoO5u7/PPD.Rr1M7gL3N0vJ0w3PjYVJx5K";

    private final PlatformTransactionManager transactionManager;
    private final LocalAccountRepository localAccountRepository;
    private final UserRepository userRepository;
    private final GuestDataMergeService guestDataMergeService;
    private final TermAgreementService termAgreementService;
    private final IdentityLockManager identityLockManager;
    private final PasswordEncoder passwordEncoder;
    private final LocalAuthProperties properties;

    public <T> T signupAndComplete(
            String email,
            String password,
            String passwordConfirm,
            String nickname,
            List<TermAgreementService.Consent> consents,
            Long guestUserId,
            Function<LocalAuthUser, T> completion
    ) {
        String normalizedEmail = normalizeEmail(email);
        String normalizedNickname = normalizeNickname(nickname);
        validatePassword(password, passwordConfirm);
        String passwordHash = passwordEncoder.encode(password);

        return identityLockManager.executeForLocalEmail(
                normalizedEmail,
                () -> executeWithOptionalGuestLock(
                        guestUserId,
                        () -> requiresNewTransaction().execute(status -> {
                            if (localAccountRepository.existsByEmail(normalizedEmail)) {
                                throw new RestApiException(AuthErrorStatus.LOCAL_ACCOUNT_ALREADY_EXISTS);
                            }
                            List<Term> agreedTerms = termAgreementService.validateAgreements(consents);
                            LocalDateTime now = LocalDateTime.now();
                            User user = createOrUpgradeUser(guestUserId, normalizedNickname, now);
                            LocalAccount account = localAccountRepository.saveAndFlush(
                                    LocalAccount.create(user, normalizedEmail, passwordHash, now)
                            );
                            termAgreementService.recordValidatedAgreements(user, agreedTerms, now);
                            return completion.apply(LocalAuthUser.from(account, true));
                        })
                )
        );
    }

    public <T> T loginAndComplete(
            String email,
            String password,
            Long guestUserId,
            Function<LocalAuthUser, T> completion
    ) {
        String normalizedEmail = normalizeEmail(email);
        return identityLockManager.executeForLocalEmail(
                normalizedEmail,
                () -> executeWithOptionalGuestLock(
                        guestUserId,
                        () -> loginInTransaction(normalizedEmail, password, guestUserId, completion)
                )
        );
    }

    public void changePassword(
            Long localAccountId,
            int expectedPasswordVersion,
            String password,
            String passwordConfirm,
            Consumer<PasswordResetAccount> completion
    ) {
        validatePassword(password, passwordConfirm);
        String passwordHash = passwordEncoder.encode(password);
        requiresNewTransaction().executeWithoutResult(status -> {
            LocalAccount account = localAccountRepository.findByIdForUpdate(localAccountId)
                    .orElseThrow(() -> new RestApiException(AuthErrorStatus.INVALID_PASSWORD_RESET_TOKEN));
            validateActive(account.getUser());
            try {
                account.changePassword(passwordHash, expectedPasswordVersion, LocalDateTime.now());
            } catch (IllegalStateException e) {
                throw new RestApiException(AuthErrorStatus.INVALID_PASSWORD_RESET_TOKEN);
            }
            completion.accept(new PasswordResetAccount(
                    account.getId(),
                    account.getUser().getId(),
                    account.getPasswordVersion(),
                    account.getEmail()
            ));
        });
    }

    public Optional<PasswordResetAccount> findPasswordResetAccount(String email) {
        return localAccountRepository.findByEmail(normalizeEmail(email))
                .filter(account -> account.getUser().isActive() && !account.getUser().isDeleted())
                .map(account -> new PasswordResetAccount(
                        account.getId(),
                        account.getUser().getId(),
                        account.getPasswordVersion(),
                        account.getEmail()
                ));
    }

    private <T> T loginInTransaction(
            String normalizedEmail,
            String password,
            Long guestUserId,
            Function<LocalAuthUser, T> completion
    ) {
        LoginAttempt<T> attempt = requiresNewTransaction().execute(status -> {
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
                return LoginAttempt.failure(AuthErrorStatus.LOCAL_ACCOUNT_LOCKED);
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
            if (guestUserId != null && !guestUserId.equals(user.getId())) {
                guestDataMergeService.mergeIntoExistingUser(guestUserId, user.getId());
            }
            user.updateLastAccessedAt(now);
            return LoginAttempt.success(completion.apply(LocalAuthUser.from(account, false)));
        });
        if (attempt == null) {
            throw new RestApiException(AuthErrorStatus.AUTH_INFRASTRUCTURE_UNAVAILABLE);
        }
        if (attempt.errorStatus() != null) {
            throw new RestApiException(attempt.errorStatus());
        }
        return attempt.result();
    }

    private User createOrUpgradeUser(Long guestUserId, String nickname, LocalDateTime now) {
        if (guestUserId == null) {
            return userRepository.save(User.createMember(nickname, now));
        }
        User guest = userRepository.findByIdForUpdate(guestUserId)
                .orElseThrow(() -> new RestApiException(AuthErrorStatus.INVALID_GUEST_SESSION));
        if (!guest.isGuest() || !guest.isActive() || guest.isDeleted()) {
            throw new RestApiException(AuthErrorStatus.INVALID_GUEST_SESSION);
        }
        guest.upgradeToUser(nickname, now);
        return guest;
    }

    private <T> T executeWithOptionalGuestLock(Long guestUserId, java.util.function.Supplier<T> action) {
        if (guestUserId == null) {
            return action.get();
        }
        return identityLockManager.executeForGuestUserId(guestUserId, action);
    }

    private void validateActive(User user) {
        if (!user.isActive() || user.isDeleted()) {
            throw new RestApiException(AuthErrorStatus.INACTIVE_USER);
        }
    }

    public String normalizeEmail(String email) {
        if (email == null) {
            throw new RestApiException(AuthErrorStatus.INVALID_CREDENTIALS);
        }
        return Normalizer.normalize(email.trim(), Normalizer.Form.NFKC).toLowerCase(Locale.ROOT);
    }

    private String normalizeNickname(String nickname) {
        if (nickname == null || nickname.trim().isEmpty()) {
            throw new RestApiException(GlobalErrorStatus._BAD_REQUEST);
        }
        return nickname.trim();
    }

    void validatePassword(String password, String passwordConfirm) {
        if (password == null || !password.equals(passwordConfirm)) {
            throw new RestApiException(AuthErrorStatus.PASSWORD_CONFIRMATION_MISMATCH);
        }
        int codePointLength = password.codePointCount(0, password.length());
        int byteLength = password.getBytes(StandardCharsets.UTF_8).length;
        if (codePointLength < properties.password().minLength()
                || byteLength > properties.password().maxBytes()) {
            throw new RestApiException(AuthErrorStatus.INVALID_PASSWORD_POLICY);
        }
    }

    private TransactionTemplate requiresNewTransaction() {
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        template.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        return template;
    }

    public record LocalAuthUser(
            Long userId,
            com.example.moodtail.domain.user.enums.UserRole role,
            String email,
            String nickname,
            boolean newUser
    ) {
        static LocalAuthUser from(LocalAccount account, boolean newUser) {
            return new LocalAuthUser(
                    account.getUser().getId(),
                    account.getUser().getRole(),
                    account.getEmail(),
                    account.getUser().getNickname(),
                    newUser
            );
        }
    }

    public record PasswordResetAccount(
            Long localAccountId,
            Long userId,
            int passwordVersion,
            String email
    ) {
    }

    private record LoginAttempt<T>(T result, AuthErrorStatus errorStatus) {
        static <T> LoginAttempt<T> success(T result) {
            return new LoginAttempt<>(result, null);
        }

        static <T> LoginAttempt<T> failure(AuthErrorStatus status) {
            return new LoginAttempt<>(null, status);
        }
    }
}
