package com.example.moodtail.domain.recommendation.repository;

import com.example.moodtail.domain.recommendation.entity.SharedPairRecommendation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SharedPairRecommendationRepository extends JpaRepository<SharedPairRecommendation, Long> {

    Optional<SharedPairRecommendation> findByShareToken(String shareToken);

    @Modifying(flushAutomatically = true)
    @Query("""
            delete from SharedPairRecommendation recommendation
             where recommendation.creator.id = :userId
            """)
    int deleteAllByCreatorId(@Param("userId") Long userId);
}
