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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.SimpleTransactionStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountWithdrawalServiceTest {

    @Mock
    private PlatformTransactionManager transactionManager;
    @Mock
    private UserRepository userRepository;
    @Mock
    private RecommendationSessionRepository recommendationSessionRepository;
    @Mock
    private RecommendationItemRepository recommendationItemRepository;
    @Mock
    private HistoryPhotoRepository historyPhotoRepository;
    @Mock
    private HistoryRepository historyRepository;
    @Mock
    private ImageRepository imageRepository;
    @Mock
    private CocktailRepository cocktailRepository;
    @Mock
    private CocktailFavoriteRepository cocktailFavoriteRepository;
    @Mock
    private UserUnlockedMoodTypeRepository userUnlockedMoodTypeRepository;
    @Mock
    private InquiryRepository inquiryRepository;
    @Mock
    private SharedMoodTestResultRepository sharedMoodTestResultRepository;
    @Mock
    private MoodTestResultRepository moodTestResultRepository;
    @Mock
    private MoodTypeRepository moodTypeRepository;
    @Mock
    private UserTermAgreementRepository userTermAgreementRepository;
    @Mock
    private SocialAccountRepository socialAccountRepository;
    @Mock
    private LocalAccountRepository localAccountRepository;
    @Mock
    private TokenSessionService tokenSessionService;
    @Mock
    private ImageService imageService;

    @InjectMocks
    private AccountWithdrawalService service;

    @BeforeEach
    void setUp() {
        when(transactionManager.getTransaction(any())).thenReturn(new SimpleTransactionStatus());
    }

    @Test
    void deletesDomainDataBeforeRevokingSessionAndCleaningStorage() {
        stubSuccessfulDatabaseDeletion(List.of(), List.of());
        when(moodTestResultRepository.deleteAllByUserId(7L)).thenReturn(3);
        when(imageService.deleteImagesFromStorage(List.of()))
                .thenReturn(new StorageCleanupResult(0, 0));

        service.withdraw(7L);

        verify(recommendationSessionRepository).findAllIdsRelatedToUserId(7L);
        verify(historyPhotoRepository).deleteAllByUserId(7L);
        verify(historyRepository).deleteAllByUserId(7L);
        verify(cocktailFavoriteRepository).deleteAllByUserId(7L);
        verify(userUnlockedMoodTypeRepository).deleteAllByUserId(7L);
        verify(inquiryRepository).anonymizeAllByUserId(7L);
        verify(sharedMoodTestResultRepository).deleteAllByUserId(7L);
        verify(userTermAgreementRepository).deleteAllByUserId(7L);
        verify(socialAccountRepository).deleteAllByUserId(7L);
        verify(localAccountRepository).deleteAllByUserId(7L);
        verify(moodTestResultRepository).deleteAllByUserId(7L);
        verify(userRepository).delete(any(User.class));

        InOrder order = inOrder(transactionManager, tokenSessionService, imageService);
        order.verify(transactionManager).commit(any());
        order.verify(tokenSessionService).revokeSession(7L);
        order.verify(imageService).deleteImagesFromStorage(List.of());
    }

    @Test
    void deletesRecommendationItemsBeforeTheirSessions() {
        stubSuccessfulDatabaseDeletion(List.of(), List.of());
        when(recommendationSessionRepository.findAllIdsRelatedToUserId(7L))
                .thenReturn(List.of(11L, 12L));
        when(imageService.deleteImagesFromStorage(List.of()))
                .thenReturn(new StorageCleanupResult(0, 0));

        service.withdraw(7L);

        InOrder order = inOrder(recommendationItemRepository, recommendationSessionRepository);
        order.verify(recommendationSessionRepository).findAllIdsRelatedToUserId(7L);
        order.verify(recommendationItemRepository)
                .deleteAllByRecommendationSessionIdIn(List.of(11L, 12L));
        order.verify(recommendationSessionRepository).deleteAllByIdIn(List.of(11L, 12L));
    }

    @Test
    void databaseFailureKeepsTheSessionAndSkipsStorageCleanup() {
        when(userRepository.findByIdForUpdate(7L))
                .thenThrow(new IllegalStateException("database unavailable"));

        assertThatThrownBy(() -> service.withdraw(7L))
                .isInstanceOf(IllegalStateException.class);

        verify(tokenSessionService, never()).revokeSession(any());
        verify(imageService, never()).deleteImagesFromStorage(any());
    }

    @Test
    void sessionCleanupFailureDoesNotPreventStorageCleanupAfterDeletion() {
        stubSuccessfulDatabaseDeletion(List.of(), List.of());
        doThrow(new RedisConnectionFailureException("redis unavailable"))
                .when(tokenSessionService)
                .revokeSession(7L);
        when(imageService.deleteImagesFromStorage(List.of()))
                .thenReturn(new StorageCleanupResult(0, 0));

        service.withdraw(7L);

        verify(imageService).deleteImagesFromStorage(List.of());
    }

    @Test
    void cleansOnlyUnreferencedHistoryImagesAndSharedResultImages() {
        OwnedImage deletedImage = ownedImage(21L, "https://cdn.example/history-deleted.png");
        OwnedImage retainedImage = ownedImage(22L, "https://cdn.example/history-retained.png");
        String sharedResultImage = "https://cdn.example/shared-result.png";
        stubSuccessfulDatabaseDeletion(List.of(deletedImage, retainedImage), List.of(sharedResultImage));
        when(cocktailRepository.existsByImageId(anyLong()))
                .thenAnswer(invocation -> invocation.<Long>getArgument(0).equals(22L));
        when(imageRepository.deleteByImageId(21L)).thenReturn(1);
        when(imageService.deleteImagesFromStorage(List.of(
                deletedImage.getImageUrl(),
                sharedResultImage
        ))).thenReturn(new StorageCleanupResult(2, 0));

        service.withdraw(7L);

        verify(imageRepository).deleteByImageId(21L);
        verify(imageRepository, never()).deleteByImageId(22L);
        verify(imageService).deleteImagesFromStorage(List.of(
                deletedImage.getImageUrl(),
                sharedResultImage
        ));
    }

    private void stubSuccessfulDatabaseDeletion(
            List<OwnedImage> historyImages,
            List<String> sharedResultImages
    ) {
        when(userRepository.findByIdForUpdate(7L)).thenReturn(Optional.of(member(7L)));
        when(historyPhotoRepository.findOwnedImagesByUserId(7L)).thenReturn(historyImages);
        when(sharedMoodTestResultRepository.findThumbnailImageUrlsByUserId(7L))
                .thenReturn(sharedResultImages);
        when(recommendationSessionRepository.findAllIdsRelatedToUserId(7L)).thenReturn(List.of());
    }

    private OwnedImage ownedImage(Long imageId, String imageUrl) {
        OwnedImage image = mock(OwnedImage.class);
        when(image.getImageId()).thenReturn(imageId);
        lenient().when(image.getImageUrl()).thenReturn(imageUrl);
        return image;
    }

    private User member(Long id) {
        User user = User.createMember("회원", LocalDateTime.now());
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }
}
