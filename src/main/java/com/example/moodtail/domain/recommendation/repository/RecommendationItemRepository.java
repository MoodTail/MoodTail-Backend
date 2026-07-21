package com.example.moodtail.domain.recommendation.repository;

import com.example.moodtail.domain.recommendation.entity.RecommendationItem;
import com.example.moodtail.domain.recommendation.entity.RecommendationSession;
import com.example.moodtail.domain.recommendation.entity.RecommendationSessionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface RecommendationItemRepository extends JpaRepository<RecommendationItem, Long> {

    void deleteByRecommendationSessionIn(Collection<RecommendationSession> recommendationSessions);

    @Modifying(flushAutomatically = true)
    @Query("delete from RecommendationItem item where item.recommendationSession.id in :sessionIds")
    int deleteAllByRecommendationSessionIdIn(@Param("sessionIds") Collection<Long> sessionIds);

    @Query("""
            select item.cocktail.id as cocktailId,
                   cocktail.nameKo as nameKo,
                   cocktail.nameEn as nameEn,
                   cocktail.shortDescription as shortDescription,
                   count(item) as recordCount
              from RecommendationItem item
              join item.cocktail cocktail
              join item.recommendationSession session
              join session.moodTestResult result
             where session.sessionType = :sessionType
               and item.ranking = 1
               and result.resultDate between :startDate and :endDate
               and (:moodTypeId is null or result.moodType.id = :moodTypeId)
             group by item.cocktail.id, cocktail.nameKo, cocktail.nameEn, cocktail.shortDescription
             order by count(item) desc, cocktail.nameKo asc, item.cocktail.id asc
            """)
    List<PopularCocktailCount> countPopularCocktails(
            @Param("sessionType") RecommendationSessionType sessionType,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("moodTypeId") Long moodTypeId
    );

    interface PopularCocktailCount {
        Long getCocktailId();

        String getNameKo();

        String getNameEn();

        String getShortDescription();

        long getRecordCount();
    }
}
