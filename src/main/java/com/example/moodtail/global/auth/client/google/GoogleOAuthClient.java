package com.example.moodtail.global.auth.client.google;

import com.example.moodtail.global.auth.client.OAuthClient;
import com.example.moodtail.global.auth.client.OAuthClientExceptionMapper;
import com.example.moodtail.global.auth.client.OAuthRestClientFactory;
import com.example.moodtail.global.auth.config.AuthProperties;
import com.example.moodtail.global.auth.model.SocialProvider;
import com.example.moodtail.global.auth.model.SocialUserProfile;
import com.example.moodtail.global.auth.validator.PkceCodeVerifierValidator;
import com.example.moodtail.global.common.exception.RestApiException;
import com.example.moodtail.global.common.exception.code.status.AuthErrorStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.ResourceAccessException;

@Component
public class GoogleOAuthClient implements OAuthClient {

    private static final String AUTHORIZATION_CODE_GRANT_TYPE = "authorization_code";
    private static final MediaType FORM_URLENCODED_UTF8 =
            MediaType.valueOf("application/x-www-form-urlencoded;charset=utf-8");

    private final RestClient restClient;
    private final boolean enabled;
    private final String clientId;
    private final String clientSecret;
    private final String redirectUri;
    private final String tokenUri;
    private final String userInfoUri;

    @Autowired
    public GoogleOAuthClient(OAuthRestClientFactory restClientFactory, AuthProperties authProperties) {
        this(restClientFactory.create(), authProperties.oauth().google());
    }

    GoogleOAuthClient(RestClient restClient, AuthProperties.Provider properties) {
        this.restClient = restClient;
        this.enabled = properties.enabled();
        if (enabled && !StringUtils.hasText(properties.clientSecret())) {
            throw new IllegalArgumentException("Enabled Google OAuth requires a client secret");
        }
        this.clientId = properties.clientId();
        this.clientSecret = properties.clientSecret();
        this.redirectUri = properties.redirectUri();
        this.tokenUri = properties.tokenUri();
        this.userInfoUri = properties.userInfoUri();
    }

    @Override
    public SocialProvider provider() {
        return SocialProvider.GOOGLE;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public SocialUserProfile requestUserProfile(
            String authorizationCode,
            String requestRedirectUri,
            String codeVerifier
    ) {
        validateAuthorizationRequest(authorizationCode, requestRedirectUri);
        String resolvedRedirectUri = resolveRedirectUri(requestRedirectUri);
        GoogleTokenResponse token = requestToken(authorizationCode, resolvedRedirectUri, codeVerifier);
        GoogleUserInfoResponse userInfo = requestUserInfo(token.accessToken());

        if (!StringUtils.hasText(userInfo.providerUserId())) {
            throw new RestApiException(AuthErrorStatus.INVALID_SOCIAL_LOGIN);
        }

        return new SocialUserProfile(
                SocialProvider.GOOGLE,
                userInfo.providerUserId(),
                userInfo.verifiedEmail(),
                userInfo.nickname()
        );
    }

    @Override
    public void validateAuthorizationRequest(String authorizationCode, String requestRedirectUri) {
        if (!enabled
                || !StringUtils.hasText(clientId)
                || !StringUtils.hasText(clientSecret)
                || !StringUtils.hasText(tokenUri)
                || !StringUtils.hasText(userInfoUri)) {
            throw new RestApiException(AuthErrorStatus.SOCIAL_LOGIN_CONFIGURATION_ERROR);
        }
        if (!StringUtils.hasText(authorizationCode)) {
            throw new RestApiException(AuthErrorStatus.INVALID_SOCIAL_LOGIN);
        }
        resolveRedirectUri(requestRedirectUri);
    }

    private GoogleTokenResponse requestToken(String authorizationCode, String redirectUri, String codeVerifier) {
        validateCodeVerifier(codeVerifier);

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", AUTHORIZATION_CODE_GRANT_TYPE);
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);
        form.add("redirect_uri", redirectUri);
        form.add("code", authorizationCode);
        form.add("code_verifier", codeVerifier);

        try {
            GoogleTokenResponse response = restClient.post()
                    .uri(tokenUri)
                    .contentType(FORM_URLENCODED_UTF8)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(form)
                    .retrieve()
                    .body(GoogleTokenResponse.class);

            if (response == null || !StringUtils.hasText(response.accessToken())) {
                throw new RestApiException(AuthErrorStatus.INVALID_SOCIAL_PROVIDER_RESPONSE);
            }
            return response;
        } catch (RestClientResponseException e) {
            throw OAuthClientExceptionMapper.fromResponse(e);
        } catch (ResourceAccessException e) {
            throw new RestApiException(AuthErrorStatus.SOCIAL_PROVIDER_UNAVAILABLE);
        } catch (RestClientException e) {
            throw new RestApiException(AuthErrorStatus.INVALID_SOCIAL_PROVIDER_RESPONSE);
        }
    }

    private GoogleUserInfoResponse requestUserInfo(String accessToken) {
        if (!StringUtils.hasText(accessToken)) {
            throw new RestApiException(AuthErrorStatus.INVALID_SOCIAL_LOGIN);
        }

        try {
            GoogleUserInfoResponse response = restClient.get()
                    .uri(userInfoUri)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(GoogleUserInfoResponse.class);

            if (response == null || !StringUtils.hasText(response.providerUserId())) {
                throw new RestApiException(AuthErrorStatus.INVALID_SOCIAL_PROVIDER_RESPONSE);
            }
            return response;
        } catch (RestClientResponseException e) {
            throw OAuthClientExceptionMapper.fromResponse(e);
        } catch (ResourceAccessException e) {
            throw new RestApiException(AuthErrorStatus.SOCIAL_PROVIDER_UNAVAILABLE);
        } catch (RestClientException e) {
            throw new RestApiException(AuthErrorStatus.INVALID_SOCIAL_PROVIDER_RESPONSE);
        }
    }

    private void validateCodeVerifier(String codeVerifier) {
        if (!PkceCodeVerifierValidator.isValid(codeVerifier)) {
            throw new RestApiException(AuthErrorStatus.INVALID_SOCIAL_LOGIN);
        }
    }

    private String resolveRedirectUri(String requestRedirectUri) {
        if (!StringUtils.hasText(redirectUri)) {
            throw new RestApiException(AuthErrorStatus.SOCIAL_LOGIN_CONFIGURATION_ERROR);
        }
        if (StringUtils.hasText(requestRedirectUri) && !redirectUri.equals(requestRedirectUri)) {
            throw new RestApiException(AuthErrorStatus.INVALID_SOCIAL_LOGIN);
        }
        return redirectUri;
    }

}
