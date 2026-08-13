package com.example.moodtail.domain.collection.dto.response;

import com.example.moodtail.domain.collection.repository.CollectionProjection;
import com.example.moodtail.domain.moodtest.entity.MoodType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

public record CollectionResponse(
    RepresentativeMoodTypeResponse representativeMoodType,
    long unlockedMoodTypeCount,
    List<MoodTypeResponse> moodTypes
) {
    public static CollectionResponse of(
            MoodType representativeMoodType,
            List<CollectionProjection> projections
    ) {
        long unlockedMoodTypeCount = projections.stream()
                .filter(projection ->
                        projection.getUnlockedMoodTypeId() != null
                )
                .count();

        List<MoodTypeResponse> moodTypes = projections.stream()
                .map(MoodTypeResponse::from)
                .toList();

        return new CollectionResponse(
                RepresentativeMoodTypeResponse.from(
                        representativeMoodType
                ),
                unlockedMoodTypeCount,
                moodTypes
        );
    }

    public record RepresentativeMoodTypeResponse(
            Long moodTypeId,
            String typeCode,
            String name,
            String characterImageUrl
    ) {

        private static RepresentativeMoodTypeResponse from(
                MoodType moodType
        ) {
            if (moodType == null) {
                return null;
            }

            String characterImageUrl =
                    moodType.getCharacterImage() == null
                            ? null
                            : moodType.getCharacterImage()
                            .getImageUrl();

            return new RepresentativeMoodTypeResponse(
                    moodType.getId(),
                    moodType.getCode(),
                    moodType.getName(),
                    characterImageUrl
            );
        }
    }

    public record MoodTypeResponse(
            Long moodTypeId,
            String typeCode,
            String name,
            boolean unlocked,
            LocalDateTime unlockedAt,
            double collectionRate,
            long collectedCocktailCount,
            long totalCocktailCount,
            long requiredCocktailCount,
            @Schema(
                    description = "수집한 칵테일 수입니다. collectedCocktailCount를 사용해 주세요.",
                    deprecated = true
            )
            long collectedUserCount,
            String characterImageUrl
    ) {

        private static MoodTypeResponse from(
                CollectionProjection projection
        ) {
            long totalCocktailCount =
                    projection.getTotalCocktailCount();

            long collectedCocktailCount =
                    projection.getCollectedUserCount();

            double collectionRate = calculateCollectionRate(
                    collectedCocktailCount,
                    totalCocktailCount
            );

            return new MoodTypeResponse(
                    projection.getMoodTypeId(),
                    projection.getTypeCode(),
                    projection.getName(),
                    projection.getUnlockedMoodTypeId() != null,
                    projection.getUnlockedAt(),
                    collectionRate,
                    collectedCocktailCount,
                    totalCocktailCount,
                    calculateRequiredCocktailCount(totalCocktailCount),
                    collectedCocktailCount,
                    projection.getCharacterImageUrl()
            );
        }

        private static double calculateCollectionRate(
                long collectedCocktailCount,
                long totalCocktailCount
        ) {
            if (totalCocktailCount == 0) {
                return 0.0;
            }

            return Math.round(
                    collectedCocktailCount * 1000.0 / totalCocktailCount
            ) / 10.0;
        }

        private static long calculateRequiredCocktailCount(long totalCocktailCount) {
            return (totalCocktailCount + 1) / 2;
        }
    }
}
