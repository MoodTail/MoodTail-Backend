package com.example.moodtail.domain.auth.controller.docs;

import com.example.moodtail.global.common.exception.code.BaseCodeDto;
import com.example.moodtail.global.common.exception.code.status.AuthErrorStatus;
import com.example.moodtail.global.common.exception.code.status.GlobalErrorStatus;
import com.example.moodtail.global.common.exception.code.status.UserErrorStatus;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class AuthControllerDocsTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void socialSignupTokenExampleMatchesRequestContract() throws Exception {
        List<String> signupRequiredExamples = List.of(
                AuthControllerDocs.KAKAO_SIGNUP_REQUIRED_EXAMPLE,
                AuthControllerDocs.GOOGLE_SIGNUP_REQUIRED_EXAMPLE
        );

        for (String signupRequiredExample : signupRequiredExamples) {
            JsonNode example = objectMapper.readTree(signupRequiredExample);
            assertThat(example.path("result").path("signupToken").asText())
                    .matches("^[A-Za-z0-9_-]{43}$");
        }
    }

    @Test
    void socialSignupSuccessExamplesDocumentBothCompletionStatuses() throws Exception {
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

    @Test
    void socialLoginExamplesDocumentExistingAndNewUserBranches() throws Exception {
        assertSocialLoginStatuses(
                "KAKAO",
                AuthControllerDocs.KAKAO_LOGIN_COMPLETED_EXAMPLE,
                AuthControllerDocs.KAKAO_SIGNUP_REQUIRED_EXAMPLE
        );
        assertSocialLoginStatuses(
                "GOOGLE",
                AuthControllerDocs.GOOGLE_LOGIN_COMPLETED_EXAMPLE,
                AuthControllerDocs.GOOGLE_SIGNUP_REQUIRED_EXAMPLE
        );
    }

    @Test
    void allJsonExamplesAreValidAndPrettyPrinted() throws Exception {
        for (Field field : jsonExampleFields()) {
            String example = (String) field.get(null);

            assertThat(example)
                    .as(field.getName())
                    .startsWith("{\n")
                    .contains("\n  \"timestamp\"")
                    .endsWith("}\n");
            assertThat(objectMapper.readTree(example))
                    .as(field.getName())
                    .isNotNull();
        }
    }

    @Test
    void everyAuthOperationHasUniqueIdAndSuccessResponse() {
        List<Method> operationMethods = operationMethods();
        Set<String> operationIds = operationMethods.stream()
                .map(method -> method.getAnnotation(Operation.class).operationId())
                .collect(Collectors.toSet());

        assertThat(operationMethods).hasSize(14);
        assertThat(operationIds).hasSize(operationMethods.size());
        assertThat(operationIds).doesNotContain("");

        for (Method method : operationMethods) {
            ApiResponses responses = method.getAnnotation(ApiResponses.class);
            assertThat(responses)
                    .as(method.getName())
                    .isNotNull();
            assertThat(Arrays.stream(responses.value()).map(ApiResponse::responseCode))
                    .as(method.getName())
                    .contains("200");
        }
    }

    @Test
    void groupedErrorResponsesDescribeEveryExampleCodeOnItsOwnLine() throws Exception {
        for (Method method : operationMethods()) {
            ApiResponses responses = method.getAnnotation(ApiResponses.class);
            for (ApiResponse response : responses.value()) {
                if ("200".equals(response.responseCode())) {
                    continue;
                }
                assertThat(response.description())
                        .as(method.getName() + " HTTP " + response.responseCode())
                        .doesNotMatch("(?s).*\\b[A-Z]+\\d{3}/[A-Z]+\\d{3}\\b.*");

                List<String> descriptionLines = response.description().lines()
                        .map(String::trim)
                        .filter(line -> !line.isEmpty())
                        .toList();

                for (Content content : response.content()) {
                    for (ExampleObject example : content.examples()) {
                        assertThat(descriptionLines)
                                .as(method.getName() + " " + example.name())
                                .anySatisfy(line -> {
                                    assertThat(line).startsWith(example.name() + " - ");
                                    assertThat(line)
                                            .doesNotMatch("^" + example.name()
                                                    + "\\s*-\\s*.*\\b[A-Z]+\\d{3}\\b.*$");
                                });
                        assertThat(objectMapper.readTree(example.value()).path("code").asText())
                                .as(method.getName() + " " + example.name())
                                .isEqualTo(example.name());
                    }
                }
            }
        }
    }

    @Test
    void errorExamplesMatchRuntimeCodeMessageAndHttpStatus() throws Exception {
        List<BaseCodeDto> runtimeErrors = runtimeErrors();

        for (Method method : operationMethods()) {
            ApiResponses responses = method.getAnnotation(ApiResponses.class);
            for (ApiResponse response : responses.value()) {
                if ("200".equals(response.responseCode())) {
                    continue;
                }

                for (Content content : response.content()) {
                    for (ExampleObject example : content.examples()) {
                        JsonNode body = objectMapper.readTree(example.value());
                        String code = body.path("code").asText();
                        String message = body.path("message").asText();
                        BaseCodeDto runtimeError = runtimeErrors.stream()
                                .filter(error -> error.getCode().equals(code))
                                .filter(error -> error.getMessage().equals(message))
                                .findFirst()
                                .orElse(null);

                        assertThat(runtimeError)
                                .as(method.getName() + " " + example.name())
                                .isNotNull();
                        assertThat(runtimeError.getHttpStatus().value())
                                .as(method.getName() + " " + example.name())
                                .isEqualTo(Integer.parseInt(response.responseCode()));
                    }
                }
            }
        }
    }

    @Test
    void everyDeclaredErrorExampleIsUsedByAnAuthOperation() throws Exception {
        Set<String> usedExamples = operationMethods().stream()
                .map(method -> method.getAnnotation(ApiResponses.class))
                .flatMap(responses -> Arrays.stream(responses.value()))
                .filter(response -> !"200".equals(response.responseCode()))
                .flatMap(response -> Arrays.stream(response.content()))
                .flatMap(content -> Arrays.stream(content.examples()))
                .map(ExampleObject::value)
                .collect(Collectors.toSet());

        for (Field field : errorExampleFields()) {
            assertThat(usedExamples)
                    .as(field.getName())
                    .contains((String) field.get(null));
        }
    }

    private void assertSocialLoginStatuses(
            String provider,
            String loginCompletedExample,
            String signupRequiredExample
    ) throws Exception {
        JsonNode loginCompleted = objectMapper.readTree(loginCompletedExample).path("result");
        JsonNode signupRequired = objectMapper.readTree(signupRequiredExample).path("result");

        assertThat(loginCompleted.path("status").asText()).isEqualTo("LOGIN_COMPLETED");
        assertThat(loginCompleted.path("provider").asText()).isEqualTo(provider);
        assertThat(loginCompleted.path("accessToken").asText()).isNotBlank();
        assertThat(signupRequired.path("status").asText()).isEqualTo("SIGNUP_REQUIRED");
        assertThat(signupRequired.path("provider").asText()).isEqualTo(provider);
        assertThat(signupRequired.path("signupToken").asText()).isNotBlank();
        assertThat(signupRequired.path("accessToken").isNull()).isTrue();
    }

    private List<Field> jsonExampleFields() {
        return Arrays.stream(AuthControllerDocs.class.getFields())
                .filter(field -> Modifier.isStatic(field.getModifiers()))
                .filter(field -> field.getType() == String.class)
                .filter(field -> field.getName().endsWith("_EXAMPLE"))
                .filter(field -> !field.getName().contains("COOKIE"))
                .sorted((left, right) -> left.getName().compareTo(right.getName()))
                .toList();
    }

    private List<Field> errorExampleFields() {
        return jsonExampleFields().stream()
                .filter(field -> field.getName().matches("(AUTH|COMMON|USER)\\d{3}_EXAMPLE"))
                .toList();
    }

    private List<BaseCodeDto> runtimeErrors() {
        List<BaseCodeDto> errors = new ArrayList<>();
        Arrays.stream(AuthErrorStatus.values())
                .map(AuthErrorStatus::getCode)
                .forEach(errors::add);
        Arrays.stream(GlobalErrorStatus.values())
                .map(GlobalErrorStatus::getCode)
                .forEach(errors::add);
        Arrays.stream(UserErrorStatus.values())
                .map(UserErrorStatus::getCode)
                .forEach(errors::add);
        return errors;
    }

    private List<Method> operationMethods() {
        return Arrays.stream(AuthControllerDocs.class.getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(Operation.class))
                .toList();
    }
}
