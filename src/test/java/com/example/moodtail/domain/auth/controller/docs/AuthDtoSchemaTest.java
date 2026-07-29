package com.example.moodtail.domain.auth.controller.docs;

import com.example.moodtail.domain.auth.dto.request.GuestLoginRequest;
import com.example.moodtail.domain.auth.dto.request.LocalEmailAvailabilityRequest;
import com.example.moodtail.domain.auth.dto.request.LocalLoginRequest;
import com.example.moodtail.domain.auth.dto.request.LocalSignupRequest;
import com.example.moodtail.domain.auth.dto.request.PasswordChangeRequest;
import com.example.moodtail.domain.auth.dto.request.PasswordResetCodeRequest;
import com.example.moodtail.domain.auth.dto.request.PasswordResetCodeVerifyRequest;
import com.example.moodtail.domain.auth.dto.request.SocialLoginRequest;
import com.example.moodtail.domain.auth.dto.request.SocialSignupRequest;
import com.example.moodtail.domain.auth.dto.request.TermAgreementRequest;
import com.example.moodtail.domain.auth.dto.response.GuestLoginResponse;
import com.example.moodtail.domain.auth.dto.response.LocalAuthResponse;
import com.example.moodtail.domain.auth.dto.response.LocalEmailAvailabilityResponse;
import com.example.moodtail.domain.auth.dto.response.OAuthStateResponse;
import com.example.moodtail.domain.auth.dto.response.PasswordResetCodeResponse;
import com.example.moodtail.domain.auth.dto.response.PasswordResetVerificationResponse;
import com.example.moodtail.domain.auth.dto.response.SocialLoginResponse;
import com.example.moodtail.domain.auth.dto.response.TokenResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import org.junit.jupiter.api.Test;

import java.lang.reflect.RecordComponent;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AuthDtoSchemaTest {

    private static final List<Class<?>> AUTH_DTOS = List.of(
            GuestLoginRequest.class,
            LocalEmailAvailabilityRequest.class,
            LocalLoginRequest.class,
            LocalSignupRequest.class,
            PasswordChangeRequest.class,
            PasswordResetCodeRequest.class,
            PasswordResetCodeVerifyRequest.class,
            SocialLoginRequest.class,
            SocialSignupRequest.class,
            TermAgreementRequest.class,
            GuestLoginResponse.class,
            LocalAuthResponse.class,
            LocalEmailAvailabilityResponse.class,
            OAuthStateResponse.class,
            PasswordResetCodeResponse.class,
            PasswordResetVerificationResponse.class,
            SocialLoginResponse.class,
            TokenResponse.class
    );

    @Test
    void everyAuthDtoFieldHasSchemaDescriptionAndExample() {
        for (Class<?> dto : AUTH_DTOS) {
            for (RecordComponent component : dto.getRecordComponents()) {
                Schema schema = component.getAccessor().getAnnotation(Schema.class);

                assertThat(schema)
                        .as(dto.getSimpleName() + "." + component.getName())
                        .isNotNull();
                assertThat(schema.description())
                        .as(dto.getSimpleName() + "." + component.getName())
                        .isNotBlank();
                assertThat(schema.example())
                        .as(dto.getSimpleName() + "." + component.getName())
                        .isNotBlank();
            }
        }
    }
}
