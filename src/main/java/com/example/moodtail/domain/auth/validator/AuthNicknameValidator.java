package com.example.moodtail.domain.auth.validator;

import com.example.moodtail.global.common.exception.RestApiException;

import static com.example.moodtail.global.common.exception.code.status.UserErrorStatus.INVALID_NICKNAME;

public final class AuthNicknameValidator {

    private static final int MIN_LENGTH = 2;
    private static final int MAX_LENGTH = 10;

    private AuthNicknameValidator() {
    }

    public static String normalize(String value) {
        if (value == null) {
            throw new RestApiException(INVALID_NICKNAME);
        }
        String nickname = value.trim();
        int length = nickname.codePointCount(0, nickname.length());
        if (length < MIN_LENGTH || length > MAX_LENGTH) {
            throw new RestApiException(INVALID_NICKNAME);
        }
        return nickname;
    }
}
