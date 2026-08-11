package com.example.moodtail.domain.report.controller;

import com.example.moodtail.domain.report.controller.docs.MonthlyReportShareControllerDocs;
import com.example.moodtail.domain.report.controller.docs.MonthlyReportSharePageControllerDocs;
import com.example.moodtail.global.common.exception.code.BaseCodeDto;
import com.example.moodtail.global.common.exception.code.status.ReportErrorStatus;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class MonthlyReportOpenApiSchemaTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void sharedImageFileDocumentsBinaryMediaTypesAndServerError() throws Exception {
        Method method = MonthlyReportShareControllerDocs.class.getDeclaredMethod(
                "getSharedImageFile",
                String.class
        );
        Operation operation = method.getAnnotation(Operation.class);
        ApiResponses responses = method.getAnnotation(ApiResponses.class);
        ApiResponse success = response(responses, "200");

        assertThat(operation.description()).doesNotContain("비공개 S3 객체");
        assertThat(operation.description()).contains("백엔드 이미지 응답");
        assertThat(success.content()).hasSize(3);
        for (Content content : success.content()) {
            assertThat(content.schema().types()).containsExactly("string");
            assertThat(content.schema().format()).isEqualTo("binary");
        }
        assertThat(response(responses, "500")).isNotNull();
    }

    @Test
    void sharePageDocumentsHtmlAsString() throws Exception {
        Method method = MonthlyReportSharePageControllerDocs.class.getDeclaredMethod(
                "getSharePage",
                String.class
        );
        ApiResponse success = response(method.getAnnotation(ApiResponses.class), "200");

        assertThat(success.content()).hasSize(1);
        assertThat(success.content()[0].schema().types()).containsExactly("string");
    }

    @Test
    void shareImageUnavailableExampleMatchesErrorStatus() throws Exception {
        JsonNode example = objectMapper.readTree(
                MonthlyReportShareControllerDocs.REPORT_IMAGE503_EXAMPLE
        );
        BaseCodeDto status = ReportErrorStatus.SHARE_IMAGE_UNAVAILABLE.getCode();

        assertThat(example.path("code").asText()).isEqualTo(status.getCode());
        assertThat(example.path("message").asText()).isEqualTo(status.getMessage());
    }

    private ApiResponse response(ApiResponses responses, String responseCode) {
        return Arrays.stream(responses.value())
                .filter(response -> response.responseCode().equals(responseCode))
                .findFirst()
                .orElseThrow();
    }
}
