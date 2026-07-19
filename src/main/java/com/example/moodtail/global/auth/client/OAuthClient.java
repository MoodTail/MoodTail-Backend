package com.example.moodtail.global.auth.client;

import com.example.moodtail.global.auth.model.SocialProvider;
import com.example.moodtail.global.auth.model.SocialUserProfile;

public interface OAuthClient {

    SocialProvider provider();

    boolean isEnabled();

    SocialUserProfile requestUserProfile(String authorizationCode, String redirectUri, String codeVerifier);
}
