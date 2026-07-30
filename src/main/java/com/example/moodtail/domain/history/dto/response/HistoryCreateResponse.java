package com.example.moodtail.domain.history.dto.response;

import java.time.LocalDate;

public record HistoryCreateResponse(
        Long recordId,
        Long cocktailId,
        LocalDate recordDate
) {
}
