package com.example.moodtail.domain.recommendation.entity;

import com.example.moodtail.domain.recommendation.model.RecommendationItemCommand;
import com.example.moodtail.domain.recommendation.model.TasteProfile;
import com.example.moodtail.domain.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "shared_pair_recommendations")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SharedPairRecommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "share_token", nullable = false, unique = true, length = 64)
    private String shareToken;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_user_id", nullable = false)
    private User creator;

    @Column(name = "compromise_alcohol_intensity", nullable = false, precision = 2, scale = 1)
    private BigDecimal compromiseAlcoholIntensity;

    @Column(name = "compromise_sweetness", nullable = false, precision = 2, scale = 1)
    private BigDecimal compromiseSweetness;

    @Column(name = "compromise_sourness", nullable = false, precision = 2, scale = 1)
    private BigDecimal compromiseSourness;

    @Column(name = "compromise_refreshing", nullable = false, precision = 2, scale = 1)
    private BigDecimal compromiseRefreshing;

    @Column(name = "compromise_bitterness", nullable = false, precision = 2, scale = 1)
    private BigDecimal compromiseBitterness;

    @Column(name = "cocktail_id_1", nullable = false)
    private Long cocktailId1;

    @Column(name = "cocktail_id_2", nullable = false)
    private Long cocktailId2;

    @Column(name = "cocktail_id_3", nullable = false)
    private Long cocktailId3;

    @Column(name = "match_score_1", nullable = false)
    private Integer matchScore1;

    @Column(name = "match_score_2", nullable = false)
    private Integer matchScore2;

    @Column(name = "match_score_3", nullable = false)
    private Integer matchScore3;

    @Column(name = "my_match_score", nullable = false)
    private Integer myMatchScore;

    @Column(name = "partner_match_score", nullable = false)
    private Integer partnerMatchScore;

    @Column(name = "thumbnail_image_url", nullable = false, length = 2048)
    private String thumbnailImageUrl;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private SharedPairRecommendation(
            User creator,
            String shareToken,
            TasteProfile compromiseProfile,
            List<RecommendationItemCommand> recommendations,
            Integer myMatchScore,
            Integer partnerMatchScore,
            String thumbnailImageUrl
    ) {
        this.creator = creator;
        this.shareToken = shareToken;
        this.compromiseAlcoholIntensity = compromiseProfile.alcoholIntensity();
        this.compromiseSweetness = compromiseProfile.sweetness();
        this.compromiseSourness = compromiseProfile.sourness();
        this.compromiseRefreshing = compromiseProfile.refreshing();
        this.compromiseBitterness = compromiseProfile.bitterness();
        this.cocktailId1 = recommendations.get(0).cocktailId();
        this.cocktailId2 = recommendations.get(1).cocktailId();
        this.cocktailId3 = recommendations.get(2).cocktailId();
        this.matchScore1 = recommendations.get(0).matchScore();
        this.matchScore2 = recommendations.get(1).matchScore();
        this.matchScore3 = recommendations.get(2).matchScore();
        this.myMatchScore = myMatchScore;
        this.partnerMatchScore = partnerMatchScore;
        this.thumbnailImageUrl = thumbnailImageUrl;
    }

    public static SharedPairRecommendation create(
            User creator,
            String shareToken,
            TasteProfile compromiseProfile,
            List<RecommendationItemCommand> recommendations,
            Integer myMatchScore,
            Integer partnerMatchScore,
            String thumbnailImageUrl
    ) {
        return new SharedPairRecommendation(
                creator,
                shareToken,
                compromiseProfile,
                recommendations,
                myMatchScore,
                partnerMatchScore,
                thumbnailImageUrl
        );
    }

    @PrePersist
    void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
