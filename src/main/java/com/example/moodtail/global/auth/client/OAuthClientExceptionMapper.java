package com.example.moodtail.global.auth.client;

import com.example.moodtail.global.common.exception.RestApiException;
import com.example.moodtail.global.common.exception.code.status.AuthErrorStatus;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.web.client.RestClientResponseException;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OAuthClientExceptionMapper {

    public static RestApiException fromResponse(RestClientResponseException exception) {
        if (exception.getStatusCode().value() == 429 || exception.getStatusCode().is5xxServerError()) {
            return new RestApiException(AuthErrorStatus.SOCIAL_PROVIDER_UNAVAILABLE);
        }
        if (exception.getStatusCode().is4xxClientError()) {
            return new RestApiException(AuthErrorStatus.INVALID_SOCIAL_LOGIN);
        }
        return new RestApiException(AuthErrorStatus.INVALID_SOCIAL_PROVIDER_RESPONSE);
    }
}
