package com.example.moodtail.domain.history.dto.response;

import java.time.LocalDate;

public record HistoryDetailResponse(
        Long recordId,
        Long cocktailId,
        String cocktailName,
        String cocktailImageUrl,
        LocalDate recordDate
) {
}
