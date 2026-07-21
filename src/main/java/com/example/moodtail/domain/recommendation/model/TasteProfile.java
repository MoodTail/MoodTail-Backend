package com.example.moodtail.domain.recommendation.model;

import com.example.moodtail.domain.moodtest.entity.TasteMetricType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.EnumMap;
import java.util.Map;

import static java.math.RoundingMode.HALF_UP;


public record TasteProfile(
        BigDecimal alcoholIntensity,
        BigDecimal sweetness,
        BigDecimal sourness,
        BigDecimal refreshing,
        BigDecimal bitterness
) {
    private static final BigDecimal TWO = BigDecimal.valueOf(2);

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

    public TasteProfile average(TasteProfile other) {
        return TasteProfile.of(
                mean(this.alcoholIntensity, other.alcoholIntensity),
                mean(this.sweetness, other.sweetness),
                mean(this.sourness, other.sourness),
                mean(this.refreshing, other.refreshing),
                mean(this.bitterness, other.bitterness)
        );
    }

    private static BigDecimal mean(BigDecimal a, BigDecimal b) {
        return a.add(b).divide(TWO, 4, RoundingMode.HALF_UP);
    }

    private static BigDecimal normalize(BigDecimal value) {
        return value.setScale(1, HALF_UP);
    }
}
