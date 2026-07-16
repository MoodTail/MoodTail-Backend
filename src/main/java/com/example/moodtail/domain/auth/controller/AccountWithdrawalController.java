package com.example.moodtail.domain.auth.controller;

import com.example.moodtail.domain.auth.service.AccountWithdrawalService;
import com.example.moodtail.domain.user.entity.UserRole;
import com.example.moodtail.global.common.base.BaseResponse;
import com.example.moodtail.global.common.exception.RestApiException;
import com.example.moodtail.global.common.exception.code.status.AuthErrorStatus;
import com.example.moodtail.global.config.security.auth.PrincipalDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class AccountWithdrawalController {

    private final AccountWithdrawalService accountWithdrawalService;

    @DeleteMapping
    @Operation(
            operationId = "withdrawUser",
            summary = "회원 탈퇴",
            description = "회원의 개인 데이터와 인증 정보를 삭제합니다. 문의는 작성자와 연락처를 익명화하고, "
                    + "테스트 결과는 사용자 연결 및 공유 토큰을 제거해 통계 집계용으로만 유지합니다."
    )
    @SecurityRequirement(name = "bearerAuth")
    public BaseResponse<Void> withdraw(
            @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principal,
            @Parameter(hidden = true) HttpServletRequest request,
            @Parameter(hidden = true) HttpServletResponse response
    ) {
        preventCaching(response);
        if (principal == null || !UserRole.USER.name().equals(principal.getRole())) {
            throw new RestApiException(AuthErrorStatus.LOGIN_USER_REQUIRED);
        }
        accountWithdrawalService.withdraw(principal.getUserId(), request, response);
        return BaseResponse.onSuccess(null);
    }

    private void preventCaching(HttpServletResponse response) {
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-store");
        response.setHeader(HttpHeaders.PRAGMA, "no-cache");
    }
}
