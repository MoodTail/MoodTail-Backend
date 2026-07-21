package com.example.moodtail.domain.moodtest.controller.docs;

import com.example.moodtail.domain.moodtest.dto.request.MoodTestResultShareCreateRequest;
import com.example.moodtail.domain.moodtest.dto.response.MoodTestResultShareCreateResponse;
import com.example.moodtail.domain.moodtest.dto.response.MoodTestResultResponse;
import com.example.moodtail.global.common.base.BaseResponse;
import com.example.moodtail.global.config.security.auth.PrincipalDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Mood Test Shares", description = "테스트 결과 공유 URL 생성 및 공개 결과 조회 API")
public interface MoodTestResultShareControllerDocs {

    String SHARED_RESULT_SUCCESS_EXAMPLE = MoodTestControllerDocs.RESULT_SUCCESS_EXAMPLE;
    String CREATE_SUCCESS_EXAMPLE = """
            {"timestamp":"2026-07-21T14:30:00","code":"COMMON200","message":"요청에 성공했습니다.","result":{"shareToken":"r_hJ7JngQmYV4x0aP9k2LmN3Qr","shareUrl":"https://api.moodtail.com/share/results/r_hJ7JngQmYV4x0aP9k2LmN3Qr"}}
            """;
    String COMMON402_EXAMPLE = """
            {"timestamp":"2026-07-21T14:30:00","code":"COMMON402","message":"입력값 검증에 실패했습니다."}
            """;
    String IMAGE400_EXAMPLE = """
            {"timestamp":"2026-07-21T14:30:00","code":"IMAGE400","message":"이미지 파일 형식이 올바르지 않습니다."}
            """;
    String COMMON401_EXAMPLE = """
            {"timestamp":"2026-07-21T14:30:00","code":"COMMON401","message":"인증이 필요합니다."}
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
    String MOOD_TEST404_EXAMPLE = """
            {"timestamp":"2026-07-21T14:30:00","code":"MOOD_TEST404","message":"공유된 테스트 결과를 찾을 수 없습니다."}
            """;
    String RECOMMENDATION422_EXAMPLE = """
            {"timestamp":"2026-07-21T14:30:00","code":"RECOMMENDATION422","message":"추천 결과를 산출할 수 없습니다."}
            """;
    String IMAGE413_EXAMPLE = """
            {"timestamp":"2026-07-21T14:30:00","code":"IMAGE413","message":"이미지 파일은 5MB 이하여야 합니다."}
            """;
    String COMMON500_EXAMPLE = """
            {"timestamp":"2026-07-21T14:30:00","code":"COMMON500","message":"서버 에러가 발생했습니다."}
            """;

    @Operation(operationId = "getSharedMoodTestResult", summary = "공유 테스트 결과 조회",
            description = "공유 토큰의 맛 프로필로 타입, 추천 칵테일 4종, 궁합 정보를 다시 산출합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "COMMON200 - 공유 테스트 결과 조회 성공", useReturnTypeSchema = true,
                    content = @Content(examples = @ExampleObject(name = "COMMON200", value = SHARED_RESULT_SUCCESS_EXAMPLE))),
            @ApiResponse(responseCode = "404", description = "MOOD_TEST404 - 공유 토큰에 해당하는 결과가 없음",
                    content = @Content(examples = @ExampleObject(name = "MOOD_TEST404", value = MOOD_TEST404_EXAMPLE))),
            @ApiResponse(responseCode = "422", description = "RECOMMENDATION422 - 추천 결과 산출 불가",
                    content = @Content(examples = @ExampleObject(name = "RECOMMENDATION422", value = RECOMMENDATION422_EXAMPLE))),
            @ApiResponse(responseCode = "500", description = "COMMON500 - 서버 내부 오류",
                    content = @Content(examples = @ExampleObject(name = "COMMON500", value = COMMON500_EXAMPLE)))
    })
    BaseResponse<MoodTestResultResponse> getSharedResult(
            @Parameter(description = "공유 결과 토큰", example = "r_hJ7JngQmYV4x0aP9k2LmN3Qr") String shareToken
    );

    @Operation(operationId = "createMoodTestResultShare", summary = "테스트 결과 공유 URL 생성",
            description = "게스트 또는 회원의 맛 프로필과 프론트엔드에서 생성한 썸네일을 저장하고 공유 토큰과 공개 URL을 반환합니다.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "COMMON200 - 공유 URL 생성 성공", useReturnTypeSchema = true,
                    content = @Content(examples = @ExampleObject(name = "COMMON200", value = CREATE_SUCCESS_EXAMPLE))),
            @ApiResponse(responseCode = "400", description = "COMMON402 - 맛 프로필 검증 실패\nIMAGE400 - 썸네일 형식·확장자 오류",
                    content = @Content(examples = {
                            @ExampleObject(name = "COMMON402", value = COMMON402_EXAMPLE),
                            @ExampleObject(name = "IMAGE400", value = IMAGE400_EXAMPLE)
                    })),
            @ApiResponse(responseCode = "401", description = "COMMON401/AUTH002/AUTH006/AUTH010 - 인증 토큰 또는 사용자 오류",
                    content = @Content(examples = {
                            @ExampleObject(name = "COMMON401", value = COMMON401_EXAMPLE),
                            @ExampleObject(name = "AUTH002", value = AUTH002_EXAMPLE),
                            @ExampleObject(name = "AUTH006", value = AUTH006_EXAMPLE),
                            @ExampleObject(name = "AUTH010", value = AUTH010_EXAMPLE)
                    })),
            @ApiResponse(responseCode = "403", description = "AUTH009/AUTH020 - 역할 불일치 또는 비활성 사용자",
                    content = @Content(examples = {
                            @ExampleObject(name = "AUTH009", value = AUTH009_EXAMPLE),
                            @ExampleObject(name = "AUTH020", value = AUTH020_EXAMPLE)
                    })),
            @ApiResponse(responseCode = "413", description = "IMAGE413 - 썸네일이 5MB를 초과함",
                    content = @Content(examples = @ExampleObject(name = "IMAGE413", value = IMAGE413_EXAMPLE))),
            @ApiResponse(responseCode = "500", description = "COMMON500 - S3 업로드 또는 서버 내부 오류",
                    content = @Content(examples = @ExampleObject(name = "COMMON500", value = COMMON500_EXAMPLE)))
    })
    BaseResponse<MoodTestResultShareCreateResponse> createShare(
            @Parameter(hidden = true) PrincipalDetails principalDetails,
            MoodTestResultShareCreateRequest request,
            @Parameter(description = "PNG, JPG, JPEG 또는 WEBP 형식의 5MB 이하 공유 썸네일") MultipartFile thumbnail
    );
}
