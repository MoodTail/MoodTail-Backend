package com.example.moodtail.domain.recommendation.entity;

import com.example.moodtail.domain.cocktail.entity.Cocktail;
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
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "recommendation_items",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_recommendation_item_session_ranking",
                        columnNames = {"recommendation_session_id", "ranking"}
                ),
                @UniqueConstraint(
                        name = "uk_recommendation_item_session_cocktail",
                        columnNames = {"recommendation_session_id", "cocktail_id"}
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RecommendationItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recommendation_session_id", nullable = false)
    private RecommendationSession recommendationSession;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cocktail_id", nullable = false)
    private Cocktail cocktail;

    @Column(nullable = false)
    private Integer ranking;

    @Column(name = "match_score", nullable = false)
    private Integer matchScore;

    private RecommendationItem(
            RecommendationSession recommendationSession,
            Cocktail cocktail,
            Integer ranking,
            Integer matchScore
    ) {
        this.recommendationSession = recommendationSession;
        this.cocktail = cocktail;
        this.ranking = ranking;
        this.matchScore = matchScore;
    }

    public static RecommendationItem create(
            RecommendationSession recommendationSession,
            Cocktail cocktail,
            Integer ranking,
            Integer matchScore
    ) {
        return new RecommendationItem(recommendationSession, cocktail, ranking, matchScore);
    }
}
