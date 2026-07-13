package com.example.moodtail.domain.collection.repository;

import java.time.LocalDateTime;

public interface MoodTypeCollectionProjection {

    Long getMoodTypeId();

    String getTypeCode();

    String getName();

    String getShortDescription();

    String getCharacterImageUrl();

    LocalDateTime getUnlockedAt();
}
