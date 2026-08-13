package com.example.moodtail.domain.auth.controller.docs;

import com.example.moodtail.domain.auth.dto.response.GuestLoginResponse;
import com.example.moodtail.domain.auth.dto.response.LocalAuthResponse;
import com.example.moodtail.domain.auth.dto.response.LocalEmailAvailabilityResponse;
import com.example.moodtail.domain.auth.dto.response.OAuthStateResponse;
import com.example.moodtail.domain.auth.dto.response.PasswordResetCodeResponse;
import com.example.moodtail.domain.auth.dto.response.PasswordResetVerificationResponse;
import com.example.moodtail.domain.auth.dto.response.SocialLoginResponse;
import com.example.moodtail.domain.auth.dto.response.TokenResponse;
import com.example.moodtail.global.common.base.BaseResponse;
import io.swagger.v3.oas.annotations.media.Schema;

/** Concrete generic wrappers used only to make Springdoc response schemas deterministic. */
public final class AuthApiResponseSchemas {

    private AuthApiResponseSchemas() {
    }

    @Schema(name = "AuthGuestLoginApiResponse")
    public static final class GuestLogin extends BaseResponse<GuestLoginResponse> {
        private GuestLogin() {
            super(null, null, null);
        }
    }

    @Schema(name = "AuthOAuthStateApiResponse")
    public static final class OAuthState extends BaseResponse<OAuthStateResponse> {
        private OAuthState() {
            super(null, null, null);
        }
    }

    @Schema(name = "AuthSocialLoginApiResponse")
    public static final class SocialLogin extends BaseResponse<SocialLoginResponse> {
        private SocialLogin() {
            super(null, null, null);
        }
    }

    @Schema(name = "AuthLocalApiResponse")
    public static final class LocalAuth extends BaseResponse<LocalAuthResponse> {
        private LocalAuth() {
            super(null, null, null);
        }
    }

    @Schema(name = "AuthEmailAvailabilityApiResponse")
    public static final class EmailAvailability extends BaseResponse<LocalEmailAvailabilityResponse> {
        private EmailAvailability() {
            super(null, null, null);
        }
    }

    @Schema(name = "AuthPasswordResetCodeApiResponse")
    public static final class PasswordResetCode extends BaseResponse<PasswordResetCodeResponse> {
        private PasswordResetCode() {
            super(null, null, null);
        }
    }

    @Schema(name = "AuthPasswordResetVerificationApiResponse")
    public static final class PasswordResetVerification
            extends BaseResponse<PasswordResetVerificationResponse> {
        private PasswordResetVerification() {
            super(null, null, null);
        }
    }

    @Schema(name = "AuthTokenApiResponse")
    public static final class Token extends BaseResponse<TokenResponse> {
        private Token() {
            super(null, null, null);
        }
    }

    @Schema(name = "AuthVoidApiResponse")
    public static final class VoidResponse extends BaseResponse<Void> {
        private VoidResponse() {
            super(null, null, null);
        }
    }
}
