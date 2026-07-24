package com.example.moodtail.domain.recommendation.calculator;

import com.example.moodtail.domain.moodtest.entity.TasteMetricType;
import com.example.moodtail.domain.recommendation.model.DominantSide;
import com.example.moodtail.domain.recommendation.model.TasteMetricContribution;
import com.example.moodtail.domain.recommendation.model.TasteProfile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class TasteContributionCalculator {

    private static final int CORE_METRIC_COUNT = 2;

    private static final Map<TasteMetricType, String> METRIC_JSON_KEY = new EnumMap<>(Map.of(
            TasteMetricType.ALCOHOL_INTENSITY, "alcoholIntensity",
            TasteMetricType.SWEETNESS, "sweetness",
            TasteMetricType.SOURNESS, "sourness",
            TasteMetricType.REFRESHING, "refreshing",
            TasteMetricType.BITTERNESS, "bitterness"
    ));

    private static final Map<TasteMetricType, String> METRIC_NAME_KO = new EnumMap<>(Map.of(
            TasteMetricType.ALCOHOL_INTENSITY, "도수",
            TasteMetricType.SWEETNESS, "달콤함",
            TasteMetricType.SOURNESS, "새콤함",
            TasteMetricType.REFRESHING, "청량감",
            TasteMetricType.BITTERNESS, "씁쓸함"
    ));

    public List<TasteMetricContribution> calculate(
            TasteProfile myProfile,
            TasteProfile partnerProfile,
            TasteProfile cocktailProfile
    ) {
        return extractCoreMetrics(cocktailProfile).stream()
                .map(metric -> new TasteMetricContribution(
                        METRIC_JSON_KEY.get(metric),
                        METRIC_NAME_KO.get(metric),
                        determineDominantSide(myProfile, partnerProfile, cocktailProfile, metric)
                ))
                .toList();
    }

    private List<TasteMetricType> extractCoreMetrics(TasteProfile cocktailProfile) {
        return Arrays.stream(TasteMetricType.values())
                .sorted(Comparator.comparing(cocktailProfile::get).reversed())
                .limit(CORE_METRIC_COUNT)
                .toList();
    }

    private DominantSide determineDominantSide(
            TasteProfile myProfile,
            TasteProfile partnerProfile,
            TasteProfile cocktailProfile,
            TasteMetricType metric
    ) {
        BigDecimal cocktailValue = cocktailProfile.get(metric);
        BigDecimal myDistance = distance(myProfile.get(metric), cocktailValue);
        BigDecimal partnerDistance = distance(partnerProfile.get(metric), cocktailValue);

        return myDistance.compareTo(partnerDistance) <= 0 ? DominantSide.ME : DominantSide.PARTNER;
    }

    private BigDecimal distance(BigDecimal source, BigDecimal target) {
        return source.subtract(target).abs();
    }
}
