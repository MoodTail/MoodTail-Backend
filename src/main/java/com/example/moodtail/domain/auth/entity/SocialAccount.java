package com.example.moodtail.domain.auth.entity;

import com.example.moodtail.domain.user.entity.User;
import com.example.moodtail.global.auth.model.SocialProvider;
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
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "social_accounts",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_social_provider_user",
                        columnNames = {"provider", "provider_user_id"}
                ),
                @UniqueConstraint(
                        name = "uk_social_user_provider",
                        columnNames = {"user_id", "provider"}
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SocialAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, columnDefinition = "varchar(20)")
    private SocialProvider provider;

    @Column(
            name = "provider_user_id",
            nullable = false,
            length = 255,
            columnDefinition = "varchar(255) collate utf8mb4_bin"
    )
    private String providerUserId;

    @Column(length = 320)
    private String email;

    private SocialAccount(
            User user,
            SocialProvider provider,
            String providerUserId,
            String email
    ) {
        this.user = user;
        this.provider = provider;
        this.providerUserId = providerUserId;
        this.email = email;
    }

    public static SocialAccount create(
            User user,
            SocialProvider provider,
            String providerUserId,
            String email
    ) {
        return new SocialAccount(
                user,
                provider,
                providerUserId,
                email == null || email.isBlank() ? null : email.trim()
        );
    }
}
