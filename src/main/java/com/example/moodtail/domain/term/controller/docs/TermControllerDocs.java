package com.example.moodtail.domain.term.controller.docs;

import com.example.moodtail.domain.term.dto.response.TermsResponse;
import com.example.moodtail.global.common.base.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Terms", description = "활성 약관 조회 API")
public interface TermControllerDocs {

    String SUCCESS_EXAMPLE = """
            {"timestamp":"2026-07-21T14:30:00","code":"COMMON200","message":"요청에 성공했습니다.","result":{"terms":[{"termId":1,"termType":"SERVICE","title":"서비스 이용약관","version":"1.0","required":true,"content":"MoodTail 서비스 이용약관 내용입니다."}]}}
            """;
    String TERM400_EXAMPLE = """
            {"timestamp":"2026-07-21T14:30:00","code":"TERM400","message":"약관 유형이 올바르지 않습니다."}
            """;
    String TERM404_EXAMPLE = """
            {"timestamp":"2026-07-21T14:30:00","code":"TERM404","message":"활성 약관 정보를 찾을 수 없습니다."}
            """;
    String COMMON500_EXAMPLE = """
            {"timestamp":"2026-07-21T14:30:00","code":"COMMON500","message":"서버 에러가 발생했습니다."}
            """;

    @Operation(operationId = "getTerms", summary = "약관 조회",
            description = "현재 활성화된 약관을 조회합니다. termType을 생략하면 모든 활성 약관을 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "COMMON200 - 약관 조회 성공", useReturnTypeSchema = true,
                    content = @Content(examples = @ExampleObject(name = "COMMON200", value = SUCCESS_EXAMPLE))),
            @ApiResponse(responseCode = "400", description = "TERM400 - 빈 값 또는 지원하지 않는 약관 유형",
                    content = @Content(examples = @ExampleObject(name = "TERM400", value = TERM400_EXAMPLE))),
            @ApiResponse(responseCode = "404", description = "TERM404 - 조건에 맞는 활성 약관이 없음",
                    content = @Content(examples = @ExampleObject(name = "TERM404", value = TERM404_EXAMPLE))),
            @ApiResponse(responseCode = "500", description = "COMMON500 - 서버 내부 오류",
                    content = @Content(examples = @ExampleObject(name = "COMMON500", value = COMMON500_EXAMPLE)))
    })
    BaseResponse<TermsResponse> getTerms(
            @Parameter(description = "약관 유형. 생략 시 전체 조회", example = "SERVICE",
                    schema = @Schema(allowableValues = {"SERVICE", "PRIVACY", "MARKETING"}))
            String termType
    );
}
