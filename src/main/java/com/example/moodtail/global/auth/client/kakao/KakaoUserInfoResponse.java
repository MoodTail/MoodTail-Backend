package com.example.moodtail.global.auth.client.kakao;

import com.fasterxml.jackson.annotation.JsonProperty;

public record KakaoUserInfoResponse(
        Long id,

        @JsonProperty("kakao_account")
        KakaoAccount kakaoAccount
) {

    public String providerUserId() {
        return id == null ? null : String.valueOf(id);
    }

    public String verifiedEmail() {
        if (kakaoAccount == null
                || !Boolean.TRUE.equals(kakaoAccount.emailValid())
                || !Boolean.TRUE.equals(kakaoAccount.emailVerified())) {
            return null;
        }
        return kakaoAccount.email();
    }

    public String nickname() {
        if (kakaoAccount == null || kakaoAccount.profile() == null) {
            return null;
        }
        return kakaoAccount.profile().nickname();
    }

    public record KakaoAccount(
            String email,

            @JsonProperty("is_email_valid")
            Boolean emailValid,

            @JsonProperty("is_email_verified")
            Boolean emailVerified,

            Profile profile
    ) {
    }

    public record Profile(
            String nickname
    ) {
    }
}
