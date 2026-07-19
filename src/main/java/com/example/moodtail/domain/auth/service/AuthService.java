package com.example.moodtail.domain.auth.service;

import com.example.moodtail.domain.auth.dto.request.GuestLoginRequest;
import com.example.moodtail.domain.auth.dto.request.LocalLoginRequest;
import com.example.moodtail.domain.auth.dto.request.LocalSignupRequest;
import com.example.moodtail.domain.auth.dto.request.PasswordChangeRequest;
import com.example.moodtail.domain.auth.dto.request.PasswordResetCodeRequest;
import com.example.moodtail.domain.auth.dto.request.PasswordResetCodeVerifyRequest;
import com.example.moodtail.domain.auth.dto.request.SocialLoginRequest;
import com.example.moodtail.domain.auth.dto.request.TermAgreementRequest;
import com.example.moodtail.domain.auth.dto.response.GuestLoginResponse;
import com.example.moodtail.domain.auth.dto.response.LocalAuthResponse;
import com.example.moodtail.domain.auth.dto.response.LocalEmailAvailabilityResponse;
import com.example.moodtail.domain.auth.dto.response.OAuthStateResponse;
import com.example.moodtail.domain.auth.dto.response.PasswordResetCodeResponse;
import com.example.moodtail.domain.auth.dto.response.PasswordResetVerificationResponse;
import com.example.moodtail.domain.auth.dto.response.SocialLoginResponse;
import com.example.moodtail.domain.auth.dto.response.TokenResponse;
import com.example.moodtail.domain.auth.model.Consent;
import com.example.moodtail.domain.auth.model.ConsumedOAuthState;
import com.example.moodtail.domain.auth.model.GuestLoginUser;
import com.example.moodtail.domain.auth.model.AuthResult;
import com.example.moodtail.domain.auth.model.LocalAuthenticationResult;
import com.example.moodtail.domain.auth.model.SocialAuthenticationResult;
import com.example.moodtail.domain.auth.model.SocialLoginUser;
import com.example.moodtail.domain.auth.validator.GuestLoginRateLimiter;
import com.example.moodtail.domain.auth.validator.LocalAuthRateLimiter;
import com.example.moodtail.global.auth.client.OAuthClient;
import com.example.moodtail.global.auth.model.SocialProvider;
import com.example.moodtail.global.auth.model.SocialUserProfile;
import com.example.moodtail.global.common.exception.RestApiException;
import com.example.moodtail.global.common.exception.code.status.AuthErrorStatus;
import com.example.moodtail.global.common.exception.code.status.GlobalErrorStatus;
import com.example.moodtail.global.config.security.jwt.TokenInfo;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.EnumSet;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final int MAX_PROVIDER_USER_ID_LENGTH = 255;
    private static final int MAX_SOCIAL_EMAIL_LENGTH = 320;

    private final List<OAuthClient> oAuthClients;
    private final SocialAccountService socialAccountService;
    private final GuestUserService guestUserService;
    private final OAuthStateService oAuthStateService;
    private final GuestLoginRateLimiter guestLoginRateLimiter;
    private final LocalAuthRateLimiter localAuthRateLimiter;
    private final LocalAccountService localAccountService;
    private final PasswordResetService passwordResetService;
    private final TokenSessionService tokenSessionService;

    @PostConstruct
    void validateOAuthClients() {
        EnumSet<SocialProvider> registeredProviders = EnumSet.noneOf(SocialProvider.class);
        for (OAuthClient client : oAuthClients) {
            if (client == null || client.provider() == null || !registeredProviders.add(client.provider())) {
                throw new IllegalStateException("Each social provider must have exactly one OAuth client");
            }
        }
        if (!registeredProviders.equals(EnumSet.allOf(SocialProvider.class))) {
            throw new IllegalStateException("Every social provider must have an OAuth client");
        }
    }

    public AuthResult<GuestLoginResponse> guestLogin(
            GuestLoginRequest request,
            String clientAddress
    ) {
        guestLoginRateLimiter.check(request.guestUuid(), clientAddress);
        GuestLoginUser guestLoginUser = guestUserService.findOrCreate(request.guestUuid());
        TokenInfo tokenInfo = tokenSessionService.issueSession(
                guestLoginUser.userId(),
                guestLoginUser.role()
        );

        return new AuthResult<>(
                GuestLoginResponse.of(guestLoginUser, tokenInfo),
                tokenInfo.refreshToken()
        );
    }

    public OAuthStateResponse createOAuthState(String providerName, Long guestUserId) {
        SocialProvider provider = parseSocialProvider(providerName);
        findEnabledOAuthClient(provider);
        return OAuthStateResponse.from(oAuthStateService.issue(guestUserId, provider));
    }

    public AuthResult<SocialLoginResponse> socialLogin(
            String providerName,
            SocialLoginRequest request
    ) {
        SocialProvider provider = parseSocialProvider(providerName);
        SocialProfileAuthentication authentication = authenticateSocial(
                provider,
                request.authorizationCode(),
                request.redirectUri(),
                request.state()
        );

        SocialUserProfile requestedProfile = withRequestedNickname(authentication.profile(), request.nickname());
        SocialAuthenticationResult completedLogin = socialAccountService.authenticate(
                requestedProfile,
                authentication.guestUserId(),
                toConsents(request.agreements())
        );
        SocialLoginUser socialLoginUser = completedLogin.user();
        TokenInfo tokenInfo = completedLogin.tokenInfo();
        return new AuthResult<>(
                SocialLoginResponse.of(
                        socialLoginUser.userId(),
                        socialLoginUser.nickname(),
                        socialLoginUser.provider(),
                        socialLoginUser.socialEmail(),
                        tokenInfo,
                        socialLoginUser.isNewUser()
                ),
                tokenInfo.refreshToken()
        );
    }

    public AuthResult<LocalAuthResponse> localSignup(
            LocalSignupRequest request,
            Long guestUserId,
            String clientAddress
    ) {
        localAuthRateLimiter.checkSignup(clientAddress);
        LocalAuthenticationResult completed = localAccountService.signup(
                request.email(),
                request.password(),
                request.passwordConfirm(),
                request.nickname(),
                toConsents(request.agreements()),
                guestUserId
        );
        return new AuthResult<>(
                LocalAuthResponse.of(
                        completed.user().userId(),
                        completed.user().email(),
                        completed.user().nickname(),
                        true,
                        completed.tokenInfo()
                ),
                completed.tokenInfo().refreshToken()
        );
    }

    public AuthResult<LocalAuthResponse> localLogin(
            LocalLoginRequest request,
            Long guestUserId,
            String clientAddress
    ) {
        localAuthRateLimiter.checkLogin(clientAddress);
        LocalAuthenticationResult completed = localAccountService.login(
                request.email(),
                request.password(),
                guestUserId
        );
        return new AuthResult<>(
                LocalAuthResponse.of(
                        completed.user().userId(),
                        completed.user().email(),
                        completed.user().nickname(),
                        false,
                        completed.tokenInfo()
                ),
                completed.tokenInfo().refreshToken()
        );
    }

    public LocalEmailAvailabilityResponse checkLocalEmailAvailability(
            String email,
            String clientAddress
    ) {
        localAuthRateLimiter.checkEmailAvailability(clientAddress);
        String normalizedEmail = localAccountService.normalizeEmail(email);
        return new LocalEmailAvailabilityResponse(
                normalizedEmail,
                localAccountService.isNormalizedEmailAvailable(normalizedEmail)
        );
    }

    public PasswordResetCodeResponse requestPasswordResetCode(
            PasswordResetCodeRequest request,
            String clientAddress
    ) {
        return passwordResetService.requestCode(request.email(), clientAddress);
    }

    public PasswordResetVerificationResponse verifyPasswordResetCode(PasswordResetCodeVerifyRequest request) {
        return passwordResetService.verifyCode(request.email(), request.code());
    }

    public void changePassword(PasswordChangeRequest request) {
        passwordResetService.changePassword(
                request.resetToken(),
                request.newPassword(),
                request.newPasswordConfirm()
        );
    }

    private List<Consent> toConsents(List<TermAgreementRequest> agreements) {
        if (agreements == null) {
            return null;
        }
        return agreements.stream()
                .map(agreement -> new Consent(
                        agreement.termId(),
                        Boolean.TRUE.equals(agreement.agreed())
                ))
                .toList();
    }

    public AuthResult<TokenResponse> reissue(String refreshToken) {
        TokenInfo tokenInfo = tokenSessionService.reissue(refreshToken);
        return new AuthResult<>(TokenResponse.from(tokenInfo), tokenInfo.refreshToken());
    }

    public void logout(String accessToken, String refreshToken) {
        tokenSessionService.logout(accessToken, refreshToken);
    }

    private SocialProvider parseSocialProvider(String providerName) {
        if (!StringUtils.hasText(providerName)) {
            throw new RestApiException(GlobalErrorStatus._BAD_REQUEST);
        }

        try {
            return SocialProvider.valueOf(providerName.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new RestApiException(GlobalErrorStatus._BAD_REQUEST);
        }
    }

    private OAuthClient findEnabledOAuthClient(SocialProvider provider) {
        OAuthClient oAuthClient = oAuthClients.stream()
                .filter(client -> client.provider() == provider)
                .findFirst()
                .orElseThrow(() -> new RestApiException(AuthErrorStatus.SOCIAL_LOGIN_CONFIGURATION_ERROR));
        if (!oAuthClient.isEnabled()) {
            throw new RestApiException(AuthErrorStatus.SOCIAL_LOGIN_CONFIGURATION_ERROR);
        }
        return oAuthClient;
    }

    private SocialProfileAuthentication authenticateSocial(
            SocialProvider provider,
            String authorizationCode,
            String redirectUri,
            String state
    ) {
        OAuthClient oAuthClient = findEnabledOAuthClient(provider);
        oAuthClient.validateAuthorizationRequest(authorizationCode, redirectUri);
        ConsumedOAuthState consumedState = oAuthStateService.consumeForAuthentication(
                state,
                provider
        );
        SocialUserProfile profile = oAuthClient.requestUserProfile(
                authorizationCode,
                redirectUri,
                consumedState.codeVerifier()
        );
        validateSocialUserProfile(provider, profile);
        return new SocialProfileAuthentication(consumedState.guestUserId(), profile);
    }

    private SocialUserProfile withRequestedNickname(SocialUserProfile profile, String requestedNickname) {
        String nickname = StringUtils.hasText(requestedNickname)
                ? requestedNickname
                : profile.nickname();
        return new SocialUserProfile(
                profile.provider(),
                profile.providerUserId(),
                profile.email(),
                nickname
        );
    }

    private void validateSocialUserProfile(SocialProvider provider, SocialUserProfile profile) {
        if (profile == null
                || profile.provider() != provider
                || !StringUtils.hasText(profile.providerUserId())
                || profile.providerUserId().length() > MAX_PROVIDER_USER_ID_LENGTH
                || !StringUtils.hasText(profile.email())
                || profile.email().length() > MAX_SOCIAL_EMAIL_LENGTH) {
            throw new RestApiException(AuthErrorStatus.INVALID_SOCIAL_LOGIN);
        }
    }

    private record SocialProfileAuthentication(Long guestUserId, SocialUserProfile profile) {
    }
}
