package com.example.moodtail.domain.moodtest.entity;

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
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "mood_test_results",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_mood_test_result_user_date", columnNames = {"user_id", "result_date"})
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MoodTestResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mood_type_id", nullable = false)
    private MoodType moodType;

    @Column(name = "result_date", nullable = false)
    private LocalDate resultDate;

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

    @Column(name = "share_token", unique = true)
    private String shareToken;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private MoodTestResult(User user, MoodType moodType, LocalDate resultDate, TasteProfile tasteProfile) {
        this.user = user;
        this.resultDate = resultDate;
        updateResult(moodType, tasteProfile);
    }

    public static MoodTestResult create(
            User user,
            MoodType moodType,
            LocalDate resultDate,
            TasteProfile tasteProfile
    ) {
        return new MoodTestResult(user, moodType, resultDate, tasteProfile);
    }

    public void updateResult(MoodType moodType, TasteProfile tasteProfile) {
        this.moodType = moodType;
        this.alcoholIntensity = tasteProfile.alcoholIntensity();
        this.sweetness = tasteProfile.sweetness();
        this.sourness = tasteProfile.sourness();
        this.refreshing = tasteProfile.refreshing();
        this.bitterness = tasteProfile.bitterness();
    }

    public TasteProfile toTasteProfile() {
        return TasteProfile.of(alcoholIntensity, sweetness, sourness, refreshing, bitterness);
    }

    @PrePersist
    void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
