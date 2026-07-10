package com.example.moodtail.domain.user.client;

import com.example.moodtail.domain.user.enums.SocialProvider;

public interface OAuthClient {

    SocialProvider provider();

    SocialUserProfile requestUserProfile(String authorizationCode, String redirectUri);
}
