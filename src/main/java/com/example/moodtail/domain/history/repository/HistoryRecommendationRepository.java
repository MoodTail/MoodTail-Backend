package com.example.moodtail.domain.history.repository;

import com.example.moodtail.domain.recommendation.entity.RecommendationItem;
import com.example.moodtail.domain.recommendation.entity.RecommendationSessionType;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface HistoryRecommendationRepository extends Repository<RecommendationItem, Long> {

    @Query("""
            select item
              from RecommendationItem item
              join fetch item.cocktail cocktail
              left join fetch cocktail.image
              join item.recommendationSession session
             where session.user.id = :userId
               and session.moodTestResult.id = :resultId
               and session.sessionType = :sessionType
             order by item.ranking
            """)
    List<RecommendationItem> findByTestResult(
            @Param("userId") Long userId,
            @Param("resultId") Long resultId,
            @Param("sessionType") RecommendationSessionType sessionType
    );
}
