package com.example.moodtail.domain.user.validator;

import com.example.moodtail.global.common.exception.RestApiException;

import static com.example.moodtail.global.common.exception.code.status.UserErrorStatus.INVALID_NICKNAME;

public final class NicknameValidator {

    public static final int MIN_LENGTH = 1;
    public static final int MAX_LENGTH = 50;
    public static final String POLICY_DESCRIPTION =
            "앞뒤 공백 제거 후 Unicode 문자 기준 1~50자인 닉네임";

    private NicknameValidator() {
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
