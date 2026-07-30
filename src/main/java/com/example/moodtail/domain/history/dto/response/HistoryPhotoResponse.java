package com.example.moodtail.domain.history.dto.response;

import java.time.LocalDate;

public record HistoryPhotoResponse(
        Long photoId,
        LocalDate recordDate,
        String imageUrl
) {
}
