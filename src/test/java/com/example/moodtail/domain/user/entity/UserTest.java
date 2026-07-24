package com.example.moodtail.domain.user.entity;

import com.example.moodtail.domain.user.entity.UserRole;
import com.example.moodtail.domain.user.entity.UserStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserTest {

    @Test
    void guestHasUuidAndGuestRole() {
        LocalDateTime now = LocalDateTime.of(2026, 7, 10, 12, 0);

        User guest = User.createGuest("b8e2b515-76f0-4a6b-a94f-8a85f6b5bc7d", "게스트", now);

        assertThat(guest.getGuestUuid()).isNotNull();
        assertThat(guest.getRole()).isEqualTo(UserRole.GUEST);
        assertThat(guest.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(guest.getLastAccessedAt()).isEqualTo(now);
    }

    @Test
    void socialUpgradeKeepsUserButRevokesGuestUuid() {
        User guest = User.createGuest(
                "b8e2b515-76f0-4a6b-a94f-8a85f6b5bc7d",
                "게스트",
                LocalDateTime.of(2026, 7, 10, 12, 0)
        );

        guest.upgradeToUser("소셜유저", LocalDateTime.of(2026, 7, 10, 12, 10));

        assertThat(guest.getGuestUuid()).isNull();
        assertThat(guest.getRole()).isEqualTo(UserRole.USER);
        assertThat(guest.getNickname()).isEqualTo("소셜유저");
    }

    @Test
    void retiringGuestRevokesOriginalUuidAndSoftDeletesIt() {
        User guest = User.createGuest(
                "b8e2b515-76f0-4a6b-a94f-8a85f6b5bc7d",
                "게스트",
                LocalDateTime.now()
        );

        guest.retireGuest();

        assertThat(guest.getGuestUuid()).isNotEqualTo("b8e2b515-76f0-4a6b-a94f-8a85f6b5bc7d");
        assertThat(guest.isDeleted()).isTrue();
        assertThat(guest.getRole()).isEqualTo(UserRole.GUEST);
    }

    @Test
    void userCannotBeUpgradedAsGuestAgain() {
        User guest = User.createGuest(
                "b8e2b515-76f0-4a6b-a94f-8a85f6b5bc7d",
                "게스트",
                LocalDateTime.now()
        );
        guest.upgradeToUser("소셜유저", LocalDateTime.now());

        assertThatThrownBy(() -> guest.upgradeToUser("다른이름", LocalDateTime.now()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void socialNicknameIsTrimmedWithoutSplittingUnicodeCodePoints() {
        User guest = User.createGuest(
                "b8e2b515-76f0-4a6b-a94f-8a85f6b5bc7d",
                "게스트",
                LocalDateTime.now()
        );
        String longNickname = "😀".repeat(51);

        guest.upgradeToUser(longNickname, LocalDateTime.now());

        assertThat(guest.getNickname().codePointCount(0, guest.getNickname().length())).isEqualTo(50);
        assertThat(guest.getNickname()).isEqualTo("😀".repeat(50));
    }
}
