package com.example.moodtail.domain.cocktail.repository;

import com.example.moodtail.domain.cocktail.entity.DailyCocktailRecommendation;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface DailyCocktailRecommendationRepository
        extends JpaRepository<DailyCocktailRecommendation, Long> {

    @EntityGraph(attributePaths = {
            "cocktail",
            "cocktail.image"
    })
    Optional<DailyCocktailRecommendation> findByRecommendationDate(
            LocalDate recommendationDate
    );

}
