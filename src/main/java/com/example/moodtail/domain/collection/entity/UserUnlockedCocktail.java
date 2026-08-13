package com.example.moodtail.domain.collection.entity;

import com.example.moodtail.domain.cocktail.entity.Cocktail;
import com.example.moodtail.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "user_unlocked_cocktails",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_user_unlocked_cocktail_user_cocktail",
                        columnNames = {"user_id", "cocktail_id"}
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserUnlockedCocktail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cocktail_id", nullable = false)
    private Cocktail cocktail;

    @Column(name = "unlocked_at", nullable = false, updatable = false)
    private LocalDateTime unlockedAt;

    private UserUnlockedCocktail(User user, Cocktail cocktail) {
        this.user = user;
        this.cocktail = cocktail;
    }

    public static UserUnlockedCocktail create(User user, Cocktail cocktail) {
        return new UserUnlockedCocktail(user, cocktail);
    }

    @PrePersist
    void prePersist() {
        this.unlockedAt = LocalDateTime.now();
    }
}
