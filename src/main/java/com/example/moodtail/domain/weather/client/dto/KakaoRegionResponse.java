package com.example.moodtail.domain.weather.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record KakaoRegionResponse(
        Meta meta,
        List<Document> documents
) {
    public record Meta(
            @JsonProperty("total_count")
            int totalCount
    ) {
    }

    public record Document(
            @JsonProperty("region_type")
            String regionType,

            @JsonProperty("region_1depth_name")
            String region1DepthName
    ) {
    }
}
