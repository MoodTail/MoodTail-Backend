package com.example.moodtail.domain.history.dto.response;

import com.example.moodtail.domain.image.entity.ImageSourceType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record HistoryDateResponse(
        LocalDate date,
        TestResult testResult,
        List<DrinkingRecordItem> drinkingRecords,
        List<Photo> photos
) {
    public HistoryDateResponse {
        drinkingRecords = List.copyOf(drinkingRecords);
        photos = List.copyOf(photos);
    }

    public record TestResult(
            Long resultId,
            MoodType moodType
    ) {
    }

    public record MoodType(
            Long moodTypeId,
            String typeCode,
            String name,
            String shortDescription,
            String characterImageUrl
    ) {
    }

    public record DrinkingRecordItem(
            Long recordId,
            Long cocktailId,
            String cocktailName,
            String shortDescription,
            BigDecimal alcoholDegree,
            String cocktailImageUrl
    ) {
    }

    public record Photo(
            Long photoId,
            ImageSourceType sourceType,
            String imageUrl
    ) {
    }
}
