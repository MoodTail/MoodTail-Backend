package com.example.moodtail.domain.moodtest.entity;

import com.example.moodtail.domain.recommendation.model.TasteProfile;
import com.example.moodtail.domain.user.entity.User;
import com.example.moodtail.global.common.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "shared_mood_test_results")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SharedMoodTestResult extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "share_token", nullable = false, unique = true, length = 64)
    private String shareToken;

    @Column(name = "alcohol_intensity", nullable = false, precision = 5, scale = 4)
    private BigDecimal alcoholIntensity;

    @Column(nullable = false, precision = 5, scale = 4)
    private BigDecimal sweetness;

    @Column(nullable = false, precision = 5, scale = 4)
    private BigDecimal sourness;

    @Column(nullable = false, precision = 5, scale = 4)
    private BigDecimal refreshing;

    @Column(nullable = false, precision = 5, scale = 4)
    private BigDecimal bitterness;

    @Column(name = "thumbnail_image_url", nullable = false, length = 2048)
    private String thumbnailImageUrl;

    private SharedMoodTestResult(
            User user,
            String shareToken,
            TasteProfile tasteProfile,
            String thumbnailImageUrl
    ) {
        this.user = user;
        this.shareToken = shareToken;
        this.alcoholIntensity = tasteProfile.alcoholIntensity();
        this.sweetness = tasteProfile.sweetness();
        this.sourness = tasteProfile.sourness();
        this.refreshing = tasteProfile.refreshing();
        this.bitterness = tasteProfile.bitterness();
        this.thumbnailImageUrl = thumbnailImageUrl;
    }

    public static SharedMoodTestResult create(
            User user,
            String shareToken,
            TasteProfile tasteProfile,
            String thumbnailImageUrl
    ) {
        return new SharedMoodTestResult(user, shareToken, tasteProfile, thumbnailImageUrl);
    }

    public TasteProfile toTasteProfile() {
        return TasteProfile.of(
                alcoholIntensity,
                sweetness,
                sourness,
                refreshing,
                bitterness
        );
    }
}
