package com.example.moodtail.domain.auth.service;

import com.example.moodtail.domain.auth.repository.GuestDataMergeRepository;
import com.example.moodtail.domain.user.entity.User;
import com.example.moodtail.domain.user.repository.UserRepository;
import com.example.moodtail.global.common.exception.RestApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static com.example.moodtail.global.common.exception.code.status.AuthErrorStatus.INACTIVE_USER;
import static com.example.moodtail.global.common.exception.code.status.AuthErrorStatus.INVALID_GUEST_SESSION;
import static com.example.moodtail.global.common.exception.code.status.AuthErrorStatus.USER_NOT_FOUND;

@Slf4j
@Service
@RequiredArgsConstructor
public class GuestDataMergeService {

    private final UserRepository userRepository;
    private final GuestDataMergeRepository mergeRepository;

    @Transactional(propagation = Propagation.MANDATORY)
    public void mergeIntoExistingUser(Long guestUserId, Long targetUserId) {
        if (guestUserId == null || targetUserId == null) {
            throw new RestApiException(INVALID_GUEST_SESSION);
        }
        if (guestUserId.equals(targetUserId)) {
            User targetUser = lockUser(targetUserId);
            if (targetUser.isGuest() || !targetUser.isActive() || targetUser.isDeleted()) {
                throw new RestApiException(INACTIVE_USER);
            }
            return;
        }

        User firstLockedUser = lockUser(Math.min(guestUserId, targetUserId));
        User secondLockedUser = lockUser(Math.max(guestUserId, targetUserId));
        User guestUser = firstLockedUser.getId().equals(guestUserId) ? firstLockedUser : secondLockedUser;
        User targetUser = firstLockedUser.getId().equals(targetUserId) ? firstLockedUser : secondLockedUser;

        if (!guestUser.isGuest() || !guestUser.isActive() || guestUser.isDeleted()) {
            throw new RestApiException(INVALID_GUEST_SESSION);
        }
        if (targetUser.isGuest() || !targetUser.isActive() || targetUser.isDeleted()) {
            throw new RestApiException(INACTIVE_USER);
        }

        boolean inheritedRepresentativeMoodType = false;
        if (targetUser.getRepresentativeMoodType() == null && guestUser.getRepresentativeMoodType() != null) {
            targetUser.updateRepresentativeMoodType(guestUser.getRepresentativeMoodType());
            inheritedRepresentativeMoodType = true;
        }

        GuestDataMergeRepository.MergeResult result = mergeRepository.merge(guestUserId, targetUserId);
        if (result.retiredGuests() != 1) {
            throw new RestApiException(INVALID_GUEST_SESSION);
        }

        log.info(
                "Merged guest user {} into existing user {}: moodTestResults={}, recommendationSessions={}, "
                        + "drinkingRecords={}, inquiries={}, cocktailFavorites={}, moodTypes={}, cocktails={}, "
                        + "representativeMoodTypeInherited={}",
                guestUserId,
                targetUserId,
                result.transferredMoodTestResults(),
                result.transferredRecommendationSessions(),
                result.transferredDrinkingRecords(),
                result.transferredInquiries(),
                result.mergedCocktailFavorites(),
                result.mergedMoodTypes(),
                result.mergedCocktails(),
                inheritedRepresentativeMoodType
        );
    }

    private User lockUser(Long userId) {
        return userRepository.findByIdForUpdate(userId)
                .orElseThrow(() -> new RestApiException(USER_NOT_FOUND));
    }
}
