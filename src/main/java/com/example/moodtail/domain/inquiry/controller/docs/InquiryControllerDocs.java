package com.example.moodtail.domain.inquiry.controller.docs;

import com.example.moodtail.domain.inquiry.dto.request.InquiryCreateRequest;
import com.example.moodtail.domain.inquiry.dto.response.InquiryCreateResponse;
import com.example.moodtail.global.common.base.BaseResponse;
import com.example.moodtail.global.config.security.auth.PrincipalDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Inquiries", description = "회원 및 비회원 문의 접수 API")
public interface InquiryControllerDocs {

    String SUCCESS_EXAMPLE = """
            {"timestamp":"2026-07-21T14:30:00","code":"COMMON200","message":"요청에 성공했습니다.","result":{"inquiryId":15,"status":"PENDING","createdAt":"2026-07-21T14:30:00"}}
            """;
    String INQUIRY400_EXAMPLE = """
            {"timestamp":"2026-07-21T14:30:00","code":"INQUIRY400","message":"문의 요청이 올바르지 않습니다."}
            """;
    String AUTH002_EXAMPLE = """
            {"timestamp":"2026-07-21T14:30:00","code":"AUTH002","message":"만료된 JWT입니다."}
            """;
    String AUTH006_EXAMPLE = """
            {"timestamp":"2026-07-21T14:30:00","code":"AUTH006","message":"유효하지 않은 액세스 토큰입니다."}
            """;
    String AUTH010_EXAMPLE = """
            {"timestamp":"2026-07-21T14:30:00","code":"AUTH010","message":"존재하지 않는 사용자입니다."}
            """;
    String AUTH009_EXAMPLE = """
            {"timestamp":"2026-07-21T14:30:00","code":"AUTH009","message":"권한이 없습니다."}
            """;
    String AUTH020_EXAMPLE = """
            {"timestamp":"2026-07-21T14:30:00","code":"AUTH020","message":"비활성화된 사용자입니다."}
            """;
    String COMMON500_EXAMPLE = """
            {"timestamp":"2026-07-21T14:30:00","code":"COMMON500","message":"서버 에러가 발생했습니다."}
            """;

    @Operation(operationId = "createInquiry", summary = "문의하기",
            description = "회원과 비회원 모두 문의할 수 있습니다. Authorization 헤더는 선택이며, "
                    + "비회원은 contactEmail이 필수이고 회원은 생략할 수 있습니다. 문의 내용은 공백 제거 후 10~1000자입니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "COMMON200 - 문의 접수 성공", useReturnTypeSchema = true,
                    content = @Content(examples = @ExampleObject(name = "COMMON200", value = SUCCESS_EXAMPLE))),
            @ApiResponse(responseCode = "400", description = "INQUIRY400 - 문의 유형·내용·연락 이메일 오류",
                    content = @Content(examples = @ExampleObject(name = "INQUIRY400", value = INQUIRY400_EXAMPLE))),
            @ApiResponse(responseCode = "401", description = "AUTH002/AUTH006/AUTH010 - 전달한 인증 토큰 또는 사용자 오류",
                    content = @Content(examples = {
                            @ExampleObject(name = "AUTH002", value = AUTH002_EXAMPLE),
                            @ExampleObject(name = "AUTH006", value = AUTH006_EXAMPLE),
                            @ExampleObject(name = "AUTH010", value = AUTH010_EXAMPLE)
                    })),
            @ApiResponse(responseCode = "403", description = "AUTH009/AUTH020 - 역할 불일치 또는 비활성 사용자",
                    content = @Content(examples = {
                            @ExampleObject(name = "AUTH009", value = AUTH009_EXAMPLE),
                            @ExampleObject(name = "AUTH020", value = AUTH020_EXAMPLE)
                    })),
            @ApiResponse(responseCode = "500", description = "COMMON500 - 서버 내부 오류",
                    content = @Content(examples = @ExampleObject(name = "COMMON500", value = COMMON500_EXAMPLE)))
    })
    BaseResponse<InquiryCreateResponse> createInquiry(
            InquiryCreateRequest request,
            @Parameter(hidden = true) PrincipalDetails principalDetails
    );
}
