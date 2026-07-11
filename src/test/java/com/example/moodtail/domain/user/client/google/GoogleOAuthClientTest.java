package com.example.moodtail.domain.user.client.google;

import com.example.moodtail.domain.user.client.SocialUserProfile;
import com.example.moodtail.domain.user.config.AuthProperties;
import com.example.moodtail.domain.user.enums.SocialProvider;
import com.example.moodtail.domain.user.support.AuthPropertiesFixtures;
import com.example.moodtail.global.common.exception.RestApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
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
import static org.springframework.test.web.client.response.MockRestResponseCreators.withBadRequest;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

class GoogleOAuthClientTest {

    private MockRestServiceServer server;
    private GoogleOAuthClient googleOAuthClient;

    @BeforeEach
    void setUp() {
        RestClient.Builder restClientBuilder = RestClient.builder();
        server = MockRestServiceServer.bindTo(restClientBuilder).build();
        googleOAuthClient = new GoogleOAuthClient(
                restClientBuilder.build(),
                AuthPropertiesFixtures.googleProvider()
        );
    }

    @Test
    void requestUserProfileExtractsGoogleProfile() {
        server.expect(requestTo("https://oauth2.googleapis.com/token"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(HttpHeaders.CONTENT_TYPE, containsString(MediaType.APPLICATION_FORM_URLENCODED_VALUE)))
                .andExpect(content().string(containsString("grant_type=authorization_code")))
                .andExpect(content().string(containsString("client_id=google-client-id")))
                .andExpect(content().string(containsString("client_secret=google-client-secret")))
                .andExpect(content().string(containsString(
                        "redirect_uri=http%3A%2F%2Flocalhost%3A5173%2Fauth%2Fgoogle%2Fcallback"
                )))
                .andExpect(content().string(containsString("code=google-code")))
                .andRespond(withSuccess(
                        """
                                {
                                  "access_token": "google-access-token",
                                  "token_type": "Bearer"
                                }
                                """,
                        MediaType.APPLICATION_JSON
                ));
        server.expect(requestTo("https://openidconnect.googleapis.com/v1/userinfo"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("Authorization", "Bearer google-access-token"))
                .andRespond(withSuccess(
                        """
                                {
                                  "sub": "google-user-id",
                                  "email": "google@example.com",
                                  "email_verified": true,
                                  "name": "구글유저"
                                }
                                """,
                        MediaType.APPLICATION_JSON
                ));

        SocialUserProfile result = googleOAuthClient.requestUserProfile("google-code", null);

        assertThat(googleOAuthClient.provider()).isEqualTo(SocialProvider.GOOGLE);
        assertThat(googleOAuthClient.isEnabled()).isTrue();
        assertThat(result.provider()).isEqualTo(SocialProvider.GOOGLE);
        assertThat(result.providerUserId()).isEqualTo("google-user-id");
        assertThat(result.email()).isEqualTo("google@example.com");
        assertThat(result.nickname()).isEqualTo("구글유저");
        server.verify();
    }

    @Test
    void unverifiedGoogleEmailIsNotUsedAsAccountEmail() {
        GoogleUserInfoResponse userInfo = new GoogleUserInfoResponse(
                "google-user-id",
                "unverified@example.com",
                false,
                "구글유저"
        );

        assertThat(userInfo.verifiedEmail()).isNull();
    }

    @Test
    void redirectUriMustExactlyMatchConfiguredValue() {
        assertThatThrownBy(() -> googleOAuthClient.requestUserProfile(
                "google-code",
                "https://attacker.example.com/callback"
        )).isInstanceOfSatisfying(RestApiException.class, exception ->
                assertThat(exception.getErrorCode().getCode()).isEqualTo("AUTH016")
        );
    }

    @Test
    void invalidAuthorizationCodeReturnsInvalidSocialLogin() {
        server.expect(requestTo("https://oauth2.googleapis.com/token"))
                .andRespond(withBadRequest());

        assertThatThrownBy(() -> googleOAuthClient.requestUserProfile("invalid-code", null))
                .isInstanceOfSatisfying(RestApiException.class, exception ->
                        assertThat(exception.getErrorCode().getCode()).isEqualTo("AUTH016")
                );
        server.verify();
    }

    @Test
    void googleServerFailureReturnsSocialLoginFailure() {
        server.expect(requestTo("https://oauth2.googleapis.com/token"))
                .andRespond(withServerError());

        assertThatThrownBy(() -> googleOAuthClient.requestUserProfile("google-code", null))
                .isInstanceOfSatisfying(RestApiException.class, exception ->
                        assertThat(exception.getErrorCode().getCode()).isEqualTo("AUTH008")
                );
        server.verify();
    }

    @Test
    void googleRateLimitIsTreatedAsTemporaryProviderFailure() {
        server.expect(requestTo("https://oauth2.googleapis.com/token"))
                .andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS));

        assertThatThrownBy(() -> googleOAuthClient.requestUserProfile("google-code", null))
                .isInstanceOfSatisfying(RestApiException.class, exception ->
                        assertThat(exception.getErrorCode().getCode()).isEqualTo("AUTH008")
                );
        server.verify();
    }

    @Test
    void enabledGoogleProviderRequiresClientSecretAtStartup() {
        AuthProperties.Provider invalidProperties = new AuthProperties.Provider(
                true,
                "google-client-id",
                "",
                "https://frontend.example.com/google/callback",
                "https://oauth2.googleapis.com/token",
                "https://openidconnect.googleapis.com/v1/userinfo"
        );

        assertThatThrownBy(() -> new GoogleOAuthClient(RestClient.builder().build(), invalidProperties))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("client secret");
    }
}
