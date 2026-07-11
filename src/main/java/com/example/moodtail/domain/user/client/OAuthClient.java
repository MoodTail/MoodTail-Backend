package com.example.moodtail.domain.user.client;

import com.example.moodtail.domain.user.enums.SocialProvider;

public interface OAuthClient {

    SocialProvider provider();

    boolean isEnabled();

    default SocialUserProfile requestUserProfile(String authorizationCode, String redirectUri) {
        return requestUserProfile(authorizationCode, redirectUri, null);
    }

    SocialUserProfile requestUserProfile(String authorizationCode, String redirectUri, String codeVerifier);
}
