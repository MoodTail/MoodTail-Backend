package com.example.moodtail.domain.recommendation.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class TasteProfileTest {

    @Test
    void averagesEachMetricAcrossTwoProfiles() {
        TasteProfile mine = TasteProfile.of(
                new BigDecimal("3.0"), new BigDecimal("2.0"), new BigDecimal("4.0"),
                new BigDecimal("1.0"), new BigDecimal("5.0")
        );
        TasteProfile partner = TasteProfile.of(
                new BigDecimal("4.0"), new BigDecimal("3.0"), new BigDecimal("2.0"),
                new BigDecimal("5.0"), new BigDecimal("1.0")
        );

        TasteProfile average = mine.average(partner);

        assertThat(average.alcoholIntensity()).isEqualTo(new BigDecimal("3.5000"));
        assertThat(average.sweetness()).isEqualTo(new BigDecimal("2.5000"));
        assertThat(average.sourness()).isEqualTo(new BigDecimal("3.0000"));
        assertThat(average.refreshing()).isEqualTo(new BigDecimal("3.0000"));
        assertThat(average.bitterness()).isEqualTo(new BigDecimal("3.0000"));
    }

    @Test
    void preservesFourDecimalPlacesWhenAveragingProfiles() {
        TasteProfile mine = TasteProfile.of(
                new BigDecimal("3.1"), new BigDecimal("3.1"), new BigDecimal("3.1"),
                new BigDecimal("3.1"), new BigDecimal("3.1")
        );
        TasteProfile partner = TasteProfile.of(
                new BigDecimal("3.2"), new BigDecimal("3.2"), new BigDecimal("3.2"),
                new BigDecimal("3.2"), new BigDecimal("3.2")
        );

        TasteProfile average = mine.average(partner);

        assertThat(average.alcoholIntensity()).isEqualTo(new BigDecimal("3.1500"));
        assertThat(average.alcoholIntensity().scale()).isEqualTo(4);
    }

    @Test
    void averageIsCommutative() {
        TasteProfile mine = TasteProfile.of(
                new BigDecimal("2.4"), new BigDecimal("1.8"), new BigDecimal("4.6"),
                new BigDecimal("3.3"), new BigDecimal("2.1")
        );
        TasteProfile partner = TasteProfile.of(
                new BigDecimal("4.1"), new BigDecimal("3.9"), new BigDecimal("1.2"),
                new BigDecimal("2.7"), new BigDecimal("4.8")
        );

        assertThat(mine.average(partner)).isEqualTo(partner.average(mine));
    }
}
