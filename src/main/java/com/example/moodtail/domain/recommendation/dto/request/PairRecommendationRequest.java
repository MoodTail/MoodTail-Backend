package com.example.moodtail.domain.recommendation.dto.request;

import jakarta.validation.constraints.NotNull;

public record PairRecommendationRequest(
        @NotNull(message = "상대방 초대 코드는 필수입니다.")
        String partnerInviteCode
) {
}
