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
import com.example.moodtail.domain.user.validator.NicknameValidator;
import com.fasterxml.jackson.databind.node.TextNode;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.annotations.media.ArraySchema;
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
                ArraySchema arraySchema = component.getAccessor().getAnnotation(ArraySchema.class);

                assertThat(schema != null || arraySchema != null)
                        .as(dto.getSimpleName() + "." + component.getName())
                        .isTrue();
                Schema documentedSchema = schema == null ? arraySchema.arraySchema() : schema;
                assertThat(documentedSchema.description())
                        .as(dto.getSimpleName() + "." + component.getName())
                        .isNotBlank();
                assertThat(documentedSchema.example())
                        .as(dto.getSimpleName() + "." + component.getName())
                        .isNotBlank();
            }
        }
    }

    @Test
    void generatedSignupSchemasMatchValidationConstraints() {
        assertAgreementArrayConstraints(LocalSignupRequest.class);
        assertAgreementArrayConstraints(SocialSignupRequest.class);
        assertNicknameConstraints(LocalSignupRequest.class);
        assertNicknameConstraints(SocialSignupRequest.class);
    }

    @Test
    void generatedPasswordResetCodeExampleIsAString() {
        io.swagger.v3.oas.models.media.Schema<?> request = generatedSchema(
                PasswordResetCodeVerifyRequest.class
        );
        Object example = property(request, "code").getExample();

        assertThat(example)
                .isInstanceOf(TextNode.class);
        assertThat(((TextNode) example).asText()).isEqualTo("123456");
    }

    private void assertAgreementArrayConstraints(Class<?> requestType) {
        io.swagger.v3.oas.models.media.Schema<?> agreements = property(
                generatedSchema(requestType),
                "agreements"
        );

        assertThat(agreements.getTypes()).contains("array");
        assertThat(agreements.getMinItems()).isEqualTo(1);
        assertThat(agreements.getMaxItems()).isEqualTo(20);
    }

    private void assertNicknameConstraints(Class<?> requestType) {
        io.swagger.v3.oas.models.media.Schema<?> nickname = property(
                generatedSchema(requestType),
                "nickname"
        );

        assertThat(nickname.getMinLength()).isEqualTo(NicknameValidator.MIN_LENGTH);
        assertThat(nickname.getMaxLength()).isEqualTo(NicknameValidator.MAX_LENGTH);
        assertThat(nickname.getDescription()).isEqualTo(NicknameValidator.POLICY_DESCRIPTION);
    }

    private io.swagger.v3.oas.models.media.Schema<?> generatedSchema(Class<?> type) {
        return ModelConverters.getInstance(true)
                .readAll(type)
                .get(type.getSimpleName());
    }

    private io.swagger.v3.oas.models.media.Schema<?> property(
            io.swagger.v3.oas.models.media.Schema<?> schema,
            String name
    ) {
        return (io.swagger.v3.oas.models.media.Schema<?>) schema.getProperties().get(name);
    }
}
