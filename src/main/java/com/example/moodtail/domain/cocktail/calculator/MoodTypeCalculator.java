package com.example.moodtail.domain.cocktail.calculator;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class MoodTypeCalculator {

    private static final BigDecimal FIGURE_SCALE = BigDecimal.valueOf(20);

    public int convertFigureTo100(BigDecimal value) {
        return value.multiply(FIGURE_SCALE).intValue();
    }

    public int calculateTypePercent(
            long typeRecordCount,
            long totalRecordCount
    ) {
        if (totalRecordCount == 0) {
            return 0;
        }

        return (int) Math.round(
                typeRecordCount * 100.0 / totalRecordCount
        );
    }

    public double calculateCollectionRate(
            int unlockedCocktailCount,
            int totalCocktailCount
    ) {
        if (totalCocktailCount == 0) {
            return 0.0;
        }

        return Math.round(
                unlockedCocktailCount * 1000.0 / totalCocktailCount
        ) / 10.0;
    }
}
