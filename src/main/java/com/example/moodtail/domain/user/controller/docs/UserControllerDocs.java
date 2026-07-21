package com.example.moodtail.domain.user.controller.docs;

import com.example.moodtail.domain.collection.dto.response.MoodTypesResponse;
import com.example.moodtail.domain.user.dto.request.UserProfileUpdateRequest;
import com.example.moodtail.domain.user.dto.response.MyPageResponse;
import com.example.moodtail.domain.user.dto.response.UserProfileUpdateResponse;
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

public interface UserControllerDocs {

    String MY_PAGE_SUCCESS_EXAMPLE = """
            {"timestamp":"2026-07-21T14:30:00","code":"COMMON200","message":"요청에 성공했습니다.","result":{"userId":37,"nickname":"푸미","representativeMoodType":{"moodTypeId":2001,"typeCode":"TYPE01","name":"몽글몽글 낭만파","characterImageUrl":"https://cdn.moodtail.com/mood-types/type01.png"},"totalTestCount":12,"monthlyRecordCount":4,"unlockedMoodTypeCount":3}}
            """;
    String MOOD_TYPES_SUCCESS_EXAMPLE = """
            {"timestamp":"2026-07-21T14:30:00","code":"COMMON200","message":"요청에 성공했습니다.","result":{"totalCount":2,"moodTypes":[{"moodTypeId":2001,"typeCode":"TYPE01","name":"몽글몽글 낭만파","shortDescription":"부드러운 달콤함 속에서 여유를 즐기는 타입","characterImageUrl":"https://cdn.moodtail.com/mood-types/type01.png","unlockedAt":"2026-07-20T21:15:00"},{"moodTypeId":2002,"typeCode":"TYPE02","name":"반짝이는 모험가","shortDescription":"새로운 자극을 즐기는 타입","characterImageUrl":"https://cdn.moodtail.com/mood-types/type02.png","unlockedAt":null}]}}
            """;
    String PROFILE_UPDATE_SUCCESS_EXAMPLE = """
            {"timestamp":"2026-07-21T14:30:00","code":"COMMON200","message":"요청에 성공했습니다.","result":{"userId":37,"nickname":"새로운푸미","representativeMoodType":{"moodTypeId":2001,"typeCode":"TYPE01","name":"몽글몽글 낭만파","characterImageUrl":"https://cdn.moodtail.com/mood-types/type01.png"}}}
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
    String AUTH027_EXAMPLE = """
            {"timestamp":"2026-07-21T14:30:00","code":"AUTH027","message":"로그인 사용자만 이용할 수 있습니다."}
            """;
    String USER400_NICKNAME_EXAMPLE = """
            {"timestamp":"2026-07-21T14:30:00","code":"USER400","message":"닉네임 입력값이 올바르지 않습니다."}
            """;
    String USER400_UPDATE_EXAMPLE = """
            {"timestamp":"2026-07-21T14:30:00","code":"USER400","message":"프로필 수정 요청이 올바르지 않습니다."}
            """;
    String USER400_MOOD_TYPE_EXAMPLE = """
            {"timestamp":"2026-07-21T14:30:00","code":"USER400","message":"해금한 무드타입만 대표 캐릭터로 지정할 수 있습니다."}
            """;
    String COMMON500_EXAMPLE = """
            {"timestamp":"2026-07-21T14:30:00","code":"COMMON500","message":"서버 에러가 발생했습니다."}
            """;

    @Tag(name = "Users", description = "마이페이지 및 사용자 프로필 API")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(operationId = "getMyPage", summary = "마이페이지 조회",
            description = "프로필, 전체 테스트 횟수, 이번 달 음주 기록 수와 해금한 무드 타입 수를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "COMMON200 - 마이페이지 조회 성공", useReturnTypeSchema = true,
                    content = @Content(examples = @ExampleObject(name = "COMMON200", value = MY_PAGE_SUCCESS_EXAMPLE))),
            @ApiResponse(responseCode = "401", description = "COMMON401/AUTH002/AUTH006/AUTH010 - 인증 토큰 또는 사용자 오류",
                    content = @Content(examples = {
                            @ExampleObject(name = "COMMON401", value = COMMON401_EXAMPLE),
                            @ExampleObject(name = "AUTH002", value = AUTH002_EXAMPLE),
                            @ExampleObject(name = "AUTH006", value = AUTH006_EXAMPLE),
                            @ExampleObject(name = "AUTH010", value = AUTH010_EXAMPLE)
                    })),
            @ApiResponse(responseCode = "403", description = "AUTH009/AUTH020/AUTH027 - 로그인 사용자 권한 필요",
                    content = @Content(examples = {
                            @ExampleObject(name = "AUTH009", value = AUTH009_EXAMPLE),
                            @ExampleObject(name = "AUTH020", value = AUTH020_EXAMPLE),
                            @ExampleObject(name = "AUTH027", value = AUTH027_EXAMPLE)
                    })),
            @ApiResponse(responseCode = "500", description = "COMMON500 - 서버 내부 오류",
                    content = @Content(examples = @ExampleObject(name = "COMMON500", value = COMMON500_EXAMPLE)))
    })
    BaseResponse<MyPageResponse> getMyPage(
            @Parameter(hidden = true) PrincipalDetails principalDetails
    );

    @Tag(name = "Users", description = "마이페이지 및 사용자 프로필 API")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(operationId = "getUnlockedMoodTypes", summary = "해금된 무드 타입 목록 조회",
            description = "전체 무드 타입을 조회하며 각 타입의 unlockedAt 값으로 사용자의 해금 여부를 제공합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "COMMON200 - 무드 타입 목록 조회 성공", useReturnTypeSchema = true,
                    content = @Content(examples = @ExampleObject(name = "COMMON200", value = MOOD_TYPES_SUCCESS_EXAMPLE))),
            @ApiResponse(responseCode = "401", description = "COMMON401/AUTH002/AUTH006 - 인증 토큰 오류",
                    content = @Content(examples = {
                            @ExampleObject(name = "COMMON401", value = COMMON401_EXAMPLE),
                            @ExampleObject(name = "AUTH002", value = AUTH002_EXAMPLE),
                            @ExampleObject(name = "AUTH006", value = AUTH006_EXAMPLE)
                    })),
            @ApiResponse(responseCode = "403", description = "AUTH009/AUTH020/AUTH027 - 로그인 사용자 권한 필요",
                    content = @Content(examples = {
                            @ExampleObject(name = "AUTH009", value = AUTH009_EXAMPLE),
                            @ExampleObject(name = "AUTH020", value = AUTH020_EXAMPLE),
                            @ExampleObject(name = "AUTH027", value = AUTH027_EXAMPLE)
                    })),
            @ApiResponse(responseCode = "500", description = "COMMON500 - 서버 내부 오류",
                    content = @Content(examples = @ExampleObject(name = "COMMON500", value = COMMON500_EXAMPLE)))
    })
    BaseResponse<MoodTypesResponse> getMoodTypes(
            @Parameter(hidden = true) PrincipalDetails principalDetails
    );

    @Tag(name = "Users", description = "마이페이지 및 사용자 프로필 API")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(operationId = "updateUserProfile", summary = "프로필 수정",
            description = "닉네임 또는 대표 무드 타입 중 하나 이상을 수정합니다. 대표 타입은 이미 해금한 타입만 지정할 수 있습니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "COMMON200 - 프로필 수정 성공", useReturnTypeSchema = true,
                    content = @Content(examples = @ExampleObject(name = "COMMON200", value = PROFILE_UPDATE_SUCCESS_EXAMPLE))),
            @ApiResponse(responseCode = "400", description = "USER400 - 수정 필드 누락, 닉네임 오류 또는 미해금 대표 타입",
                    content = @Content(examples = {
                            @ExampleObject(name = "수정 필드 누락", value = USER400_UPDATE_EXAMPLE),
                            @ExampleObject(name = "닉네임 오류", value = USER400_NICKNAME_EXAMPLE),
                            @ExampleObject(name = "미해금 대표 타입", value = USER400_MOOD_TYPE_EXAMPLE)
                    })),
            @ApiResponse(responseCode = "401", description = "COMMON401/AUTH002/AUTH006/AUTH010 - 인증 토큰 또는 사용자 오류",
                    content = @Content(examples = {
                            @ExampleObject(name = "COMMON401", value = COMMON401_EXAMPLE),
                            @ExampleObject(name = "AUTH002", value = AUTH002_EXAMPLE),
                            @ExampleObject(name = "AUTH006", value = AUTH006_EXAMPLE),
                            @ExampleObject(name = "AUTH010", value = AUTH010_EXAMPLE)
                    })),
            @ApiResponse(responseCode = "403", description = "AUTH009/AUTH020/AUTH027 - 로그인 사용자 권한 필요",
                    content = @Content(examples = {
                            @ExampleObject(name = "AUTH009", value = AUTH009_EXAMPLE),
                            @ExampleObject(name = "AUTH020", value = AUTH020_EXAMPLE),
                            @ExampleObject(name = "AUTH027", value = AUTH027_EXAMPLE)
                    })),
            @ApiResponse(responseCode = "500", description = "COMMON500 - 서버 내부 오류",
                    content = @Content(examples = @ExampleObject(name = "COMMON500", value = COMMON500_EXAMPLE)))
    })
    BaseResponse<UserProfileUpdateResponse> updateProfile(
            @Parameter(hidden = true) PrincipalDetails principalDetails,
            UserProfileUpdateRequest request
    );
}
