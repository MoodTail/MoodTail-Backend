package com.example.moodtail.domain.recommendation.model;

import com.example.moodtail.global.common.exception.RestApiException;
import com.example.moodtail.global.common.exception.code.status.RecommendationErrorStatus;

public record WeatherTasteVector(
        double alcoholIntensity,
        double sweetness,
        double sourness,
        double refreshing,
        double bitterness
) {
    public static WeatherTasteVector from(TasteProfile profile) {
        return new WeatherTasteVector(
                profile.alcoholIntensity().doubleValue(),
                profile.sweetness().doubleValue(),
                profile.sourness().doubleValue(),
                profile.refreshing().doubleValue(),
                profile.bitterness().doubleValue()
        );
    }

    public double magnitude() {
        return Math.sqrt(
                alcoholIntensity * alcoholIntensity
                        + sweetness * sweetness
                        + sourness * sourness
                        + refreshing * refreshing
                        + bitterness * bitterness
        );
    }

    public WeatherTasteVector normalize() {
        double magnitude = magnitude();

        if (magnitude == 0.0) {
            throw new RestApiException(RecommendationErrorStatus.UNPROCESSABLE_ENTITY);
        }

        return new WeatherTasteVector(
                alcoholIntensity / magnitude,
                sweetness / magnitude,
                sourness / magnitude,
                refreshing / magnitude,
                bitterness / magnitude
        );
    }

}
