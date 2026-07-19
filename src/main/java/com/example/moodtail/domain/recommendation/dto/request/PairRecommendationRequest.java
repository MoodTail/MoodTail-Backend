package com.example.moodtail.domain.recommendation.dto.request;

public record PairRecommendationRequest(
        Long resultId,
        String resultShareToken,
        String partnerShareToken
){

}
