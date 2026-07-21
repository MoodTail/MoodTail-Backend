package com.example.moodtail.domain.collection.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record RepresentativeMoodTypeUpdateRequest(
        @NotNull
        Long moodTypeId
) {
}