package com.example.moodtail.domain.user.service;

import com.example.moodtail.domain.user.entity.User;
import com.example.moodtail.domain.moodtest.entity.MoodType;
import com.example.moodtail.domain.user.repository.GuestDataMergeRepository;
import com.example.moodtail.domain.user.repository.UserRepository;
import com.example.moodtail.global.common.exception.RestApiException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GuestDataMergeServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private GuestDataMergeRepository mergeRepository;

    @Test
    void mergesSelectedDataAndSoftDeletesGuestWhileLockingUsersInIdOrder() {
        User target = socialUser(3L);
        User guest = guestUser(8L);
        MoodType guestMoodType = mock(MoodType.class);
        guest.updateRepresentativeMoodType(guestMoodType);
        when(userRepository.findByIdForUpdate(3L)).thenReturn(Optional.of(target));
        when(userRepository.findByIdForUpdate(8L)).thenReturn(Optional.of(guest));
        when(mergeRepository.merge(8L, 3L)).thenReturn(
                new GuestDataMergeRepository.MergeResult(2, 1, 3, 4, 5, 6)
        );
        GuestDataMergeService service = new GuestDataMergeService(userRepository, mergeRepository);

        service.mergeIntoExistingUser(8L, 3L);

        InOrder lockOrder = inOrder(userRepository);
        lockOrder.verify(userRepository).findByIdForUpdate(3L);
        lockOrder.verify(userRepository).findByIdForUpdate(8L);
        verify(mergeRepository).merge(8L, 3L);
        assertThat(guest.isDeleted()).isTrue();
        assertThat(target.isDeleted()).isFalse();
        assertThat(target.getRepresentativeMoodType()).isSameAs(guestMoodType);
    }

    @Test
    void rejectsAlreadyDeletedGuestWithoutMovingData() {
        User guest = guestUser(2L);
        guest.delete();
        User target = socialUser(9L);
        when(userRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(guest));
        when(userRepository.findByIdForUpdate(9L)).thenReturn(Optional.of(target));
        GuestDataMergeService service = new GuestDataMergeService(userRepository, mergeRepository);

        assertThatThrownBy(() -> service.mergeIntoExistingUser(2L, 9L))
                .isInstanceOfSatisfying(RestApiException.class, exception ->
                        assertThat(exception.getErrorCode().getCode()).isEqualTo("AUTH019")
                );

        verify(mergeRepository, never()).merge(2L, 9L);
    }

    private User guestUser(Long id) {
        User user = User.createGuest(
                "b8e2b515-76f0-4a6b-a94f-8a85f6b5bc7d",
                "게스트",
                LocalDateTime.now()
        );
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private User socialUser(Long id) {
        User user = guestUser(id);
        user.upgradeToUser("기존 회원", LocalDateTime.now());
        return user;
    }
}
