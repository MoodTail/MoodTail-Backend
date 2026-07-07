package com.example.moodtail.domain.user.dto.response;

import com.example.moodtail.domain.user.entity.User;
import com.example.moodtail.domain.user.entity.UserRole;
import com.example.moodtail.domain.user.entity.UserStatus;
import com.example.moodtail.global.config.security.jwt.TokenInfo;

public record AuthResponse(
        UserDto user,
        TokenDto token,
        boolean isNewUser
) {

    public static AuthResponse of(User user, TokenInfo tokenInfo, boolean isNewUser) {
        return new AuthResponse(
                UserDto.from(user),
                TokenDto.from(tokenInfo),
                isNewUser
        );
    }

    public record UserDto(
            Long userId,
            String nickname,
            UserRole role,
            UserStatus status
    ) {
        private static UserDto from(User user) {
            return new UserDto(
                    user.getId(),
                    user.getNickname(),
                    user.getRole(),
                    user.getStatus()
            );
        }
    }

    public record TokenDto(
            String grantType,
            String accessToken,
            String refreshToken
    ) {
        private static TokenDto from(TokenInfo tokenInfo) {
            return new TokenDto("Bearer", tokenInfo.accessToken(), tokenInfo.refreshToken());
        }
    }
}
