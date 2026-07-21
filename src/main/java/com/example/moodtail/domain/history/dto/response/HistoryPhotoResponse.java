package com.example.moodtail.domain.history.dto.response;

import com.example.moodtail.domain.image.entity.ImageSourceType;

import java.time.LocalDate;

public record HistoryPhotoResponse(
        Long photoId,
        LocalDate recordDate,
        ImageSourceType sourceType,
        String imageUrl
) {
}
