package com.example.moodtail.domain.report.controller.docs;

import com.example.moodtail.domain.report.dto.response.MonthlyReportResponse;
import com.example.moodtail.domain.report.dto.response.MonthlyReportShareImageResponse;
import com.example.moodtail.global.common.base.BaseResponse;
import com.example.moodtail.global.config.security.auth.PrincipalDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Reports", description = "월간 리포트 조회 및 공유 API")
@SecurityRequirement(name = "bearerAuth")
public interface MonthlyReportControllerDocs {

    @Operation(
            operationId = "getMonthlyReport",
            summary = "월간 리포트 조회",
            description = "월별 테스트 결과와 음주 기록을 집계합니다. 테스트 결과가 5건 미만이면 조회할 수 없습니다."
    )
    BaseResponse<MonthlyReportResponse> getMonthlyReport(
            @Parameter(hidden = true) PrincipalDetails principal,
            @Parameter(description = "조회 연도", example = "2026") int year,
            @Parameter(description = "조회 월(1~12)", example = "7") int month
    );

    @Operation(
            operationId = "createMonthlyReportShareImage",
            summary = "월간 리포트 공유 이미지 생성",
            description = "월간 리포트를 PNG 공유 카드로 생성하고 이미지 URL을 반환합니다."
    )
    BaseResponse<MonthlyReportShareImageResponse> createMonthlyReportShareImage(
            @Parameter(hidden = true) PrincipalDetails principal,
            @Parameter(description = "조회 연도", example = "2026") int year,
            @Parameter(description = "조회 월(1~12)", example = "7") int month
    );
}
