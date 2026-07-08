package com.example.moodtail.domain.recommendation.calculator;

import com.example.moodtail.domain.moodtest.entity.MoodQuestionOption;
import com.example.moodtail.domain.moodtest.entity.MoodQuestionOptionScore;
import com.example.moodtail.domain.moodtest.entity.QuestionScoreType;
import com.example.moodtail.domain.moodtest.entity.TasteMetricType;
import com.example.moodtail.domain.recommendation.model.TasteProfile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class TasteProfileCalculator {

    private static final BigDecimal MIN_SCORE = BigDecimal.valueOf(1.0);
    private static final BigDecimal MAX_SCORE = BigDecimal.valueOf(5.0);

    public TasteProfile calculate(List<MoodQuestionOption> fixedOptions, List<MoodQuestionOption> randomOptions) {
        Map<TasteMetricType, BigDecimal> fixedProfile = calculateFixedProfile(fixedOptions);
        Map<TasteMetricType, BigDecimal> deltaScores = calculateRandomDelta(randomOptions);

        return TasteProfile.of(
                applyDelta(fixedProfile, deltaScores, TasteMetricType.ALCOHOL_INTENSITY),
                applyDelta(fixedProfile, deltaScores, TasteMetricType.SWEETNESS),
                applyDelta(fixedProfile, deltaScores, TasteMetricType.SOURNESS),
                applyDelta(fixedProfile, deltaScores, TasteMetricType.REFRESHING),
                applyDelta(fixedProfile, deltaScores, TasteMetricType.BITTERNESS)
        );
    }

    private Map<TasteMetricType, BigDecimal> calculateFixedProfile(List<MoodQuestionOption> fixedOptions) {
        Map<TasteMetricType, BigDecimal> sums = new EnumMap<>(TasteMetricType.class);
        Map<TasteMetricType, Integer> counts = new EnumMap<>(TasteMetricType.class);

        fixedOptions.stream()
                .flatMap(option -> option.getScores().stream())
                .filter(score -> score.getScoreType() == QuestionScoreType.ABSOLUTE)
                .forEach(score -> {
                    sums.merge(score.getMetricType(), score.getScoreValue(), BigDecimal::add);
                    counts.merge(score.getMetricType(), 1, Integer::sum);
                });

        Map<TasteMetricType, BigDecimal> profile = new EnumMap<>(TasteMetricType.class);
        for (TasteMetricType metricType : TasteMetricType.values()) {
            BigDecimal sum = sums.get(metricType);
            Integer count = counts.get(metricType);
            if (sum == null || count == null || count == 0) {
                throw new IllegalStateException("Missing fixed question score for metric: " + metricType);
            }
            profile.put(metricType, sum.divide(BigDecimal.valueOf(count), 4, RoundingMode.HALF_UP));
        }
        return profile;
    }

    private Map<TasteMetricType, BigDecimal> calculateRandomDelta(List<MoodQuestionOption> randomOptions) {
        Map<TasteMetricType, BigDecimal> deltas = new EnumMap<>(TasteMetricType.class);

        randomOptions.stream()
                .flatMap(option -> option.getScores().stream())
                .filter(score -> score.getScoreType() == QuestionScoreType.DELTA)
                .forEach(score -> deltas.merge(score.getMetricType(), score.getScoreValue(), BigDecimal::add));

        return deltas;
    }

    private BigDecimal applyDelta(
            Map<TasteMetricType, BigDecimal> fixedProfile,
            Map<TasteMetricType, BigDecimal> deltaScores,
            TasteMetricType metricType
    ) {
        BigDecimal value = fixedProfile.get(metricType)
                .add(deltaScores.getOrDefault(metricType, BigDecimal.ZERO));
        return clamp(value);
    }

    private BigDecimal clamp(BigDecimal value) {
        if (value.compareTo(MIN_SCORE) < 0) {
            return MIN_SCORE;
        }
        if (value.compareTo(MAX_SCORE) > 0) {
            return MAX_SCORE;
        }
        return value;
    }
}
