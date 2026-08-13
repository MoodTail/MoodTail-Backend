package com.example.moodtail.domain.cocktail.calculator;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MoodTypeCalculatorTest {

    private final MoodTypeCalculator calculator = new MoodTypeCalculator();

    @Test
    void calculatesCollectionRateAsPercentageWithOneDecimalPlace() {
        assertThat(calculator.calculateCollectionRate(1, 8)).isEqualTo(12.5);
        assertThat(calculator.calculateCollectionRate(3, 8)).isEqualTo(37.5);
        assertThat(calculator.calculateCollectionRate(4, 8)).isEqualTo(50.0);
        assertThat(calculator.calculateCollectionRate(8, 8)).isEqualTo(100.0);
    }

    @Test
    void returnsZeroWhenMoodTypeHasNoCocktails() {
        assertThat(calculator.calculateCollectionRate(0, 0)).isZero();
    }
}
