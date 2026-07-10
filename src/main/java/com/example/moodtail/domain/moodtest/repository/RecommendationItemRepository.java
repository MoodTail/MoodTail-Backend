package com.example.moodtail.domain.moodtest.repository;

import com.example.moodtail.domain.moodtest.entity.RecommendationItem;
import com.example.moodtail.domain.moodtest.entity.RecommendationSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;

public interface RecommendationItemRepository extends JpaRepository<RecommendationItem, Long> {

    void deleteByRecommendationSessionIn(Collection<RecommendationSession> recommendationSessions);
}
