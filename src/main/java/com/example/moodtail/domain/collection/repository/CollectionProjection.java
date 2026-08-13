package com.example.moodtail.domain.collection.repository;

import java.time.LocalDateTime;

public interface CollectionProjection {
    Long getMoodTypeId();

    String getTypeCode();

    String getName();

    String getCharacterImageUrl();

    Long getUnlockedMoodTypeId();

    LocalDateTime getUnlockedAt();

    Long getTotalCocktailCount();

    Long getCollectedUserCount();
}
