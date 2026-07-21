package com.example.moodtail.domain.recommendation.repository;

import com.example.moodtail.domain.recommendation.entity.RecommendationItem;
import com.example.moodtail.domain.recommendation.entity.RecommendationSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;

public interface RecommendationItemRepository extends JpaRepository<RecommendationItem, Long> {

    void deleteByRecommendationSessionIn(Collection<RecommendationSession> recommendationSessions);

    @Modifying(flushAutomatically = true)
    @Query("delete from RecommendationItem item where item.recommendationSession.id in :sessionIds")
    int deleteAllByRecommendationSessionIdIn(@Param("sessionIds") Collection<Long> sessionIds);
}
