package com.example.moodtail.domain.auth.model;

import com.example.moodtail.global.auth.model.SocialProvider;

public record SocialSignupSession(
        SocialProvider provider,
        String providerUserId,
        String email,
        Long guestUserId
) {
}
