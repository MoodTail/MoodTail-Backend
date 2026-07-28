package com.example.moodtail.domain.cocktail.repository.redis;

import com.example.moodtail.domain.cocktail.entity.Region;
import com.example.moodtail.domain.cocktail.model.DailyCocktailCacheValue;

import java.time.LocalDate;
import java.util.Optional;

public interface DailyCocktailRedisRepository {

    Optional<DailyCocktailCacheValue> find(
            LocalDate recommendationDate,
            Region region
    );

    void save(
            Region region,
            DailyCocktailCacheValue value
    );

    void delete(
            LocalDate recommendationDate,
            Region region
    );
}
