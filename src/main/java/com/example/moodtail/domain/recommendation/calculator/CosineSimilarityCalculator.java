package com.example.moodtail.domain.recommendation.calculator;

import com.example.moodtail.domain.recommendation.model.WeatherTasteVector;
import org.springframework.stereotype.Component;

@Component
public class CosineSimilarityCalculator {

    public double calculate(
            WeatherTasteVector source,
            WeatherTasteVector target
    ) {
        WeatherTasteVector normalizedSource = source.normalize();
        WeatherTasteVector normalizedTarget = target.normalize();

        double similarity =
                normalizedSource.alcoholIntensity()
                        * normalizedTarget.alcoholIntensity()
                        + normalizedSource.sweetness()
                        * normalizedTarget.sweetness()
                        + normalizedSource.sourness()
                        * normalizedTarget.sourness()
                        + normalizedSource.refreshing()
                        * normalizedTarget.refreshing()
                        + normalizedSource.bitterness()
                        * normalizedTarget.bitterness();

        // double 연산 오차 방지
        return Math.max(-1.0, Math.min(1.0, similarity));
    }
}
