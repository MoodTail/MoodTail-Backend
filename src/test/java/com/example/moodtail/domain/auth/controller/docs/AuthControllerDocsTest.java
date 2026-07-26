package com.example.moodtail.domain.auth.controller.docs;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AuthControllerDocsTest {

    @Test
    void socialSignupTokenExampleMatchesRequestContract() throws Exception {
        JsonNode example = new ObjectMapper().readTree(
                AuthControllerDocs.GOOGLE_LOGIN_SUCCESS_EXAMPLE
        );

        assertThat(example.path("result").path("signupToken").asText())
                .matches("^[A-Za-z0-9_-]{43}$");
    }

    @Test
    void socialSignupSuccessExamplesDocumentBothCompletionStatuses() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode signupCompleted = objectMapper.readTree(
                AuthControllerDocs.SOCIAL_SIGNUP_SUCCESS_EXAMPLE
        );
        JsonNode loginCompleted = objectMapper.readTree(
                AuthControllerDocs.SOCIAL_SIGNUP_RECOVERED_LOGIN_EXAMPLE
        );

        assertThat(signupCompleted.path("result").path("status").asText())
                .isEqualTo("SIGNUP_COMPLETED");
        assertThat(loginCompleted.path("result").path("status").asText())
                .isEqualTo("LOGIN_COMPLETED");
    }
}
