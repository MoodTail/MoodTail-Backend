package com.example.moodtail.domain.user.dto.response;

import com.example.moodtail.domain.image.entity.Image;
import com.example.moodtail.domain.moodtest.entity.MoodType;
import com.example.moodtail.domain.user.entity.User;

public record UserProfileUpdateResponse(
        Long userId,
        String nickname,
        RepresentativeMoodTypeResponse representativeMoodType
) {

    public static UserProfileUpdateResponse from(User user) {
        return new UserProfileUpdateResponse(
                user.getId(),
                user.getNickname(),
                RepresentativeMoodTypeResponse.from(user.getRepresentativeMoodType())
        );
    }

    public record RepresentativeMoodTypeResponse(
            Long moodTypeId,
            String typeCode,
            String name,
            String characterImageUrl
    ) {

        private static RepresentativeMoodTypeResponse from(MoodType moodType) {
            if (moodType == null) {
                return null;
            }

            Image characterImage = moodType.getCharacterImage();
            return new RepresentativeMoodTypeResponse(
                    moodType.getId(),
                    moodType.getCode(),
                    moodType.getName(),
                    characterImage == null ? null : characterImage.getImageUrl()
            );
        }
    }
}
