package com.example.moodtail.domain.auth.service;

import com.example.moodtail.domain.user.entity.User;
import com.example.moodtail.domain.user.entity.UserRole;
import com.example.moodtail.domain.user.repository.UserRepository;
import com.example.moodtail.global.auth.config.AuthProperties;
import com.example.moodtail.global.common.exception.RestApiException;
import com.example.moodtail.global.common.exception.code.status.AuthErrorStatus;
import com.example.moodtail.global.common.exception.code.status.GlobalErrorStatus;
import com.example.moodtail.global.lock.IdentityLockManager;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GuestUserRegistrationService {

    public record GuestLoginUser(
            Long userId,
            String guestUuid,
            UserRole role,
            boolean isNewUser
    ) {

        private static GuestLoginUser from(User user, boolean isNewUser) {
            return new GuestLoginUser(
                    user.getId(),
                    user.getGuestUuid(),
                    user.getRole(),
                    isNewUser
            );
        }
    }

    private final UserRepository userRepository;
    private final IdentityLockManager identityLockManager;
    private final PlatformTransactionManager transactionManager;
    private final AuthProperties authProperties;

    public GuestLoginUser findOrCreate(UUID guestUuid) {
        String normalizedGuestUuid = guestUuid.toString();

        try {
            return identityLockManager.executeForGuestUser(
                    normalizedGuestUuid,
                    () -> requiresNewTransactionTemplate().execute(
                            status -> findOrCreateInTransaction(normalizedGuestUuid)
                    )
            );
        } catch (DataIntegrityViolationException e) {
            GuestLoginUser existingGuest = requiresNewTransactionTemplate().execute(
                    status -> userRepository.findByGuestUuidAndRole(normalizedGuestUuid, UserRole.GUEST)
                            .map(this::restoreGuest)
                            .orElse(null)
            );
            if (existingGuest != null) {
                return existingGuest;
            }
            throw new RestApiException(GlobalErrorStatus._INTERNAL_SERVER_ERROR);
        }
    }

    private TransactionTemplate requiresNewTransactionTemplate() {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        return transactionTemplate;
    }

    private GuestLoginUser findOrCreateInTransaction(String guestUuid) {
        return userRepository.findByGuestUuidAndRole(guestUuid, UserRole.GUEST)
                .map(user -> restoreGuest(user))
                .orElseGet(() -> GuestLoginUser.from(createGuestUser(guestUuid), true));
    }

    private GuestLoginUser restoreGuest(User user) {
        if (!user.isActive() || user.isDeleted()) {
            throw new RestApiException(AuthErrorStatus.INACTIVE_USER);
        }
        user.updateLastAccessedAt(LocalDateTime.now());
        return GuestLoginUser.from(user, false);
    }

    private User createGuestUser(String guestUuid) {
        return userRepository.save(User.createGuest(
                guestUuid,
                authProperties.guestLogin().defaultNickname(),
                LocalDateTime.now()
        ));
    }
}
