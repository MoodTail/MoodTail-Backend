package com.example.moodtail.domain.auth.service;

import com.example.moodtail.domain.collection.entity.CollectionShare;
import com.example.moodtail.domain.collection.repository.CollectionShareRepository;
import com.example.moodtail.domain.collection.repository.UserUnlockedMoodTypeRepository;
import com.example.moodtail.domain.image.service.ImageService;
import com.example.moodtail.domain.moodtest.entity.MoodType;
import com.example.moodtail.domain.moodtest.repository.MoodTestResultRepository;
import com.example.moodtail.domain.moodtest.repository.SharedMoodTestResultRepository;
import com.example.moodtail.domain.recommendation.repository.RecommendationItemRepository;
import com.example.moodtail.domain.recommendation.repository.RecommendationSessionRepository;
import com.example.moodtail.domain.user.entity.User;
import com.example.moodtail.domain.user.repository.UserRepository;
import com.example.moodtail.global.common.exception.RestApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.inOrder;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GuestDataTransferServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private MoodTestResultRepository moodTestResultRepository;

    @Mock
    private RecommendationSessionRepository recommendationSessionRepository;

    @Mock
    private RecommendationItemRepository recommendationItemRepository;

    @Mock
    private UserUnlockedMoodTypeRepository userUnlockedMoodTypeRepository;

    @Mock
    private SharedMoodTestResultRepository sharedMoodTestResultRepository;

    @Mock
    private CollectionShareRepository collectionShareRepository;

    @Mock
    private ImageService imageService;

    private GuestDataTransferService service;

    @BeforeEach
    void setUp() {
        service = new GuestDataTransferService(
                userRepository,
                moodTestResultRepository,
                recommendationSessionRepository,
                recommendationItemRepository,
                userUnlockedMoodTypeRepository,
                sharedMoodTestResultRepository,
                collectionShareRepository,
                imageService
        );
    }

    @Test
    void transfersGuestDataAndRetiresGuestWhileLockingUsersInIdOrder() {
        User targetUser = member(3L);
        User guestUser = guest(8L);
        MoodType representativeMoodType = mock(MoodType.class);
        guestUser.updateRepresentativeMoodType(representativeMoodType);
        when(userRepository.findByIdForUpdate(3L)).thenReturn(Optional.of(targetUser));
        when(userRepository.findByIdForUpdate(8L)).thenReturn(Optional.of(guestUser));
        when(moodTestResultRepository.findIdsConflictingWithUser(8L, 3L)).thenReturn(List.of());
        when(userUnlockedMoodTypeRepository.findMoodTypeIdsByUserId(3L)).thenReturn(List.of());
        when(collectionShareRepository.findByUserId(3L)).thenReturn(Optional.empty());

        boolean transferred = service.transferToExistingUserIfActiveGuest(8L, 3L);

        assertThat(transferred).isTrue();
        InOrder order = inOrder(userRepository);
        order.verify(userRepository).findByIdForUpdate(3L);
        order.verify(userRepository).findByIdForUpdate(8L);
        verify(moodTestResultRepository).transferAllByUserId(8L, targetUser);
        verify(recommendationSessionRepository).transferAllByUserId(8L, targetUser);
        verify(userUnlockedMoodTypeRepository).transferAllByUserId(8L, targetUser);
        verify(sharedMoodTestResultRepository).transferAllByUserId(8L, targetUser);
        verify(collectionShareRepository).transferByUserId(8L, targetUser);
        assertThat(targetUser.getRepresentativeMoodType()).isSameAs(representativeMoodType);
        assertThat(guestUser.isDeleted()).isTrue();
        assertThat(guestUser.getGuestUuid()).isNotEqualTo("guest-uuid");
    }

    @Test
    void removesGuestConflictsBeforeTransferringRemainingData() {
        User targetUser = member(9L);
        User guestUser = guest(2L);
        when(userRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(guestUser));
        when(userRepository.findByIdForUpdate(9L)).thenReturn(Optional.of(targetUser));
        when(moodTestResultRepository.findIdsConflictingWithUser(2L, 9L))
                .thenReturn(List.of(101L));
        when(recommendationSessionRepository
                .findAllIdsOwnedByUserIdInAndRelatedToMoodTestResultIdIn(
                        List.of(2L, 9L),
                        List.of(101L)
                ))
                .thenReturn(List.of(201L));
        when(userUnlockedMoodTypeRepository.findMoodTypeIdsByUserId(9L))
                .thenReturn(List.of(11L));
        when(collectionShareRepository.findByUserId(9L))
                .thenReturn(Optional.of(mock(CollectionShare.class)));
        when(collectionShareRepository.findThumbnailImageUrlByUserId(2L))
                .thenReturn(Optional.of("https://bucket.s3.ap-northeast-2.amazonaws.com/public/share/collections/image.png"));

        TransactionSynchronizationManager.initSynchronization();
        try {
            boolean transferred = service.transferToExistingUserIfActiveGuest(2L, 9L);
            assertThat(transferred).isTrue();
            List<TransactionSynchronization> synchronizations =
                    TransactionSynchronizationManager.getSynchronizations();
            assertThat(synchronizations).hasSize(1);
            synchronizations.get(0).afterCommit();
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }

        InOrder resultOrder = inOrder(
                recommendationItemRepository,
                recommendationSessionRepository,
                moodTestResultRepository
        );
        resultOrder.verify(recommendationSessionRepository)
                .clearPartnerMoodTestResultForOtherOwners(
                        List.of(2L, 9L),
                        List.of(101L)
                );
        resultOrder.verify(recommendationItemRepository)
                .deleteAllByRecommendationSessionIdIn(List.of(201L));
        resultOrder.verify(recommendationSessionRepository).deleteAllByIdIn(List.of(201L));
        resultOrder.verify(moodTestResultRepository).deleteAllByIdInBatch(List.of(101L));
        resultOrder.verify(moodTestResultRepository).transferAllByUserId(2L, targetUser);
        verify(userUnlockedMoodTypeRepository)
                .deleteAllByUserIdAndMoodTypeIdIn(2L, List.of(11L));
        verify(collectionShareRepository).deleteByUserId(2L);
        verify(collectionShareRepository, never()).transferByUserId(2L, targetUser);
        verify(imageService).deleteImagesFromStorage(List.of(
                "https://bucket.s3.ap-northeast-2.amazonaws.com/public/share/collections/image.png"
        ));
        assertThat(guestUser.isDeleted()).isTrue();
    }

    @Test
    void preservesThirdPartySessionsWhileRemovingTheirGuestPartnerReference() {
        User targetUser = member(9L);
        User guestUser = guest(2L);
        when(userRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(guestUser));
        when(userRepository.findByIdForUpdate(9L)).thenReturn(Optional.of(targetUser));
        when(moodTestResultRepository.findIdsConflictingWithUser(2L, 9L))
                .thenReturn(List.of(101L));
        when(recommendationSessionRepository
                .findAllIdsOwnedByUserIdInAndRelatedToMoodTestResultIdIn(
                        List.of(2L, 9L),
                        List.of(101L)
                ))
                .thenReturn(List.of());
        when(userUnlockedMoodTypeRepository.findMoodTypeIdsByUserId(9L)).thenReturn(List.of());
        when(collectionShareRepository.findByUserId(9L)).thenReturn(Optional.empty());

        boolean transferred = service.transferToExistingUserIfActiveGuest(2L, 9L);

        assertThat(transferred).isTrue();
        InOrder order = inOrder(recommendationSessionRepository, moodTestResultRepository);
        order.verify(recommendationSessionRepository)
                .clearPartnerMoodTestResultForOtherOwners(
                        List.of(2L, 9L),
                        List.of(101L)
                );
        order.verify(moodTestResultRepository).deleteAllByIdInBatch(List.of(101L));
        verify(recommendationItemRepository, never())
                .deleteAllByRecommendationSessionIdIn(any());
        verify(recommendationSessionRepository, never())
                .deleteAllByIdIn(any());
    }

    @Test
    void skipsInactiveGuestBeforeChangingDomainData() {
        User guestUser = guest(2L);
        guestUser.delete();
        User targetUser = member(9L);
        when(userRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(guestUser));
        when(userRepository.findByIdForUpdate(9L)).thenReturn(Optional.of(targetUser));

        boolean transferred = service.transferToExistingUserIfActiveGuest(2L, 9L);

        assertThat(transferred).isFalse();
        verifyNoInteractions(
                moodTestResultRepository,
                recommendationSessionRepository,
                recommendationItemRepository,
                userUnlockedMoodTypeRepository,
                sharedMoodTestResultRepository,
                collectionShareRepository
        );
    }

    @Test
    void treatsSameActiveMemberIdAsAlreadyTransferred() {
        User targetUser = member(2L);
        when(userRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(targetUser));

        boolean transferred = service.transferToExistingUserIfActiveGuest(2L, 2L);

        assertThat(transferred).isFalse();
        verify(userRepository).findByIdForUpdate(2L);
        verifyNoInteractions(
                moodTestResultRepository,
                recommendationSessionRepository,
                recommendationItemRepository,
                userUnlockedMoodTypeRepository,
                sharedMoodTestResultRepository,
                collectionShareRepository
        );
    }

    @Test
    void skipsMissingGuestWithoutChangingDomainData() {
        User targetUser = member(9L);
        when(userRepository.findByIdForUpdate(2L)).thenReturn(Optional.empty());
        when(userRepository.findByIdForUpdate(9L)).thenReturn(Optional.of(targetUser));

        boolean transferred = service.transferToExistingUserIfActiveGuest(2L, 9L);

        assertThat(transferred).isFalse();
        verifyNoInteractions(
                moodTestResultRepository,
                recommendationSessionRepository,
                recommendationItemRepository,
                userUnlockedMoodTypeRepository,
                sharedMoodTestResultRepository,
                collectionShareRepository
        );
    }

    @Test
    void rejectsMissingTargetUser() {
        User guestUser = guest(2L);
        when(userRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(guestUser));
        when(userRepository.findByIdForUpdate(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.transferToExistingUserIfActiveGuest(2L, 9L))
                .isInstanceOfSatisfying(RestApiException.class, exception ->
                        assertThat(exception.getErrorCode().getCode()).isEqualTo("AUTH010")
                );
    }

    private User guest(Long id) {
        User user = User.createGuest("guest-uuid", "게스트", LocalDateTime.now());
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private User member(Long id) {
        User user = User.createMember("기존 회원", LocalDateTime.now());
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }
}
