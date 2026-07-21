package com.example.moodtail.domain.recommendation.model;

import com.example.moodtail.domain.cocktail.entity.Cocktail;

public record CocktailRecommendationResult (
        Cocktail cocktail,
        double similarity
){
}
