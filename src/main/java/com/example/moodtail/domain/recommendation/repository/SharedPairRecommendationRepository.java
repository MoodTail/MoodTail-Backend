package com.example.moodtail.domain.recommendation.repository;

import com.example.moodtail.domain.recommendation.entity.SharedPairRecommendation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SharedPairRecommendationRepository extends JpaRepository<SharedPairRecommendation, Long> {

    Optional<SharedPairRecommendation> findByShareToken(String shareToken);
}
