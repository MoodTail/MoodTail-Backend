package com.example.moodtail.domain.collection.repository;

import java.time.LocalDateTime;

public interface CollectionProjection {
    Long getMoodTypeId();

    String getTypeCode();

    String getName();

    String getCharacterImageUrl();

    Long getUnlockedMoodTypeId();

    Long getTotalCocktailCount();

    Long getCollectedUserCount();
}
