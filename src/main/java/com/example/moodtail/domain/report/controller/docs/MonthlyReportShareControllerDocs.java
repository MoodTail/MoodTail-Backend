package com.example.moodtail.domain.report.controller.docs;

import com.example.moodtail.domain.report.dto.response.MonthlyReportSharedImageResponse;
import com.example.moodtail.global.common.base.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

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
                "shareImageUrl": "https://mood-tail.site/api/v1/reports/monthly/shares/mr_hJ7JngQmYV4x0aP9k2LmN3Qr/image"
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
    String REPORT_IMAGE503_EXAMPLE = """
            {
              "timestamp": "2026-08-09T14:30:00",
              "code": "REPORT_IMAGE503",
              "message": "월간 리포트 공유 이미지를 일시적으로 저장할 수 없습니다."
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
            operationId = "getSharedImage",
            summary = "공유 월간 리포트 정보 조회",
            description = "공유 토큰으로 리포트 연도·월과 토큰 검증 이미지 URL을 조회합니다. "
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
    @SecurityRequirements
    BaseResponse<MonthlyReportSharedImageResponse> getSharedImage(
            @Parameter(
                    description = "월간 리포트 공유 URL 생성 API가 반환한 27자 공유 토큰",
                    required = true,
                    schema = @Schema(
                            pattern = "^mr_[A-Za-z0-9_-]{24}$",
                            minLength = 27,
                            maxLength = 27
                    ),
                    example = "mr_hJ7JngQmYV4x0aP9k2LmN3Qr"
            )
            String shareToken
    );

    @Operation(
            operationId = "getSharedImageFile",
            summary = "공유 월간 리포트 이미지 파일 조회",
            description = "공유 토큰의 유효기간을 검사한 뒤 저장된 월간 리포트 이미지를 백엔드 이미지 "
                    + "응답으로 반환합니다. 공유 링크 생성 후 30일 동안 인증 없이 조회할 수 있습니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "공유 월간 리포트 이미지 파일 조회 성공",
                    content = {
                            @Content(mediaType = MediaType.IMAGE_PNG_VALUE,
                                    schema = @Schema(types = {"string"}, format = "binary")),
                            @Content(mediaType = MediaType.IMAGE_JPEG_VALUE,
                                    schema = @Schema(types = {"string"}, format = "binary")),
                            @Content(mediaType = "image/webp",
                                    schema = @Schema(types = {"string"}, format = "binary"))
                    }),
            @ApiResponse(responseCode = "404", description = "REPORT404 - 토큰이 없거나 공유 기간이 만료됨",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(name = "REPORT404", value = REPORT404_EXAMPLE))),
            @ApiResponse(responseCode = "503", description = "REPORT_IMAGE503 - S3 이미지 조회 실패",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    name = "REPORT_IMAGE503",
                                    value = REPORT_IMAGE503_EXAMPLE
                            ))),
            @ApiResponse(responseCode = "500", description = "COMMON500 - 서버 내부 오류",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    name = "COMMON500",
                                    value = COMMON500_EXAMPLE
                            )))
    })
    @SecurityRequirements
    ResponseEntity<byte[]> getSharedImageFile(
            @Parameter(
                    description = "월간 리포트 공유 URL 생성 API가 반환한 27자 공유 토큰",
                    required = true,
                    schema = @Schema(
                            pattern = "^mr_[A-Za-z0-9_-]{24}$",
                            minLength = 27,
                            maxLength = 27
                    ),
                    example = "mr_hJ7JngQmYV4x0aP9k2LmN3Qr"
            )
            String shareToken
    );
}
