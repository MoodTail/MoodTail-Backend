package com.example.moodtail.global.auth.client;

import com.example.moodtail.global.auth.model.SocialProvider;
import com.example.moodtail.global.auth.model.SocialUserProfile;

public interface OAuthClient {

    SocialProvider provider();

    boolean isEnabled();

    void validateAuthorizationRequest(String authorizationCode, String redirectUri);

    SocialUserProfile requestUserProfile(String authorizationCode, String redirectUri, String codeVerifier);
}
