package com.example.moodtail.domain.report.controller.docs;

import com.example.moodtail.domain.report.dto.response.MonthlyReportSharedImageResponse;
import com.example.moodtail.global.common.base.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Reports", description = "월간 리포트 공개 공유 API")
public interface MonthlyReportShareControllerDocs {

    String SUCCESS_EXAMPLE = """
            {
              "timestamp": "2026-08-09T14:30:00",
              "code": "COMMON200",
              "message": "요청에 성공했습니다.",
              "result": {
                "year": 2026,
                "month": 7,
                "shareImageUrl": "https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/reports/monthly/8d5f57e1-40e5-46b2-852d-1c3dd640efb8.png"
              }
            }
            """;
    String REPORT404_EXAMPLE = """
            {
              "timestamp": "2026-08-09T14:30:00",
              "code": "REPORT404",
              "message": "공유된 월간 리포트를 찾을 수 없습니다."
            }
            """;
    String COMMON500_EXAMPLE = """
            {
              "timestamp": "2026-08-09T14:30:00",
              "code": "COMMON500",
              "message": "서버 에러가 발생했습니다."
            }
            """;

    @Operation(
            operationId = "getSharedMonthlyReportImage",
            summary = "공유 월간 리포트 이미지 조회",
            description = "공유 토큰으로 리포트 연도·월과 공개 이미지를 조회합니다. "
                    + "공유 링크는 생성 후 30일 동안 조회할 수 있으며 인증이 필요하지 않습니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "COMMON200 - 공유 월간 리포트 조회 성공",
                    useReturnTypeSchema = true,
                    content = @Content(examples = @ExampleObject(
                            name = "COMMON200",
                            value = SUCCESS_EXAMPLE
                    ))),
            @ApiResponse(responseCode = "404", description = "REPORT404 - 토큰이 없거나 공유 기간이 만료됨",
                    content = @Content(examples = @ExampleObject(
                            name = "REPORT404",
                            value = REPORT404_EXAMPLE
                    ))),
            @ApiResponse(responseCode = "500", description = "COMMON500 - 서버 내부 오류",
                    content = @Content(examples = @ExampleObject(
                            name = "COMMON500",
                            value = COMMON500_EXAMPLE
                    )))
    })
    BaseResponse<MonthlyReportSharedImageResponse> getSharedImage(
            @Parameter(
                    description = "월간 리포트 공유 토큰",
                    example = "mr_hJ7JngQmYV4x0aP9k2LmN3Qr"
            )
            String shareToken
    );
}
