package com.example.moodtail.domain.collection.dto.response;

import com.example.moodtail.domain.collection.repository.MoodTypeCollectionProjection;

import java.time.LocalDateTime;
import java.util.List;

public record MoodTypesResponse(
        int totalCount,
        List<MoodTypeResponse> moodTypes
) {

    public static MoodTypesResponse from(List<MoodTypeCollectionProjection> projections) {
        List<MoodTypeResponse> moodTypes = projections.stream()
                .map(MoodTypeResponse::from)
                .toList();
        return new MoodTypesResponse(moodTypes.size(), moodTypes);
    }

    public record MoodTypeResponse(
            Long moodTypeId,
            String typeCode,
            String name,
            String shortDescription,
            String characterImageUrl,
            LocalDateTime unlockedAt
    ) {

        private static MoodTypeResponse from(MoodTypeCollectionProjection projection) {
            return new MoodTypeResponse(
                    projection.getMoodTypeId(),
                    projection.getTypeCode(),
                    projection.getName(),
                    projection.getShortDescription(),
                    projection.getCharacterImageUrl(),
                    projection.getUnlockedAt()
            );
        }
    }
}
