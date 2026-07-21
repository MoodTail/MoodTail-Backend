package com.example.moodtail.domain.recommendation.model;

public record TasteMetricContribution(
        String metric,
        String metricNameKo,
        DominantSide dominantSide
) {
}
