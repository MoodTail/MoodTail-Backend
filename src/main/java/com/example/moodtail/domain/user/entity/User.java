package com.example.moodtail.domain.user.entity;

import com.example.moodtail.domain.moodtest.entity.MoodType;
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
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

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
    private UserStatus status;

    @Builder
    private User(String nickname, MoodType representativeMoodType, UserRole role, UserStatus status) {
        this.nickname = nickname;
        this.representativeMoodType = representativeMoodType;
        this.role = role == null ? UserRole.USER : role;
        this.status = status == null ? UserStatus.ACTIVE : status;
    }

    public static User createSocialUser(String nickname) {
        return User.builder()
                .nickname(nickname)
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .build();
    }

    public String getRoleAuthority() {
        return role.getAuthority();
    }

    public boolean isWithdrawn() {
        return status == UserStatus.WITHDRAWN;
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    public void updateRepresentativeMoodType(MoodType representativeMoodType) {
        this.representativeMoodType = representativeMoodType;
    }

    public void withdraw() {
        this.status = UserStatus.WITHDRAWN;
        delete();
    }
}
