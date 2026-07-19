package com.example.moodtail.global.auth.client.google;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GoogleUserInfoResponse(
        String sub,
        String email,
        @JsonProperty("email_verified")
        Boolean emailVerified,
        String name
) {

    public String providerUserId() {
        return sub;
    }

    public String nickname() {
        return name;
    }

    public String verifiedEmail() {
        return Boolean.TRUE.equals(emailVerified) ? email : null;
    }
}
