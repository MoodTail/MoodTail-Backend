package com.example.moodtail.domain.collection.dto.response;

import com.example.moodtail.domain.collection.entity.UserUnlockedMoodType;
import com.example.moodtail.domain.image.entity.Image;
import com.example.moodtail.domain.moodtest.entity.MoodType;

import java.time.LocalDateTime;
import java.util.List;

public record UnlockedMoodTypesResponse(
        int totalCount,
        List<UnlockedMoodTypeResponse> moodTypes
) {

    public static UnlockedMoodTypesResponse from(List<UserUnlockedMoodType> unlockedMoodTypes) {
        List<UnlockedMoodTypeResponse> moodTypes = unlockedMoodTypes.stream()
                .map(UnlockedMoodTypeResponse::from)
                .toList();
        return new UnlockedMoodTypesResponse(moodTypes.size(), moodTypes);
    }

    public record UnlockedMoodTypeResponse(
            Long moodTypeId,
            String typeCode,
            String name,
            String shortDescription,
            String characterImageUrl,
            LocalDateTime unlockedAt
    ) {

        private static UnlockedMoodTypeResponse from(UserUnlockedMoodType unlockedMoodType) {
            MoodType moodType = unlockedMoodType.getMoodType();
            Image characterImage = moodType.getCharacterImage();
            return new UnlockedMoodTypeResponse(
                    moodType.getId(),
                    moodType.getCode(),
                    moodType.getName(),
                    moodType.getShortDescription(),
                    characterImage == null ? null : characterImage.getImageUrl(),
                    unlockedMoodType.getUnlockedAt()
            );
        }
    }
}
