package com.example.moodtail.domain.recommendation.controller.docs;

import com.example.moodtail.domain.recommendation.dto.response.PairRecommendationShareImageResponse;
import com.example.moodtail.global.common.base.BaseResponse;
import com.example.moodtail.global.config.security.auth.PrincipalDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Pair Recommendation Shares", description = "페어 추천 결과 공유 URL 생성 및 공개 결과 조회 API")
@SecurityRequirement(name = "bearerAuth")
public interface PairRecommendationShareImageControllerDocs {

    String UPLOAD_SHARE_IMAGE_SUCCESS_EXAMPLE = """
            {"timestamp":"2026-07-29T14:30:00","code":"COMMON200","message":"요청에 성공했습니다.","result":{"shareImageUrl":"https://moodtail-bucket.s3.ap-northeast-2.amazonaws.com/recommendations/pair/9f3ab21c-1234-4a56-9abc-7890def12345.png"}}
            """;
    String COMMON401_EXAMPLE = """
            {"timestamp":"2026-07-29T14:30:00","code":"COMMON401","message":"인증이 필요합니다."}
            """;
    String AUTH006_EXAMPLE = """
            {"timestamp":"2026-07-29T14:30:00","code":"AUTH006","message":"유효하지 않은 액세스 토큰입니다."}
            """;
    String INVITE_CODE404_EXAMPLE = """
            {"timestamp":"2026-07-29T14:30:00","code":"INVITE_CODE404","message":"초대 코드에 해당하는 사용자를 찾을 수 없습니다."}
            """;
    String MOOD_TEST_404_EXAMPLE = """
            {"timestamp":"2026-07-29T14:30:00","code":"MOOD_TEST_404","message":"테스트 결과를 찾을 수 없습니다."}
            """;
    String RECOMMENDATION422_EXAMPLE = """
            {"timestamp":"2026-07-29T14:30:00","code":"RECOMMENDATION422","message":"추천 결과를 산출할 수 없습니다."}
            """;
    String IMAGE400_EXAMPLE = """
            {"timestamp":"2026-07-29T14:30:00","code":"IMAGE400","message":"이미지 파일 형식이 올바르지 않습니다."}
            """;
    String IMAGE413_EXAMPLE = """
            {"timestamp":"2026-07-29T14:30:00","code":"IMAGE413","message":"이미지 파일은 5MB 이하여야 합니다."}
            """;
    String PAIR_IMAGE503_EXAMPLE = """
            {"timestamp":"2026-07-29T14:30:00","code":"PAIR_IMAGE503","message":"페어 추천 공유 이미지를 일시적으로 저장할 수 없습니다."}
            """;
    String COMMON500_EXAMPLE = """
            {"timestamp":"2026-07-29T14:30:00","code":"COMMON500","message":"서버 에러가 발생했습니다."}
            """;

    @Operation(
            operationId = "uploadPairRecommendationShareImage",
            summary = "페어 추천 결과 공유 이미지 업로드",
            description = "partnerInviteCode로 상대방을 다시 조회해 페어 추천 결과(초대 코드 유효성, 양쪽 최신 감정 테스트 결과 존재 여부)를 "
                    + "재검증한 뒤, 전달받은 이미지를 S3에 저장하고 공유 이미지 URL을 반환합니다. 로그인이 필요합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "COMMON200 - 페어 추천 공유 이미지 업로드 성공",
                    useReturnTypeSchema = true,
                    content = @Content(examples = @ExampleObject(
                            name = "COMMON200",
                            value = UPLOAD_SHARE_IMAGE_SUCCESS_EXAMPLE
                    ))),
            @ApiResponse(responseCode = "400", description = "IMAGE400 - 이미지가 없거나 지원하지 않는 파일 형식",
                    content = @Content(examples = @ExampleObject(name = "IMAGE400", value = IMAGE400_EXAMPLE))),
            @ApiResponse(responseCode = "401",
                    description = "COMMON401/AUTH006 - 인증 정보 없음 또는 유효하지 않은 액세스 토큰",
                    content = @Content(examples = {
                            @ExampleObject(name = "COMMON401", value = COMMON401_EXAMPLE),
                            @ExampleObject(name = "AUTH006", value = AUTH006_EXAMPLE)
                    })),
            @ApiResponse(responseCode = "404",
                    description = "INVITE_CODE404/MOOD_TEST_404 - 존재하지 않는 초대 코드 또는 테스트 결과 없음",
                    content = @Content(examples = {
                            @ExampleObject(name = "INVITE_CODE404", value = INVITE_CODE404_EXAMPLE),
                            @ExampleObject(name = "MOOD_TEST_404", value = MOOD_TEST_404_EXAMPLE)
                    })),
            @ApiResponse(responseCode = "413", description = "IMAGE413 - 이미지 파일이 5MB를 초과함",
                    content = @Content(examples = @ExampleObject(name = "IMAGE413", value = IMAGE413_EXAMPLE))),
            @ApiResponse(responseCode = "422", description = "RECOMMENDATION422 - 타협 추천 산출 불가",
                    content = @Content(examples = @ExampleObject(name = "RECOMMENDATION422", value = RECOMMENDATION422_EXAMPLE))),
            @ApiResponse(responseCode = "503", description = "PAIR_IMAGE503 - 공유 이미지를 S3에 저장하지 못함",
                    content = @Content(examples = @ExampleObject(name = "PAIR_IMAGE503", value = PAIR_IMAGE503_EXAMPLE))),
            @ApiResponse(responseCode = "500", description = "COMMON500 - 서버 내부 오류",
                    content = @Content(examples = @ExampleObject(name = "COMMON500", value = COMMON500_EXAMPLE)))
    })
    BaseResponse<PairRecommendationShareImageResponse> uploadPairRecommendationShareImage(
            PrincipalDetails principalDetails,
            String partnerInviteCode,
            MultipartFile image
    );
}
