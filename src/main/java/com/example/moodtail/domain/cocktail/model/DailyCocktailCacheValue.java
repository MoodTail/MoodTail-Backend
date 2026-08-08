package com.example.moodtail.domain.cocktail.model;

import com.example.moodtail.domain.weather.dto.response.WeatherResponse;
import com.example.moodtail.domain.weather.entity.WeatherCondition;

import java.time.LocalDate;

public record DailyCocktailCacheValue (
        LocalDate recommendationDate,
        Long cocktailId,
        double temperature,
        int humidity,
        WeatherCondition weather,
        double cosineSimilarity
){
    public static DailyCocktailCacheValue of(
            LocalDate recommendationDate,
            Long cocktailId,
            WeatherResponse weatherResponse,
            double cosineSimilarity
    ) {
        return new DailyCocktailCacheValue(
                recommendationDate,
                cocktailId,
                weatherResponse.temperature(),
                weatherResponse.humidity(),
                weatherResponse.weather(),
                cosineSimilarity
        );
    }
}
