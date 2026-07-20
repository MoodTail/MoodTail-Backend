package com.example.moodtail.domain.collection.dto.response;

import com.example.moodtail.domain.collection.repository.CollectionProjection;
import com.example.moodtail.domain.moodtest.entity.MoodType;
import com.example.moodtail.domain.user.dto.response.MyPageResponse;

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
            int collectionRate,
            long collectedUserCount,
            String characterImageUrl
    ) {

        private static MoodTypeResponse from(
                CollectionProjection projection
        ) {
            long totalCocktailCount =
                    projection.getTotalCocktailCount();

            long collectedUserCount =
                    projection.getCollectedUserCount();

            int collectionRate = calculateCollectionRate(
                    collectedUserCount,
                    totalCocktailCount
            );

            return new MoodTypeResponse(
                    projection.getMoodTypeId(),
                    projection.getTypeCode(),
                    projection.getName(),
                    projection.getUnlockedMoodTypeId() != null,
                    collectionRate,
                    collectedUserCount,
                    projection.getCharacterImageUrl()
            );
        }

        private static int calculateCollectionRate(
                long collectedCocktailCount,
                long totalCocktailCount
        ) {
            if (totalCocktailCount == 0) {
                return 0;
            }

            return (int) (
                    collectedCocktailCount * 100
                            / totalCocktailCount
            );
        }
    }
}
