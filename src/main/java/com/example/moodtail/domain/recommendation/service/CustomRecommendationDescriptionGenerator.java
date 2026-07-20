package com.example.moodtail.domain.recommendation.service;

import com.example.moodtail.domain.cocktail.entity.Cocktail;
import com.example.moodtail.domain.recommendation.model.CustomTasteMetric;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@Component
public class CustomRecommendationDescriptionGenerator {
    public String generate(Cocktail cocktail) {
        List<CustomTasteMetric> sortedMetrics =
                sortMetricsByValue(cocktail);

        CustomTasteMetric firstCoreMetric =
                sortedMetrics.get(0);

        CustomTasteMetric secondCoreMetric =
                sortedMetrics.get(1);

        CustomTasteMetric moderationMetric =
                sortedMetrics.get(sortedMetrics.size() - 1);

        String corePhrase = joinWithAnd(
                firstCoreMetric.getDisplayName(),
                secondCoreMetric.getDisplayName()
        );

        return String.format(
                "%s%s 살리고, %s 맞춘 추천이에요.",
                corePhrase,
                selectTopicParticle(corePhrase),
                moderationMetric.getModerationPhrase()
        );
    }

    private List<CustomTasteMetric> sortMetricsByValue(
            Cocktail cocktail
    ) {
        return Arrays.stream(CustomTasteMetric.values())
                .sorted(Comparator
                        .comparing(
                                (CustomTasteMetric metric) ->
                                        metric.getValue(cocktail)
                        )
                        .reversed()
                        .thenComparingInt(
                                CustomTasteMetric::ordinal
                        ))
                .toList();
    }

    private String joinWithAnd(
            String first,
            String second
    ) {
        return first
                + selectAndParticle(first)
                + " "
                + second;
    }

    private String selectAndParticle(String word) {
        return hasFinalConsonant(word)
                ? "과"
                : "와";
    }

    private String selectTopicParticle(String word) {
        return hasFinalConsonant(word)
                ? "은"
                : "는";
    }

    private boolean hasFinalConsonant(String word) {
        char lastCharacter =
                word.charAt(word.length() - 1);

        return (lastCharacter - 0xAC00) % 28 != 0;
    }
}
