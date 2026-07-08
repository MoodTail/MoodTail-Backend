package com.example.moodtail.domain.recommendation.model;

import com.example.moodtail.domain.moodtest.entity.TasteMetricType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.EnumMap;
import java.util.Map;

public record TasteProfile(
        BigDecimal alcoholIntensity,
        BigDecimal sweetness,
        BigDecimal sourness,
        BigDecimal refreshing,
        BigDecimal bitterness
) {

    public static TasteProfile of(
            BigDecimal alcoholIntensity,
            BigDecimal sweetness,
            BigDecimal sourness,
            BigDecimal refreshing,
            BigDecimal bitterness
    ) {
        return new TasteProfile(
                normalize(alcoholIntensity),
                normalize(sweetness),
                normalize(sourness),
                normalize(refreshing),
                normalize(bitterness)
        );
    }

    public BigDecimal get(TasteMetricType metricType) {
        return switch (metricType) {
            case ALCOHOL_INTENSITY -> alcoholIntensity;
            case SWEETNESS -> sweetness;
            case SOURNESS -> sourness;
            case REFRESHING -> refreshing;
            case BITTERNESS -> bitterness;
        };
    }

    public Map<TasteMetricType, BigDecimal> toMap() {
        Map<TasteMetricType, BigDecimal> values = new EnumMap<>(TasteMetricType.class);
        values.put(TasteMetricType.ALCOHOL_INTENSITY, alcoholIntensity);
        values.put(TasteMetricType.SWEETNESS, sweetness);
        values.put(TasteMetricType.SOURNESS, sourness);
        values.put(TasteMetricType.REFRESHING, refreshing);
        values.put(TasteMetricType.BITTERNESS, bitterness);
        return values;
    }

    private static BigDecimal normalize(BigDecimal value) {
        return value.setScale(1, RoundingMode.HALF_UP);
    }
}
