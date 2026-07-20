package com.example.moodtail.domain.recommendation.entity;

import com.example.moodtail.domain.moodtest.entity.MoodTestResult;
import com.example.moodtail.domain.user.entity.User;
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
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "recommendation_sessions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RecommendationSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "session_type", nullable = false)
    private RecommendationSessionType sessionType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mood_test_result_id")
    private MoodTestResult moodTestResult;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partner_mood_test_result_id")
    private MoodTestResult partnerMoodTestResult;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private RecommendationSession(
            User user,
            RecommendationSessionType sessionType,
            MoodTestResult moodTestResult,
            MoodTestResult partnerMoodTestResult
    ) {
        this.user = user;
        this.sessionType = sessionType;
        this.moodTestResult = moodTestResult;
        this.partnerMoodTestResult = partnerMoodTestResult;
    }

    public static RecommendationSession forTestResult(User user, MoodTestResult moodTestResult) {
        return new RecommendationSession(user, RecommendationSessionType.TEST_RESULT, moodTestResult, null);
    }

    public static RecommendationSession forCompromise(
            User user,
            MoodTestResult moodTestResult,
            MoodTestResult partnerMoodTestResult
    ) {
        return new RecommendationSession(
                user,
                RecommendationSessionType.COMPROMISE,
                moodTestResult,
                partnerMoodTestResult
        );
    }


    @PrePersist
    void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
