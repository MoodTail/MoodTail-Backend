package com.example.moodtail.domain.auth.service;

import com.example.moodtail.domain.auth.repository.LocalAccountRepository;
import com.example.moodtail.domain.auth.repository.SocialAccountRepository;
import com.example.moodtail.domain.auth.repository.WithdrawalHistoryRepository;
import com.example.moodtail.domain.cocktail.repository.CocktailFavoriteRepository;
import com.example.moodtail.domain.collection.repository.CollectionShareRepository;
import com.example.moodtail.domain.collection.repository.UserUnlockedCocktailRepository;
import com.example.moodtail.domain.collection.repository.UserUnlockedMoodTypeRepository;
import com.example.moodtail.domain.image.entity.Image;
import com.example.moodtail.domain.image.repository.ImageRepository;
import com.example.moodtail.domain.image.service.ImageService;
import com.example.moodtail.domain.image.service.ImageService.StorageCleanupResult;
import com.example.moodtail.domain.inquiry.repository.InquiryRepository;
import com.example.moodtail.domain.moodtest.repository.MoodTestResultRepository;
import com.example.moodtail.domain.moodtest.repository.SharedMoodTestResultRepository;
import com.example.moodtail.domain.recommendation.repository.RecommendationItemRepository;
import com.example.moodtail.domain.recommendation.repository.RecommendationSessionRepository;
import com.example.moodtail.domain.recommendation.repository.SharedPairRecommendationRepository;
import com.example.moodtail.domain.report.repository.MonthlyReportShareRepository;
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
    private final SharedPairRecommendationRepository sharedPairRecommendationRepository;
    private final WithdrawalHistoryRepository withdrawalHistoryRepository;
    private final ImageRepository imageRepository;
    private final CocktailFavoriteRepository cocktailFavoriteRepository;
    private final UserUnlockedMoodTypeRepository userUnlockedMoodTypeRepository;
    private final InquiryRepository inquiryRepository;
    private final SharedMoodTestResultRepository sharedMoodTestResultRepository;
    private final MonthlyReportShareRepository monthlyReportShareRepository;
    private final MoodTestResultRepository moodTestResultRepository;
    private final UserTermAgreementRepository userTermAgreementRepository;
    private final SocialAccountRepository socialAccountRepository;
    private final LocalAccountRepository localAccountRepository;
    private final TokenSessionService tokenSessionService;
    private final ImageService imageService;
    private final UserUnlockedCocktailRepository userUnlockedCocktailRepository;
    private final CollectionShareRepository collectionShareRepository;

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

        List<Long> historyImageIds = withdrawalHistoryRepository.findOwnedImageIdsByUserId(userId);
        List<String> sharedResultImages =
                sharedMoodTestResultRepository.findThumbnailImageUrlsByUserId(userId).stream()
                        .filter(imageUrl -> imageUrl != null && !imageUrl.isBlank())
                        .toList();
        List<String> monthlyReportShareImages =
                monthlyReportShareRepository.findShareImageUrlsByUserId(userId).stream()
                        .filter(imageUrl -> imageUrl != null && !imageUrl.isBlank())
                        .toList();

        String collectionShareImage =
                collectionShareRepository.findThumbnailImageUrlByUserId(userId)
                        .filter(imageUrl -> !imageUrl.isBlank())
                        .orElse(null);

        deleteRecommendations(userId);
        sharedPairRecommendationRepository.deleteAllByCreatorId(userId);
        withdrawalHistoryRepository.deletePhotosByUserId(userId);
        withdrawalHistoryRepository.deleteRecordsByUserId(userId);
        List<String> storageCleanupCandidates = new ArrayList<>(deleteUnreferencedHistoryImages(historyImageIds));
        if (collectionShareImage != null) {
            storageCleanupCandidates.add(collectionShareImage);
        }

        cocktailFavoriteRepository.deleteAllByUserId(userId);
        userUnlockedCocktailRepository.deleteAllByUserId(userId);
        userUnlockedMoodTypeRepository.deleteAllByUserId(userId);
        collectionShareRepository.deleteByUserId(userId);
        inquiryRepository.anonymizeAllByUserId(userId);
        sharedMoodTestResultRepository.deleteAllByUserId(userId);
        storageCleanupCandidates.addAll(sharedResultImages);
        monthlyReportShareRepository.deleteAllByUserId(userId);
        storageCleanupCandidates.addAll(monthlyReportShareImages);
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

    private List<String> deleteUnreferencedHistoryImages(List<Long> imageIds) {
        if (imageIds.isEmpty()) {
            return List.of();
        }

        List<Image> unreferencedImages = imageRepository.findUnreferencedByIdIn(imageIds);
        if (unreferencedImages.isEmpty()) {
            return List.of();
        }

        imageRepository.deleteAllByIdInBatch(
                unreferencedImages.stream()
                        .map(Image::getId)
                        .toList()
        );
        return unreferencedImages.stream()
                .map(Image::getImageUrl)
                .toList();
    }

    private record WithdrawalResult(
            int deletedMoodTestResults,
            List<String> storageCleanupCandidates
    ) {
    }
}
