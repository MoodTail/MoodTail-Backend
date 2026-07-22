package com.example.moodtail.domain.history.controller.docs;

import com.example.moodtail.domain.history.dto.request.HistoryCreateRequest;
import com.example.moodtail.domain.history.dto.request.HistoryUpdateRequest;
import com.example.moodtail.domain.history.dto.response.HistoryCalendarResponse;
import com.example.moodtail.domain.history.dto.response.HistoryCreateResponse;
import com.example.moodtail.domain.history.dto.response.HistoryDateResponse;
import com.example.moodtail.domain.history.dto.response.HistoryDetailResponse;
import com.example.moodtail.domain.history.dto.response.HistoryPhotoResponse;
import com.example.moodtail.domain.history.dto.response.HistoryTestResultDetailResponse;
import com.example.moodtail.domain.history.dto.response.HistoryUpdateResponse;
import com.example.moodtail.global.common.base.BaseResponse;
import com.example.moodtail.global.config.security.auth.PrincipalDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Histories", description = "일별 테스트 기록과 음주 기록 조회 및 관리 API")
@SecurityRequirement(name = "bearerAuth")
public interface HistoryControllerDocs {

    @Operation(
            operationId = "getHistoryCalendar",
            summary = "월간 히스토리 조회",
            description = "월 단위 테스트 결과와 음주 기록 존재 여부, 월간 리포트 열람 가능 여부를 조회합니다."
    )
    BaseResponse<HistoryCalendarResponse> getHistoryCalendar(
            @Parameter(hidden = true) PrincipalDetails principal,
            @Parameter(description = "조회 연도", example = "2026") int year,
            @Parameter(description = "조회 월(1~12)", example = "7") int month
    );

    @Operation(
            operationId = "getHistoryByDate",
            summary = "날짜별 히스토리 조회",
            description = "선택한 날짜의 테스트 결과 요약, 음주 기록 1건과 날짜별 사진을 조회합니다."
    )
    BaseResponse<HistoryDateResponse> getHistoryByDate(
            @Parameter(hidden = true) PrincipalDetails principal,
            @Parameter(description = "조회 날짜(yyyy-MM-dd)", example = "2026-07-05") String date
    );

    @Operation(
            operationId = "getTestResultDetail",
            summary = "저장된 테스트 결과 상세 조회",
            description = "무드 타입, 맛 프로필, 당시 추천 칵테일을 조회합니다."
    )
    BaseResponse<HistoryTestResultDetailResponse> getTestResultDetail(
            @Parameter(hidden = true) PrincipalDetails principal,
            @Parameter(description = "테스트 결과 ID", example = "10") Long resultId
    );

    @Operation(operationId = "getHistoryDetail", summary = "음주 기록 상세 조회")
    BaseResponse<HistoryDetailResponse> getHistoryDetail(
            @Parameter(hidden = true) PrincipalDetails principal,
            @Parameter(description = "음주 기록 ID", example = "31") Long recordId
    );

    @Operation(operationId = "createHistory", summary = "음주 기록 생성")
    BaseResponse<HistoryCreateResponse> createHistory(
            @Parameter(hidden = true) PrincipalDetails principal,
            HistoryCreateRequest request
    );

    @Operation(operationId = "updateHistory", summary = "음주 기록 수정")
    BaseResponse<HistoryUpdateResponse> updateHistory(
            @Parameter(hidden = true) PrincipalDetails principal,
            @Parameter(description = "음주 기록 ID", example = "31") Long recordId,
            HistoryUpdateRequest request
    );

    @Operation(operationId = "deleteHistory", summary = "음주 기록 삭제")
    BaseResponse<Void> deleteHistory(
            @Parameter(hidden = true) PrincipalDetails principal,
            @Parameter(description = "음주 기록 ID", example = "31") Long recordId
    );

    @Operation(
            operationId = "addHistoryPhoto",
            summary = "히스토리 사진 추가",
            description = "선택한 날짜에 사진을 추가합니다. 날짜별로 최대 5장까지 저장할 수 있습니다."
    )
    BaseResponse<HistoryPhotoResponse> addHistoryPhoto(
            @Parameter(hidden = true) PrincipalDetails principal,
            @Parameter(description = "사진 기록 날짜(yyyy-MM-dd)", example = "2026-07-05") String date,
            @Parameter(description = "JPG, PNG 또는 WEBP 이미지") MultipartFile image,
            @Parameter(
                    description = "사진 출처",
                    schema = @Schema(allowableValues = {"CAMERA", "GALLERY"})
            )
            String sourceType
    );

    @Operation(operationId = "deleteHistoryPhoto", summary = "히스토리 사진 삭제")
    BaseResponse<Void> deleteHistoryPhoto(
            @Parameter(hidden = true) PrincipalDetails principal,
            @Parameter(description = "사진 기록 날짜(yyyy-MM-dd)", example = "2026-07-05") String date,
            @Parameter(description = "사진 ID", example = "3") Long photoId
    );
}
