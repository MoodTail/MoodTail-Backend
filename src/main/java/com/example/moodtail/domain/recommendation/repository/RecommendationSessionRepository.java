package com.example.moodtail.domain.recommendation.repository;

import com.example.moodtail.domain.recommendation.entity.RecommendationSession;
import com.example.moodtail.domain.recommendation.entity.RecommendationSessionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface RecommendationSessionRepository extends JpaRepository<RecommendationSession, Long> {

    List<RecommendationSession> findByMoodTestResultIdAndSessionType(
            Long moodTestResultId,
            RecommendationSessionType sessionType
    );

    @Query("""
            select distinct session.id
              from RecommendationSession session
              left join session.moodTestResult result
              left join session.partnerMoodTestResult partnerResult
             where session.user.id = :userId
                or result.user.id = :userId
                or partnerResult.user.id = :userId
            """)
    List<Long> findAllIdsRelatedToUserId(@Param("userId") Long userId);

    @Modifying(flushAutomatically = true)
    @Query("delete from RecommendationSession session where session.id in :sessionIds")
    int deleteAllByIdIn(@Param("sessionIds") Collection<Long> sessionIds);
}
