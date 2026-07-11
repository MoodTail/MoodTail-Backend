package com.example.moodtail.domain.user.service;

import com.example.moodtail.domain.user.entity.User;
import com.example.moodtail.domain.user.repository.GuestDataMergeRepository;
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
        if (guestUserId.equals(targetUserId)) {
            return;
        }

        User firstLockedUser = lockUser(Math.min(guestUserId, targetUserId));
        User secondLockedUser = lockUser(Math.max(guestUserId, targetUserId));
        User guestUser = firstLockedUser.getId().equals(guestUserId) ? firstLockedUser : secondLockedUser;
        User targetUser = firstLockedUser.getId().equals(targetUserId) ? firstLockedUser : secondLockedUser;

        if (!guestUser.isGuest() || !guestUser.isActive() || guestUser.isDeleted()) {
            throw new RestApiException(INVALID_GUEST_SESSION);
        }
        if (!targetUser.isActive() || targetUser.isDeleted()) {
            throw new RestApiException(INACTIVE_USER);
        }

        boolean inheritedRepresentativeMoodType = false;
        if (targetUser.getRepresentativeMoodType() == null && guestUser.getRepresentativeMoodType() != null) {
            targetUser.updateRepresentativeMoodType(guestUser.getRepresentativeMoodType());
            inheritedRepresentativeMoodType = true;
        }

        GuestDataMergeRepository.MergeResult result = mergeRepository.merge(guestUserId, targetUserId);
        guestUser.delete();

        log.info(
                "Merged guest user {} into existing user {}: drinkingRecords={}, moodTestResults={}, "
                        + "moodTypes={}, cocktails={}, representativeMoodTypeInherited={}",
                guestUserId,
                targetUserId,
                result.transferredDrinkingRecords(),
                result.copiedMoodTestResults(),
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
