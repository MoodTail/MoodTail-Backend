package com.example.moodtail.domain.recommendation.model;

import com.example.moodtail.domain.cocktail.entity.Cocktail;

import java.math.BigDecimal;

public enum CustomTasteMetric {
    ALCOHOL_INTENSITY(
            "도수",
            "도수는 부담 없이"
    ) {
        @Override
        public BigDecimal getValue(Cocktail cocktail) {
            return cocktail.getAlcoholIntensity();
        }
    },

    SWEETNESS(
            "달콤함",
            "달콤함은 과하지 않게"
    ) {
        @Override
        public BigDecimal getValue(Cocktail cocktail) {
            return cocktail.getSweetness();
        }
    },

    SOURNESS(
            "산미",
            "산미는 산뜻하게"
    ) {
        @Override
        public BigDecimal getValue(Cocktail cocktail) {
            return cocktail.getSourness();
        }
    },

    REFRESHING(
            "청량감",
            "청량감은 산뜻하게"
    ) {
        @Override
        public BigDecimal getValue(Cocktail cocktail) {
            return cocktail.getRefreshing();
        }
    },

    BITTERNESS(
            "쓴맛",
            "씁쓸함은 은은하게"
    ) {
        @Override
        public BigDecimal getValue(Cocktail cocktail) {
            return cocktail.getBitterness();
        }
    };

    private final String displayName;
    private final String moderationPhrase;

    CustomTasteMetric(
            String displayName,
            String moderationPhrase
    ) {
        this.displayName = displayName;
        this.moderationPhrase = moderationPhrase;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getModerationPhrase() {
        return moderationPhrase;
    }

    public abstract BigDecimal getValue(Cocktail cocktail);
}
