package com.example.moodtail.domain.user.controller.docs;

import com.example.moodtail.domain.user.dto.request.UserProfileUpdateRequest;
import com.example.moodtail.domain.user.validator.NicknameValidator;
import io.swagger.v3.core.converter.ModelConverters;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserProfileDtoSchemaTest {

    @Test
    void generatedProfileSchemaUsesSharedNicknamePolicy() {
        io.swagger.v3.oas.models.media.Schema<?> request = ModelConverters.getInstance(true)
                .readAll(UserProfileUpdateRequest.class)
                .get(UserProfileUpdateRequest.class.getSimpleName());
        io.swagger.v3.oas.models.media.Schema<?> nickname =
                (io.swagger.v3.oas.models.media.Schema<?>) request.getProperties().get("nickname");

        assertThat(nickname.getMinLength()).isEqualTo(NicknameValidator.MIN_LENGTH);
        assertThat(nickname.getMaxLength()).isEqualTo(NicknameValidator.MAX_LENGTH);
        assertThat(nickname.getDescription()).isEqualTo(NicknameValidator.POLICY_DESCRIPTION);
    }
}
