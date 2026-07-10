package com.example.moodtail.domain.recommendation.repository;

import com.example.moodtail.domain.recommendation.entity.RecommendationSession;
import com.example.moodtail.domain.recommendation.entity.RecommendationSessionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecommendationSessionRepository extends JpaRepository<RecommendationSession, Long> {

    List<RecommendationSession> findByMoodTestResultIdAndSessionType(
            Long moodTestResultId,
            RecommendationSessionType sessionType
    );
}
