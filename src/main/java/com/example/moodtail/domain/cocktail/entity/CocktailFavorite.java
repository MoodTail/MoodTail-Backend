package com.example.moodtail.domain.cocktail.entity;

import com.example.moodtail.domain.moodtest.entity.Cocktail;
import com.example.moodtail.domain.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "cocktail_favorites",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_cocktail_favorites_user_cocktail",
                        columnNames = {"user_id", "cocktail_id"}
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CocktailFavorite {

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

    @Builder
    private CocktailFavorite(User user, Cocktail cocktail) {
        this.user = user;
        this.cocktail = cocktail;
    }

    public static CocktailFavorite create(User user, Cocktail cocktail) {
        return CocktailFavorite.builder()
                .user(user)
                .cocktail(cocktail)
                .build();
    }
}