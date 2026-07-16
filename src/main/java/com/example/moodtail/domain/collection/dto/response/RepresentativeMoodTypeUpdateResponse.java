package com.example.moodtail.domain.collection.dto.response;

import com.example.moodtail.domain.image.entity.Image;
import com.example.moodtail.domain.moodtest.entity.MoodType;
import io.swagger.v3.oas.annotations.media.Schema;

public record RepresentativeMoodTypeUpdateResponse(
        Long moodTypeId,

        String typeCode,

        String name,

        String characterImageUrl
) {
    public static RepresentativeMoodTypeUpdateResponse from(
            MoodType moodType
    ) {
        Image characterImage = moodType.getCharacterImage();

        return new RepresentativeMoodTypeUpdateResponse(
                moodType.getId(),
                moodType.getCode(),
                moodType.getName(),
                characterImage == null
                        ? null
                        : characterImage.getImageUrl()
        );
    }
}