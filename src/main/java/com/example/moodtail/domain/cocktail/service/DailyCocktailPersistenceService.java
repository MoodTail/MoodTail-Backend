package com.example.moodtail.domain.cocktail.service;

import com.example.moodtail.domain.cocktail.entity.DailyCocktailRecommendation;
import com.example.moodtail.domain.cocktail.repository.DailyCocktailRecommendationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DailyCocktailPersistenceService {

    private final DailyCocktailRecommendationRepository repository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public DailyCocktailRecommendation save(
            DailyCocktailRecommendation recommendation
    ) {
        return repository.saveAndFlush(recommendation);
    }
}
