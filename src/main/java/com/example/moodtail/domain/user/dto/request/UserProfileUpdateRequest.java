package com.example.moodtail.domain.user.dto.request;

import com.example.moodtail.domain.user.validator.NicknameValidator;
import io.swagger.v3.oas.annotations.media.Schema;

public record UserProfileUpdateRequest(
        @Schema(
                description = NicknameValidator.POLICY_DESCRIPTION,
                minLength = NicknameValidator.MIN_LENGTH,
                maxLength = NicknameValidator.MAX_LENGTH,
                example = "새로운푸미"
        )
        String nickname,

        @Schema(
                description = "대표 무드 타입 ID. 이미 해금한 무드 타입만 지정할 수 있습니다.",
                example = "2001"
        )
        Long representativeMoodTypeId
) {
}
