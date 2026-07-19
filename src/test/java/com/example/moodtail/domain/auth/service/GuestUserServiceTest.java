package com.example.moodtail.domain.auth.service;

import com.example.moodtail.domain.auth.model.GuestLoginUser;
import com.example.moodtail.domain.user.entity.User;
import com.example.moodtail.domain.user.entity.UserRole;
import com.example.moodtail.domain.user.repository.UserRepository;
import com.example.moodtail.support.auth.AuthPropertiesFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.SimpleTransactionStatus;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class GuestUserServiceTest {

    private static final UUID GUEST_UUID = UUID.fromString("b8e2b515-76f0-4a6b-a94f-8a85f6b5bc7d");

    @Mock
    private UserRepository userRepository;

    @Mock
    private PlatformTransactionManager transactionManager;

    private GuestUserService service;

    @BeforeEach
    void setUp() {
        service = new GuestUserService(
                userRepository,
                transactionManager,
                AuthPropertiesFixtures.defaults()
        );
        lenient().when(transactionManager.getTransaction(any())).thenReturn(new SimpleTransactionStatus());
    }

    @Test
    void createsGuestWithNormalizedUuidConfiguredNicknameAndGuestRole() {
        when(userRepository.findByGuestUuidAndRole(GUEST_UUID.toString(), UserRole.GUEST))
                .thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            ReflectionTestUtils.setField(user, "id", 7L);
            return user;
        });

        GuestLoginUser result = service.findOrCreate(GUEST_UUID);

        assertThat(result.userId()).isEqualTo(7L);
        assertThat(result.guestUuid()).isEqualTo(GUEST_UUID.toString());
        assertThat(result.role()).isEqualTo(UserRole.GUEST);
        assertThat(result.isNewUser()).isTrue();
        verify(userRepository).save(org.mockito.ArgumentMatchers.argThat(user ->
                user.getRole() == UserRole.GUEST
                        && GUEST_UUID.toString().equals(user.getGuestUuid())
                        && "게스트".equals(user.getNickname())
        ));
    }

    @Test
    void recoversExistingGuestWhenConcurrentInsertHitsUniqueConstraint() {
        User existingGuest = User.createGuest(GUEST_UUID.toString(), "게스트", LocalDateTime.now());
        ReflectionTestUtils.setField(existingGuest, "id", 9L);
        when(userRepository.findByGuestUuidAndRole(GUEST_UUID.toString(), UserRole.GUEST))
                .thenReturn(Optional.empty(), Optional.of(existingGuest));
        when(userRepository.save(any(User.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate guest uuid"));

        GuestLoginUser result = service.findOrCreate(GUEST_UUID);

        assertThat(result.userId()).isEqualTo(9L);
        assertThat(result.role()).isEqualTo(UserRole.GUEST);
        assertThat(result.isNewUser()).isFalse();
    }

    @Test
    void unrelatedGuestInsertIntegrityFailureIsNotHidden() {
        DataIntegrityViolationException databaseFailure =
                new DataIntegrityViolationException("unrelated foreign key failure");
        when(userRepository.findByGuestUuidAndRole(GUEST_UUID.toString(), UserRole.GUEST))
                .thenReturn(Optional.empty(), Optional.empty());
        when(userRepository.save(any(User.class))).thenThrow(databaseFailure);

        assertThatThrownBy(() -> service.findOrCreate(GUEST_UUID)).isSameAs(databaseFailure);
    }

    @Test
    void rejectsSoftDeletedGuestInsteadOfRestoringSession() {
        User deletedGuest = User.createGuest(GUEST_UUID.toString(), "게스트", LocalDateTime.now());
        deletedGuest.delete();
        when(userRepository.findByGuestUuidAndRole(GUEST_UUID.toString(), UserRole.GUEST))
                .thenReturn(Optional.of(deletedGuest));

        assertThatThrownBy(() -> service.findOrCreate(GUEST_UUID))
                .isInstanceOf(com.example.moodtail.global.common.exception.RestApiException.class);
    }

}
