package com.example.moodtail.domain.user.entity;

import com.example.moodtail.domain.moodtest.entity.MoodType;
import com.example.moodtail.domain.user.enums.UserRole;
import com.example.moodtail.domain.user.enums.UserStatus;
import com.example.moodtail.global.common.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Check;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "users",
        uniqueConstraints = @UniqueConstraint(name = "uk_users_guest_uuid", columnNames = "guest_uuid")
)
@Check(constraints = "(role = 'GUEST' and guest_uuid is not null) or "
        + "(role in ('USER', 'ADMIN') and guest_uuid is null)")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class User extends BaseEntity {

    private static final int MAX_NICKNAME_CODE_POINTS = 50;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "guest_uuid", length = 36)
    private String guestUuid;

    @Column(length = 50)
    private String nickname;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "representative_mood_type_id")
    private MoodType representativeMoodType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @ColumnDefault("'ACTIVE'")
    private UserStatus status;

    @Column(name = "last_accessed_at", nullable = false)
    private LocalDateTime lastAccessedAt;

    public static User createGuest(String guestUuid, String nickname, LocalDateTime accessedAt) {
        return User.builder()
                .guestUuid(guestUuid)
                .nickname(truncateNickname(nickname))
                .role(UserRole.GUEST)
                .status(UserStatus.ACTIVE)
                .lastAccessedAt(accessedAt)
                .build();
    }

    public void upgradeToUser(String socialNickname, LocalDateTime accessedAt) {
        if (role != UserRole.GUEST || guestUuid == null) {
            throw new IllegalStateException("게스트 사용자만 소셜 계정으로 전환할 수 있습니다.");
        }
        if (socialNickname != null && !socialNickname.isBlank()) {
            nickname = truncateNickname(socialNickname.trim());
        }
        guestUuid = null;
        role = UserRole.USER;
        lastAccessedAt = accessedAt;
    }

    public void updateLastAccessedAt(LocalDateTime accessedAt) {
        lastAccessedAt = accessedAt;
    }

    public boolean isGuest() {
        return role == UserRole.GUEST;
    }

    public boolean isActive() {
        return status == UserStatus.ACTIVE;
    }

    private static String truncateNickname(String value) {
        int codePointCount = value.codePointCount(0, value.length());
        if (codePointCount <= MAX_NICKNAME_CODE_POINTS) {
            return value;
        }
        int endIndex = value.offsetByCodePoints(0, MAX_NICKNAME_CODE_POINTS);
        return value.substring(0, endIndex);
    }
}
