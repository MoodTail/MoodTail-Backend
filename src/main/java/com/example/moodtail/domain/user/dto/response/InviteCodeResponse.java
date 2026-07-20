package com.example.moodtail.domain.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record InviteCodeResponse(
        @Schema(description = "사용자 초대 코드", example = "MOOD-0483")
        String inviteCode
) {
}
