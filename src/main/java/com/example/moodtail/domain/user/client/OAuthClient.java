package com.example.moodtail.domain.user.client;

import com.example.moodtail.domain.user.enums.SocialProvider;

public interface OAuthClient {

    SocialProvider provider();

    boolean isEnabled();

    SocialUserProfile requestUserProfile(String authorizationCode, String redirectUri, String codeVerifier);
}
