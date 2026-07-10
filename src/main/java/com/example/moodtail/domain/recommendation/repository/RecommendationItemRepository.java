package com.example.moodtail.domain.recommendation.repository;

import com.example.moodtail.domain.recommendation.entity.RecommendationItem;
import com.example.moodtail.domain.recommendation.entity.RecommendationSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;

public interface RecommendationItemRepository extends JpaRepository<RecommendationItem, Long> {

    void deleteByRecommendationSessionIn(Collection<RecommendationSession> recommendationSessions);
}
