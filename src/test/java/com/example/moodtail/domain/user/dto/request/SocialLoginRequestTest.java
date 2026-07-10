package com.example.moodtail.domain.user.dto.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SocialLoginRequestTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void authorizationCodeCanBeDeserializedFromCodeAlias() throws Exception {
        SocialLoginRequest request = objectMapper.readValue(
                """
                        {
                          "code": "social-code",
                          "redirectUri": "http://localhost:5173/auth/kakao/callback",
                          "state": "oauth-state"
                        }
                        """,
                SocialLoginRequest.class
        );

        assertThat(request.authorizationCode()).isEqualTo("social-code");
        assertThat(request.redirectUri()).isEqualTo("http://localhost:5173/auth/kakao/callback");
        assertThat(request.state()).isEqualTo("oauth-state");
    }
}
