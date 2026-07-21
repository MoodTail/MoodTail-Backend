package com.example.moodtail.domain.auth.service;

import com.example.moodtail.domain.auth.model.GuestLoginUser;
import com.example.moodtail.domain.user.entity.User;
import com.example.moodtail.domain.user.entity.UserRole;
import com.example.moodtail.domain.user.repository.UserRepository;
import com.example.moodtail.global.auth.config.AuthProperties;
import com.example.moodtail.global.common.exception.RestApiException;
import com.example.moodtail.global.common.exception.code.status.AuthErrorStatus;
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
public class GuestUserService {

    private final UserRepository userRepository;
    private final PlatformTransactionManager transactionManager;
    private final AuthProperties authProperties;

    public GuestLoginUser findOrCreate(UUID guestUuid) {
        String normalizedGuestUuid = guestUuid.toString();

        try {
            GuestLoginUser guest = requiresNewTransactionTemplate().execute(
                    status -> findOrCreateInTransaction(normalizedGuestUuid)
            );
            if (guest == null) {
                throw new RestApiException(AuthErrorStatus.AUTH_INFRASTRUCTURE_UNAVAILABLE);
            }
            return guest;
        } catch (DataIntegrityViolationException exception) {
            GuestLoginUser existingGuest = requiresNewTransactionTemplate().execute(
                    status -> userRepository.findByGuestUuidAndRole(normalizedGuestUuid, UserRole.GUEST)
                            .map(this::reuseOrReplaceGuest)
                            .orElse(null)
            );
            if (existingGuest != null) {
                return existingGuest;
            }
            throw exception;
        }
    }

    private TransactionTemplate requiresNewTransactionTemplate() {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        return transactionTemplate;
    }

    private GuestLoginUser findOrCreateInTransaction(String guestUuid) {
        return userRepository.findByGuestUuidAndRole(guestUuid, UserRole.GUEST)
                .map(this::reuseOrReplaceGuest)
                .orElseGet(() -> GuestLoginUser.from(createGuestUser(guestUuid), true));
    }

    private GuestLoginUser reuseOrReplaceGuest(User user) {
        if (user.isAvailableForAuthentication()) {
            user.updateLastAccessedAt(LocalDateTime.now());
            return GuestLoginUser.from(user, false);
        }

        String guestUuid = user.getGuestUuid();
        userRepository.delete(user);
        userRepository.flush();
        return GuestLoginUser.from(createGuestUser(guestUuid), true);
    }

    private User createGuestUser(String guestUuid) {
        return userRepository.save(User.createGuest(
                guestUuid,
                authProperties.guestLogin().defaultNickname(),
                LocalDateTime.now()
        ));
    }
}
