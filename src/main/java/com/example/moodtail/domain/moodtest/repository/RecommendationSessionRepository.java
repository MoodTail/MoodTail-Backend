package com.example.moodtail.domain.moodtest.repository;

import com.example.moodtail.domain.moodtest.entity.RecommendationSession;
import com.example.moodtail.domain.moodtest.entity.RecommendationSessionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecommendationSessionRepository extends JpaRepository<RecommendationSession, Long> {

    List<RecommendationSession> findByMoodTestResultIdAndSessionType(
            Long moodTestResultId,
            RecommendationSessionType sessionType
    );
}
