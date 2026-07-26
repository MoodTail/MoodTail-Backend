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
}
