package com.example.moodtail.domain.auth.service;

import com.example.moodtail.domain.collection.repository.CollectionShareRepository;
import com.example.moodtail.domain.collection.repository.UserUnlockedMoodTypeRepository;
import com.example.moodtail.domain.image.service.ImageService;
import com.example.moodtail.domain.moodtest.repository.MoodTestResultRepository;
import com.example.moodtail.domain.moodtest.repository.SharedMoodTestResultRepository;
import com.example.moodtail.domain.recommendation.repository.RecommendationItemRepository;
import com.example.moodtail.domain.recommendation.repository.RecommendationSessionRepository;
import com.example.moodtail.domain.user.entity.User;
import com.example.moodtail.domain.user.repository.UserRepository;
import com.example.moodtail.global.common.exception.RestApiException;
import com.example.moodtail.global.common.exception.code.status.AuthErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GuestDataTransferService {

    private final UserRepository userRepository;
    private final MoodTestResultRepository moodTestResultRepository;
    private final RecommendationSessionRepository recommendationSessionRepository;
    private final RecommendationItemRepository recommendationItemRepository;
    private final UserUnlockedMoodTypeRepository userUnlockedMoodTypeRepository;
    private final SharedMoodTestResultRepository sharedMoodTestResultRepository;
    private final CollectionShareRepository collectionShareRepository;
    private final ImageService imageService;

    @Transactional(propagation = Propagation.MANDATORY)
    public void transferToExistingUser(Long guestUserId, Long targetUserId) {
        if (guestUserId == null || targetUserId == null) {
            throw new RestApiException(AuthErrorStatus.INVALID_GUEST_SESSION);
        }
        if (guestUserId.equals(targetUserId)) {
            validateTargetUser(lockUser(targetUserId));
            return;
        }

        User firstUser = lockUser(Math.min(guestUserId, targetUserId));
        User secondUser = lockUser(Math.max(guestUserId, targetUserId));
        User guestUser = firstUser.getId().equals(guestUserId) ? firstUser : secondUser;
        User targetUser = firstUser.getId().equals(targetUserId) ? firstUser : secondUser;

        validateGuestUser(guestUser);
        validateTargetUser(targetUser);

        transferMoodTestData(guestUserId, targetUser);
        transferUnlockedMoodTypes(guestUserId, targetUser);
        sharedMoodTestResultRepository.transferAllByUserId(guestUserId, targetUser);
        transferCollectionShare(guestUserId, targetUser);

        if (targetUser.getRepresentativeMoodType() == null && guestUser.getRepresentativeMoodType() != null) {
            targetUser.updateRepresentativeMoodType(guestUser.getRepresentativeMoodType());
        }
        guestUser.retireGuest();
    }

    private void transferMoodTestData(Long guestUserId, User targetUser) {
        List<Long> conflictingResultIds = moodTestResultRepository.findIdsConflictingWithUser(
                guestUserId,
                targetUser.getId()
        );
        if (!conflictingResultIds.isEmpty()) {
            List<Long> conflictingSessionIds =
                    recommendationSessionRepository.findAllIdsRelatedToMoodTestResultIdIn(conflictingResultIds);
            if (!conflictingSessionIds.isEmpty()) {
                recommendationItemRepository.deleteAllByRecommendationSessionIdIn(conflictingSessionIds);
                recommendationSessionRepository.deleteAllByIdIn(conflictingSessionIds);
            }
            moodTestResultRepository.deleteAllByIdInBatch(conflictingResultIds);
        }

        moodTestResultRepository.transferAllByUserId(guestUserId, targetUser);
        recommendationSessionRepository.transferAllByUserId(guestUserId, targetUser);
    }

    private void transferUnlockedMoodTypes(Long guestUserId, User targetUser) {
        List<Long> targetMoodTypeIds =
                userUnlockedMoodTypeRepository.findMoodTypeIdsByUserId(targetUser.getId());
        if (!targetMoodTypeIds.isEmpty()) {
            userUnlockedMoodTypeRepository.deleteAllByUserIdAndMoodTypeIdIn(
                    guestUserId,
                    targetMoodTypeIds
            );
        }
        userUnlockedMoodTypeRepository.transferAllByUserId(guestUserId, targetUser);
    }

    private void transferCollectionShare(Long guestUserId, User targetUser) {
        if (collectionShareRepository.findByUserId(targetUser.getId()).isPresent()) {
            String discardedThumbnailUrl =
                    collectionShareRepository.findThumbnailImageUrlByUserId(guestUserId).orElse(null);
            collectionShareRepository.deleteByUserId(guestUserId);
            registerImageCleanupAfterCommit(discardedThumbnailUrl);
            return;
        }
        collectionShareRepository.transferByUserId(guestUserId, targetUser);
    }

    private void registerImageCleanupAfterCommit(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return;
        }
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            throw new IllegalStateException("게스트 데이터 승계는 활성 트랜잭션 안에서 실행되어야 합니다.");
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                imageService.deleteImagesFromStorage(List.of(imageUrl));
            }
        });
    }

    private User lockUser(Long userId) {
        return userRepository.findByIdForUpdate(userId)
                .orElseThrow(() -> new RestApiException(AuthErrorStatus.USER_NOT_FOUND));
    }

    private void validateGuestUser(User guestUser) {
        if (!guestUser.isGuest() || !guestUser.isAvailableForAuthentication()) {
            throw new RestApiException(AuthErrorStatus.INVALID_GUEST_SESSION);
        }
    }

    private void validateTargetUser(User targetUser) {
        if (targetUser.isGuest() || !targetUser.isAvailableForAuthentication()) {
            throw new RestApiException(AuthErrorStatus.INACTIVE_USER);
        }
    }
}
