package com.example.moodtail.domain.user.controller;

import com.example.moodtail.domain.user.dto.request.LoginRequest;
import com.example.moodtail.domain.user.dto.request.SignupRequest;
import com.example.moodtail.domain.user.dto.response.LoginResponse;
import com.example.moodtail.domain.user.dto.response.SignupResponse;
import com.example.moodtail.domain.user.service.AuthService;
import com.example.moodtail.global.common.base.BaseResponse;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public BaseResponse<SignupResponse> signup(
        @Valid @RequestBody SignupRequest request
    ) {
        return BaseResponse.onSuccess(authService.signup(request));
    }

    @PostMapping("/login")
    public BaseResponse<LoginResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response
    ) {
        return BaseResponse.onSuccess(authService.login(request, response));
    }
}
