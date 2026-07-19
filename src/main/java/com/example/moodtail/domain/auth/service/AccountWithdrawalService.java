package com.example.moodtail.domain.auth.service;

import com.example.moodtail.domain.auth.repository.LocalAccountRepository;
import com.example.moodtail.domain.auth.repository.SocialAccountRepository;
import com.example.moodtail.domain.cocktail.repository.CocktailFavoriteRepository;
import com.example.moodtail.domain.cocktail.repository.CocktailRepository;
import com.example.moodtail.domain.collection.repository.UserUnlockedMoodTypeRepository;
import com.example.moodtail.domain.history.repository.HistoryPhotoRepository;
import com.example.moodtail.domain.history.repository.HistoryPhotoRepository.OwnedImage;
import com.example.moodtail.domain.history.repository.HistoryRepository;
import com.example.moodtail.domain.image.repository.ImageRepository;
import com.example.moodtail.domain.image.service.ImageService;
import com.example.moodtail.domain.image.service.ImageService.StorageCleanupResult;
import com.example.moodtail.domain.inquiry.repository.InquiryRepository;
import com.example.moodtail.domain.moodtest.repository.MoodTestResultRepository;
import com.example.moodtail.domain.moodtest.repository.MoodTypeRepository;
import com.example.moodtail.domain.moodtest.repository.SharedMoodTestResultRepository;
import com.example.moodtail.domain.recommendation.repository.RecommendationItemRepository;
import com.example.moodtail.domain.recommendation.repository.RecommendationSessionRepository;
import com.example.moodtail.domain.user.entity.User;
import com.example.moodtail.domain.user.repository.UserRepository;
import com.example.moodtail.domain.user.repository.UserTermAgreementRepository;
import com.example.moodtail.global.common.exception.RestApiException;
import com.example.moodtail.global.common.exception.code.status.AuthErrorStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.List;

import static com.example.moodtail.global.token.redis.AuthRedisFailurePolicy.bestEffort;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountWithdrawalService {

    private final PlatformTransactionManager transactionManager;
    private final UserRepository userRepository;
    private final RecommendationSessionRepository recommendationSessionRepository;
    private final RecommendationItemRepository recommendationItemRepository;
    private final HistoryPhotoRepository historyPhotoRepository;
    private final HistoryRepository historyRepository;
    private final ImageRepository imageRepository;
    private final CocktailRepository cocktailRepository;
    private final CocktailFavoriteRepository cocktailFavoriteRepository;
    private final UserUnlockedMoodTypeRepository userUnlockedMoodTypeRepository;
    private final InquiryRepository inquiryRepository;
    private final SharedMoodTestResultRepository sharedMoodTestResultRepository;
    private final MoodTestResultRepository moodTestResultRepository;
    private final MoodTypeRepository moodTypeRepository;
    private final UserTermAgreementRepository userTermAgreementRepository;
    private final SocialAccountRepository socialAccountRepository;
    private final LocalAccountRepository localAccountRepository;
    private final TokenSessionService tokenSessionService;
    private final ImageService imageService;

    public void withdraw(Long userId) {
        WithdrawalResult result = deleteAccountData(userId);

        bestEffort(
                "delete withdrawn user authentication session",
                () -> tokenSessionService.revokeSession(userId)
        );
        StorageCleanupResult storageCleanup =
                imageService.deleteImagesFromStorage(result.storageCleanupCandidates());

        log.info(
                "Completed account withdrawal: userId={}, deletedMoodTestResults={}, "
                        + "storageCleanupCandidates={}, deletedStorageObjects={}, failedStorageObjects={}",
                userId,
                result.deletedMoodTestResults(),
                result.storageCleanupCandidates().size(),
                storageCleanup.deleted(),
                storageCleanup.failed()
        );
    }

    private WithdrawalResult deleteAccountData(Long userId) {
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        template.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        WithdrawalResult result = template.execute(status -> deleteAccountDataInTransaction(userId));
        if (result == null) {
            throw new RestApiException(AuthErrorStatus.AUTH_INFRASTRUCTURE_UNAVAILABLE);
        }
        return result;
    }

    private WithdrawalResult deleteAccountDataInTransaction(Long userId) {
        User user = userRepository.findByIdForUpdate(userId)
                .orElseThrow(() -> new RestApiException(AuthErrorStatus.USER_NOT_FOUND));

        List<OwnedImage> historyImages = historyPhotoRepository.findOwnedImagesByUserId(userId);
        List<String> sharedResultImages =
                sharedMoodTestResultRepository.findThumbnailImageUrlsByUserId(userId);

        deleteRecommendations(userId);
        historyPhotoRepository.deleteAllByUserId(userId);
        historyRepository.deleteAllByUserId(userId);
        List<String> storageCleanupCandidates = new ArrayList<>(deleteUnreferencedHistoryImages(historyImages));
        cocktailFavoriteRepository.deleteAllByUserId(userId);
        userUnlockedMoodTypeRepository.deleteAllByUserId(userId);
        inquiryRepository.anonymizeAllByUserId(userId);
        sharedMoodTestResultRepository.deleteAllByUserId(userId);
        storageCleanupCandidates.addAll(sharedResultImages);
        userTermAgreementRepository.deleteAllByUserId(userId);
        socialAccountRepository.deleteAllByUserId(userId);
        localAccountRepository.deleteAllByUserId(userId);
        int deletedMoodTestResults = moodTestResultRepository.deleteAllByUserId(userId);

        userRepository.delete(user);
        userRepository.flush();
        return new WithdrawalResult(
                deletedMoodTestResults,
                List.copyOf(storageCleanupCandidates)
        );
    }

    private void deleteRecommendations(Long userId) {
        List<Long> sessionIds = recommendationSessionRepository.findAllIdsRelatedToUserId(userId);
        if (sessionIds.isEmpty()) {
            return;
        }
        recommendationItemRepository.deleteAllByRecommendationSessionIdIn(sessionIds);
        recommendationSessionRepository.deleteAllByIdIn(sessionIds);
    }

    private List<String> deleteUnreferencedHistoryImages(List<OwnedImage> images) {
        List<String> storageCleanupCandidates = new ArrayList<>();
        for (OwnedImage image : images) {
            if (isImageReferenced(image.getImageId())) {
                continue;
            }
            if (imageRepository.deleteByImageId(image.getImageId()) == 1) {
                storageCleanupCandidates.add(image.getImageUrl());
            }
        }
        return storageCleanupCandidates;
    }

    private boolean isImageReferenced(Long imageId) {
        return historyPhotoRepository.existsByImageId(imageId)
                || cocktailRepository.existsByImageId(imageId)
                || moodTypeRepository.existsByCharacterImageId(imageId);
    }

    private record WithdrawalResult(
            int deletedMoodTestResults,
            List<String> storageCleanupCandidates
    ) {
    }
}
