package com.example.moodtail.domain.recommendation.model;

public record RecommendationItemCommand(
        Long cocktailId,
        Integer matchScore
) {
}
