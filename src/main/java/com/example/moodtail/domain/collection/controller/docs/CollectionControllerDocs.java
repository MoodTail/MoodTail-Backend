package com.example.moodtail.domain.collection.controller.docs;

import com.example.moodtail.domain.collection.dto.request.RepresentativeMoodTypeUpdateRequest;
import com.example.moodtail.domain.collection.dto.response.CollectionResponse;
import com.example.moodtail.domain.collection.dto.response.CollectionShareCreateResponse;
import com.example.moodtail.domain.collection.dto.response.RepresentativeMoodTypeUpdateResponse;
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

@Tag(
        name = "Collections",
        description = "도감 조회, 대표 타입 변경 및 도감 공유 API"
)
public interface CollectionControllerDocs {

    String COLLECTION_SUCCESS_EXAMPLE = """
              {
                "timestamp": "2026-07-22T14:30:00",
                "code": "COMMON200",
                "message": "요청에 성공했습니다.",
                "result": {
                  "representativeMoodType": {
                    "moodTypeId": 1,
                    "typeCode": "FRESH_SPARK",
                    "name": "상큼한 스파클러",
                    "characterImageUrl":
                    "https://cdn.moodtail.com/images/fresh-spark.png"
                  },
                  "unlockedMoodTypeCount": 3,
                  "moodTypes": [
                    {
                      "moodTypeId": 1,
                      "typeCode": "FRESH_SPARK",
                      "name": "상큼한 스파클러",
                      "unlocked": true,
                      "collectionRate": 75,
                      "collectedUserCount": 3,
                      "characterImageUrl":
                      "https://cdn.moodtail.com/images/fresh-spark.png"
                    },
                    {
                      "moodTypeId": 2,
                      "typeCode": "DEEP_NIGHT",
                      "name": "깊은 밤의 무드",
                      "unlocked": false,
                      "collectionRate": 0,
                      "collectedUserCount": 0,
                      "characterImageUrl":
                      "https://cdn.moodtail.com/images/deep-night.png"
                    }
                  ]
                }
              }
              """;

    String REPRESENTATIVE_UPDATE_SUCCESS_EXAMPLE = """
              {
                "timestamp": "2026-07-22T14:30:00",
                "code": "COMMON200",
                "message": "요청에 성공했습니다.",
                "result": {
                  "moodTypeId": 1,
                  "typeCode": "FRESH_SPARK",
                  "name": "상큼한 스파클러",
                  "characterImageUrl":
                  "https://cdn.moodtail.com/images/fresh-spark.png"
                }
              }
              """;

    String CREATE_SHARE_SUCCESS_EXAMPLE = """
              {
                "timestamp": "2026-07-22T14:30:00",
                "code": "COMMON200",
                "message": "요청에 성공했습니다.",
                "result": {
                  "shareToken": "c_hJ7JngQmYV4x0aP9k2LmN3Qr",
                  "shareUrl":
                  "https://api.moodtail.com/share/collections/c_hJ7JngQmYV4x0aP9k2LmN3Qr?v=1"                                                                                 
                }
              }
              """;

    String COMMON401_EXAMPLE = """
              {
                "timestamp": "2026-07-22T14:30:00",
                "code": "COMMON401",
                "message": "인증이 필요합니다."
              }
              """;

    String AUTH006_EXAMPLE = """
              {
                "timestamp": "2026-07-22T14:30:00",
                "code": "AUTH006",
                "message": "유효하지 않은 액세스 토큰입니다."
              }
              """;

    String AUTH010_EXAMPLE = """
              {
                "timestamp": "2026-07-22T14:30:00",
                "code": "AUTH010",
                "message": "존재하지 않는 사용자입니다."
              }
              """;

    String COMMON402_EXAMPLE = """
              {
                "timestamp": "2026-07-22T14:30:00",
                "code": "COMMON402",
                "message": "입력값 검증에 실패했습니다."
              }
              """;

    String MOOD_TYPE404_EXAMPLE = """
              {
                "timestamp": "2026-07-22T14:30:00",
                "code": "MOOD_TYPE404",
                "message": "해당 칵테일 타입을 찾을 수 없습니다."
              }
              """;

    String COLLECTION_MOOD_TYPE400_EXAMPLE = """
              {
                "timestamp": "2026-07-22T14:30:00",
                "code": "COLLECTION_MOOD_TYPE400",
                "message": "해금한 무드 타입만 대표 타입으로 지정할 수 있습니다."
              }
              """;

    String IMAGE400_EXAMPLE = """
              {
                "timestamp": "2026-07-22T14:30:00",
                "code": "IMAGE400",
                "message": "이미지 파일 형식이 올바르지 않습니다."
              }
              """;

    String IMAGE413_EXAMPLE = """
              {
                "timestamp": "2026-07-22T14:30:00",
                "code": "IMAGE413",
                "message": "이미지 파일은 5MB 이하여야 합니다."
              }
              """;

    String COLLECTION_SHARE404_EXAMPLE = """
              {
                "timestamp": "2026-07-22T14:30:00",
                "code": "COLLECTION_SHARE404",
                "message": "공유된 도감을 찾을 수 없습니다."
              }
              """;

    String COMMON500_EXAMPLE = """
              {
                "timestamp": "2026-07-22T14:30:00",
                "code": "COMMON500",
                "message": "서버 오류가 발생했습니다."
              }
              """;


    @Operation(
            operationId = "getCollection",
            summary = "도감 전체 조회",
            description = """
                      사용자의 대표 무드 타입과 전체 무드 타입별 해금 여부 및 칵테일 수집률을 조회합니다.
                      로그인한 회원만 사용할 수 있습니다.
                      """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "COMMON200 - 도감 조회 성공",
                    useReturnTypeSchema = true,
                    content = @Content(
                            examples = @ExampleObject(
                                    name = "COMMON200",
                                    value = COLLECTION_SUCCESS_EXAMPLE
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "COMMON401/AUTH006/AUTH010 - 인증 토큰 또는 사용자 오류",
            content = @Content(examples = {
            @ExampleObject(
                    name = "COMMON401",
                    value = COMMON401_EXAMPLE
            ),
            @ExampleObject(
                    name = "AUTH006",
                    value = AUTH006_EXAMPLE
            ),
            @ExampleObject(
                    name = "AUTH010",
                    value = AUTH010_EXAMPLE
            )
    })
              ),
            @ApiResponse(
                    responseCode = "500",
                    description = "COMMON500 - 서버 내부 오류",
                    content = @Content(
                            examples = @ExampleObject(
                                    name = "COMMON500",
                                    value = COMMON500_EXAMPLE
                            )
                    )
            )
    })
    BaseResponse<CollectionResponse> getCollection(
            @Parameter(hidden = true)
            PrincipalDetails principalDetails
    );


    @Operation(
            operationId = "updateRepresentativeMoodType",
            summary = "대표 무드 타입 변경",
            description = """
                      사용자가 해금한 무드 타입을 대표 타입으로 지정합니다.
                      존재하지 않는 타입 또는 해금하지 않은 타입은 대표 타입으로 지정할 수 없습니다.
                      """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "COMMON200 - 대표 무드 타입 변경 성공",
                    useReturnTypeSchema = true,
                    content = @Content(
                            examples = @ExampleObject(
                                    name = "COMMON200",
                                    value = REPRESENTATIVE_UPDATE_SUCCESS_EXAMPLE
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "COMMON402/COLLECTION_MOOD_TYPE400 - 입력값 검증 실패 또는 미해금 타입",
                    content = @Content(examples = {
                            @ExampleObject(
                                    name = "COMMON402",
                                    value = COMMON402_EXAMPLE
                            ),
                            @ExampleObject(
                                    name = "COLLECTION_MOOD_TYPE400",
                                    value = COLLECTION_MOOD_TYPE400_EXAMPLE
                            )
                    })
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "COMMON401/AUTH006/AUTH010 - 인증 토큰 또는 사용자 오류",
                    content = @Content(examples = {
                            @ExampleObject(
                                    name = "COMMON401",
                                    value = COMMON401_EXAMPLE
                            ),
                            @ExampleObject(
                                    name = "AUTH006",
                                    value = AUTH006_EXAMPLE
                            ),
                            @ExampleObject(
                                    name = "AUTH010",
                                    value = AUTH010_EXAMPLE
                            )
                    })
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "MOOD_TYPE404 - 요청한 무드 타입이 존재하지 않음",
                    content = @Content(
                            examples = @ExampleObject(
                                    name = "MOOD_TYPE404",
                                    value = MOOD_TYPE404_EXAMPLE
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "COMMON500 - 서버 내부 오류",
                    content = @Content(
                            examples = @ExampleObject(
                                    name = "COMMON500",
                                    value = COMMON500_EXAMPLE
                            )
                    )
            )
    })
    BaseResponse<RepresentativeMoodTypeUpdateResponse>
    updateRepresentativeMoodType(
            @Parameter(hidden = true)
            PrincipalDetails principalDetails,

            RepresentativeMoodTypeUpdateRequest request
    );

    @Operation(
            operationId = "createOrUpdateShare",
            summary = "도감 공유 URL 생성",
            description = """
                      프론트엔드에서 생성한 도감 공유 이미지를 저장하고 공유 URL을 반환합니다.
                      사용자별 공유 토큰은 하나만 유지됩니다.
                      기존 공유 도감이 있으면 토큰은 유지하고 이미지와 버전을 갱신합니다.
                      """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "COMMON200 - 도감 공유 URL 생성 또는 갱신 성공",
                    useReturnTypeSchema = true,
                    content = @Content(
                            examples = @ExampleObject(
                                    name = "COMMON200",
                                    value = CREATE_SHARE_SUCCESS_EXAMPLE
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "IMAGE400 - 이미지가 비었거나 형식 또는 확장자가 올 바르지 않음",
                    content = @Content(
                    examples = @ExampleObject(name = "IMAGE400", value = IMAGE400_EXAMPLE))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "COMMON401/AUTH006/AUTH010 - 인증 토큰 또는 사용자 오류",
                    content = @Content(examples = {
                            @ExampleObject(
                                    name = "COMMON401",
                                    value = COMMON401_EXAMPLE
                            ),
                            @ExampleObject(
                                    name = "AUTH006",
                                    value = AUTH006_EXAMPLE
                            ),
                            @ExampleObject(
                                    name = "AUTH010",
                                    value = AUTH010_EXAMPLE
                            )
                    })
              ),
    @ApiResponse(
            responseCode = "413",
            description = "IMAGE413 - 이미지가 5MB를 초과함",
            content = @Content(examples = @ExampleObject(name = "IMAGE413", value = IMAGE413_EXAMPLE))
    ),
    @ApiResponse(
            responseCode = "500",
            description = "COMMON500 - 이미지 저장 또는 서버 내부 오류",
            content = @Content(examples = @ExampleObject(name = "COMMON500", value = COMMON500_EXAMPLE)))
    })
    BaseResponse<CollectionShareCreateResponse> createOrUpdateShare(
            @Parameter(hidden = true)
            PrincipalDetails principalDetails,

            @Parameter(
                    description = "PNG, JPG, JPEG 또는 WEBP 형식의 5MB 이하 도감 공유 이미지"
            )
            MultipartFile thumbnail
    );


    @Operation(
            operationId = "getSharedCollection",
            summary = "공유 도감 조회",
            description = """
                      공유 토큰을 사용하여 해당 사용자의 현재 도감 상태를 조회합니다.
                      공유 링크를 받은 사용자는 로그인하지 않아도 조회할 수 있습니다.
                      조회 시점의 최신 도감 상태를 반환합니다.
                      """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "COMMON200 - 공유 도감 조회 성공",
                    useReturnTypeSchema = true,
                    content = @Content(
                            examples = @ExampleObject(
                                    name = "COMMON200",
                                    value = COLLECTION_SUCCESS_EXAMPLE
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "COLLECTION_SHARE404 - 공유 토큰에 해당하는 도감이 없음",
                    content = @Content(
                            examples = @ExampleObject(
                                    name = "COLLECTION_SHARE404",
                                    value = COLLECTION_SHARE404_EXAMPLE
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "COMMON500 - 서버 내부 오류",
                    content = @Content(
                            examples = @ExampleObject(
                                    name = "COMMON500",
                                    value = COMMON500_EXAMPLE
                            )
                    )
            )
    })
    BaseResponse<CollectionResponse> getSharedCollection(
            @Parameter(
                    description = "도감 공유 토큰",
                    example = "c_hJ7JngQmYV4x0aP9k2LmN3Qr"
            )
            String shareToken
    );

}