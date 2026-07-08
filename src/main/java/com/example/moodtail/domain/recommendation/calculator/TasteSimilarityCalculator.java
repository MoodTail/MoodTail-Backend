package com.example.moodtail.domain.recommendation.calculator;

import com.example.moodtail.domain.recommendation.model.TasteProfile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class TasteSimilarityCalculator {

    private static final double MAX_DISTANCE = Math.sqrt(80.0);

    public double calculateDistance(TasteProfile source, TasteProfile target) {
        return Math.sqrt(
                squaredDifference(source.alcoholIntensity(), target.alcoholIntensity())
                        + squaredDifference(source.sweetness(), target.sweetness())
                        + squaredDifference(source.sourness(), target.sourness())
                        + squaredDifference(source.refreshing(), target.refreshing())
                        + squaredDifference(source.bitterness(), target.bitterness())
        );
    }

    public int calculateMatchScore(double distance) {
        int score = (int) Math.round((1.0 - distance / MAX_DISTANCE) * 100.0);
        return Math.max(0, Math.min(100, score));
    }

    private double squaredDifference(BigDecimal source, BigDecimal target) {
        double difference = source.doubleValue() - target.doubleValue();
        return difference * difference;
    }
}
