package com.example.moodtail.domain.user.client.kakao;

import com.example.moodtail.domain.user.client.SocialUserProfile;
import com.example.moodtail.domain.user.config.AuthProperties;
import com.example.moodtail.domain.user.enums.SocialProvider;
import com.example.moodtail.domain.user.support.AuthPropertiesFixtures;
import com.example.moodtail.global.common.exception.RestApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class KakaoOAuthClientTest {

    private MockRestServiceServer server;
    private KakaoOAuthClient kakaoOAuthClient;

    @BeforeEach
    void setUp() {
        RestClient.Builder restClientBuilder = RestClient.builder();
        server = MockRestServiceServer.bindTo(restClientBuilder).build();
        kakaoOAuthClient = new KakaoOAuthClient(
                restClientBuilder.build(),
                AuthPropertiesFixtures.kakaoProvider()
        );
    }

    @Test
    void requestUserProfileExtractsKakaoProfile() {
        server.expect(requestTo("https://kauth.kakao.com/oauth/token"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(HttpHeaders.CONTENT_TYPE, containsString(MediaType.APPLICATION_FORM_URLENCODED_VALUE)))
                .andExpect(content().string(containsString("grant_type=authorization_code")))
                .andExpect(content().string(containsString("client_id=kakao-client-id")))
                .andExpect(content().string(containsString("client_secret=kakao-client-secret")))
                .andExpect(content().string(containsString("redirect_uri=http%3A%2F%2Ffrontend%2Fkakao%2Fcallback")))
                .andExpect(content().string(containsString("code=kakao-code")))
                .andRespond(withSuccess(
                        """
                                {
                                  "access_token": "kakao-access-token",
                                  "token_type": "bearer"
                                }
                                """,
                        MediaType.APPLICATION_JSON
                ));
        server.expect(requestTo("https://kapi.kakao.com/v2/user/me"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("Authorization", "Bearer kakao-access-token"))
                .andRespond(withSuccess(
                        """
                                {
                                  "id": 12345,
                                  "kakao_account": {
                                    "email": "kakao@example.com",
                                    "profile": {
                                      "nickname": "카카오유저"
                                    }
                                  }
                                }
                                """,
                        MediaType.APPLICATION_JSON
                ));

        SocialUserProfile result = kakaoOAuthClient.requestUserProfile("kakao-code", null);

        assertThat(kakaoOAuthClient.provider()).isEqualTo(SocialProvider.KAKAO);
        assertThat(result.provider()).isEqualTo(SocialProvider.KAKAO);
        assertThat(result.providerUserId()).isEqualTo("12345");
        assertThat(result.email()).isEqualTo("kakao@example.com");
        assertThat(result.nickname()).isEqualTo("카카오유저");
        server.verify();
    }

    @Test
    void requestUserProfileRejectsMissingProviderUserId() {
        server.expect(requestTo("https://kauth.kakao.com/oauth/token"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(
                        """
                                {
                                  "access_token": "kakao-access-token",
                                  "token_type": "bearer"
                                }
                                """,
                        MediaType.APPLICATION_JSON
                ));
        server.expect(requestTo("https://kapi.kakao.com/v2/user/me"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> kakaoOAuthClient.requestUserProfile("kakao-code", null))
                .isInstanceOf(RestApiException.class);
    }

    @Test
    void requestUserProfileRejectsRedirectUriDifferentFromConfiguredValue() {
        assertThatThrownBy(() -> kakaoOAuthClient.requestUserProfile(
                "kakao-code",
                "http://untrusted.example.com/callback"
        )).isInstanceOfSatisfying(RestApiException.class, exception ->
                assertThat(exception.getErrorCode().getCode()).isEqualTo("AUTH016")
        );
    }

    @Test
    void requestUserProfileRejectsMissingClientConfigurationBeforeCallingKakao() {
        KakaoOAuthClient unconfiguredClient = new KakaoOAuthClient(
                RestClient.builder().build(),
                new AuthProperties.Provider(
                        false,
                        "",
                        "",
                        "http://frontend/kakao/callback",
                        "https://kauth.kakao.com/oauth/token",
                        "https://kapi.kakao.com/v2/user/me"
                )
        );

        assertThatThrownBy(() -> unconfiguredClient.requestUserProfile("kakao-code", null))
                .isInstanceOfSatisfying(RestApiException.class, exception ->
                        assertThat(exception.getErrorCode().getCode()).isEqualTo("AUTH017")
                );
    }

    @Test
    void requestUserProfileAcceptsMissingEmailWhenProviderIdExists() {
        server.expect(requestTo("https://kauth.kakao.com/oauth/token"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(
                        """
                                {
                                  "access_token": "kakao-access-token"
                                }
                                """,
                        MediaType.APPLICATION_JSON
                ));
        server.expect(requestTo("https://kapi.kakao.com/v2/user/me"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        """
                                {
                                  "id": 12345,
                                  "kakao_account": {
                                    "profile": {
                                      "nickname": "카카오유저"
                                    }
                                  }
                                }
                                """,
                        MediaType.APPLICATION_JSON
                ));

        SocialUserProfile result = kakaoOAuthClient.requestUserProfile("kakao-code", null);

        assertThat(result.providerUserId()).isEqualTo("12345");
        assertThat(result.email()).isNull();
    }
}
