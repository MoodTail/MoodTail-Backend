package com.example.moodtail.domain.user.client.kakao;

import com.fasterxml.jackson.annotation.JsonProperty;

public record KakaoUserInfoResponse(
        Long id,

        @JsonProperty("kakao_account")
        KakaoAccount kakaoAccount
) {

    public String providerUserId() {
        return id == null ? null : String.valueOf(id);
    }

    public String email() {
        return kakaoAccount == null ? null : kakaoAccount.email();
    }

    public String nickname() {
        if (kakaoAccount == null || kakaoAccount.profile() == null) {
            return null;
        }
        return kakaoAccount.profile().nickname();
    }

    public record KakaoAccount(
            String email,
            Profile profile
    ) {
    }

    public record Profile(
            String nickname
    ) {
    }
}
