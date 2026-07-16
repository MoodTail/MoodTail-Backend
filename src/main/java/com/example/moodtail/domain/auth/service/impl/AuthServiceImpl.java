package com.example.moodtail.domain.auth.service.impl;

import com.example.moodtail.domain.auth.dto.request.GuestLoginRequest;
import com.example.moodtail.domain.auth.dto.request.LocalLoginRequest;
import com.example.moodtail.domain.auth.dto.request.LocalSignupRequest;
import com.example.moodtail.domain.auth.dto.request.PasswordChangeRequest;
import com.example.moodtail.domain.auth.dto.request.PasswordResetCodeRequest;
import com.example.moodtail.domain.auth.dto.request.PasswordResetCodeVerifyRequest;
import com.example.moodtail.domain.auth.dto.request.SocialLoginRequest;
import com.example.moodtail.domain.auth.dto.request.TermAgreementRequest;
import com.example.moodtail.domain.auth.dto.response.EmailAvailabilityResponse;
import com.example.moodtail.domain.auth.dto.response.GuestLoginResponse;
import com.example.moodtail.domain.auth.dto.response.LocalAuthResponse;
import com.example.moodtail.domain.auth.dto.response.OAuthStateResponse;
import com.example.moodtail.domain.auth.dto.response.PasswordResetCodeResponse;
import com.example.moodtail.domain.auth.dto.response.PasswordResetVerificationResponse;
import com.example.moodtail.domain.auth.dto.response.SocialLoginResponse;
import com.example.moodtail.domain.auth.dto.response.TokenResponse;
import com.example.moodtail.domain.auth.service.AuthService;
import com.example.moodtail.domain.auth.service.GuestLoginRateLimitService;
import com.example.moodtail.domain.auth.service.GuestUserRegistrationService;
import com.example.moodtail.domain.auth.service.GuestUserRegistrationService.GuestLoginUser;
import com.example.moodtail.domain.auth.service.LocalAccountService;
import com.example.moodtail.domain.auth.service.LocalAccountService.LocalAuthUser;
import com.example.moodtail.domain.auth.service.OAuthStateService;
import com.example.moodtail.domain.auth.service.OAuthStateService.ConsumedOAuthState;
import com.example.moodtail.domain.auth.service.OAuthStateService.OAuthState;
import com.example.moodtail.domain.auth.service.PasswordResetService;
import com.example.moodtail.domain.auth.service.SocialAccountService;
import com.example.moodtail.domain.auth.service.SocialAccountService.SocialLoginUser;
import com.example.moodtail.domain.auth.service.TermAgreementService.Consent;
import com.example.moodtail.domain.auth.service.TokenSessionService;
import com.example.moodtail.domain.user.entity.UserRole;
import com.example.moodtail.global.auth.client.OAuthClient;
import com.example.moodtail.global.auth.model.SocialProvider;
import com.example.moodtail.global.auth.model.SocialUserProfile;
import com.example.moodtail.global.common.exception.RestApiException;
import com.example.moodtail.global.common.exception.code.status.AuthErrorStatus;
import com.example.moodtail.global.common.exception.code.status.GlobalErrorStatus;
import com.example.moodtail.global.config.security.jwt.TokenInfo;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.EnumSet;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final int MAX_PROVIDER_USER_ID_LENGTH = 255;
    private static final int MAX_SOCIAL_EMAIL_LENGTH = 320;

    private final List<OAuthClient> oAuthClients;
    private final SocialAccountService socialAccountService;
    private final GuestUserRegistrationService guestUserRegistrationService;
    private final OAuthStateService oAuthStateService;
    private final GuestLoginRateLimitService guestLoginRateLimitService;
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

    @Override
    public GuestLoginResponse guestLogin(
            GuestLoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse response
    ) {
        guestLoginRateLimitService.check(request.guestUuid(), httpRequest);
        GuestLoginUser guestLoginUser = guestUserRegistrationService.findOrCreate(request.guestUuid());
        TokenInfo tokenInfo = tokenSessionService.issueAndSetCookie(
                guestLoginUser.userId(),
                guestLoginUser.role(),
                response
        );

        return GuestLoginResponse.of(
                guestLoginUser.userId(),
                guestLoginUser.guestUuid(),
                guestLoginUser.isNewUser(),
                tokenInfo
        );
    }

    @Override
    public OAuthStateResponse createOAuthState(String providerName, Long guestUserId) {
        SocialProvider provider = parseSocialProvider(providerName);
        findEnabledOAuthClient(provider);
        OAuthState state = oAuthStateService.issue(guestUserId, provider);
        return new OAuthStateResponse(
                state.value(),
                state.codeChallenge(),
                state.codeChallengeMethod(),
                state.expiresInSeconds()
        );
    }

    @Override
    public SocialLoginResponse socialLogin(
            String providerName,
            SocialLoginRequest request,
            HttpServletResponse response
    ) {
        SocialProvider provider = parseSocialProvider(providerName);
        SocialProfileAuthentication authentication = authenticateSocial(
                provider,
                request.authorizationCode(),
                request.redirectUri(),
                request.state()
        );

        SocialUserProfile requestedProfile = withRequestedNickname(authentication.profile(), request.nickname());
        SocialLoginUser socialLoginUser = socialAccountService.authenticate(
                requestedProfile,
                authentication.guestUserId(),
                toConsents(request.agreements())
        );
        TokenInfo tokenInfo = issueSessionReplacingGuest(
                socialLoginUser.userId(),
                socialLoginUser.role(),
                authentication.guestUserId(),
                response
        );
        return SocialLoginResponse.of(
                socialLoginUser.userId(),
                socialLoginUser.nickname(),
                socialLoginUser.provider(),
                socialLoginUser.socialEmail(),
                tokenInfo,
                socialLoginUser.isNewUser()
        );
    }

    @Override
    public LocalAuthResponse localSignup(
            LocalSignupRequest request,
            Long guestUserId,
            HttpServletResponse response
    ) {
        LocalAuthUser user = localAccountService.signup(
                request.email(),
                request.password(),
                request.passwordConfirm(),
                request.nickname(),
                toConsents(request.agreements()),
                guestUserId
        );
        TokenInfo tokenInfo = issueSessionReplacingGuest(
                user.userId(),
                user.role(),
                guestUserId,
                response
        );
        return LocalAuthResponse.of(
                user.userId(),
                user.email(),
                user.nickname(),
                true,
                tokenInfo
        );
    }

    @Override
    public EmailAvailabilityResponse checkLocalEmailAvailability(String email) {
        String normalizedEmail = localAccountService.normalizeEmail(email);
        return new EmailAvailabilityResponse(
                normalizedEmail,
                localAccountService.isEmailAvailable(normalizedEmail)
        );
    }

    @Override
    public LocalAuthResponse localLogin(
            LocalLoginRequest request,
            Long guestUserId,
            HttpServletResponse response
    ) {
        LocalAuthUser user = localAccountService.login(
                request.email(),
                request.password(),
                guestUserId
        );
        TokenInfo tokenInfo = issueSessionReplacingGuest(
                user.userId(),
                user.role(),
                guestUserId,
                response
        );
        return LocalAuthResponse.of(
                user.userId(),
                user.email(),
                user.nickname(),
                false,
                tokenInfo
        );
    }

    @Override
    public PasswordResetCodeResponse requestPasswordResetCode(
            PasswordResetCodeRequest request,
            HttpServletRequest httpRequest
    ) {
        return passwordResetService.requestCode(request.email(), httpRequest);
    }

    @Override
    public PasswordResetVerificationResponse verifyPasswordResetCode(PasswordResetCodeVerifyRequest request) {
        return passwordResetService.verifyCode(request.email(), request.code());
    }

    @Override
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

    @Override
    public TokenResponse reissue(HttpServletRequest request, HttpServletResponse response) {
        return tokenSessionService.reissue(request, response);
    }

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        tokenSessionService.logout(request, response);
    }

    private TokenInfo issueSessionReplacingGuest(
            Long userId,
            UserRole role,
            Long guestUserId,
            HttpServletResponse response
    ) {
        TokenInfo tokenInfo = tokenSessionService.issueSessionReplacingGuest(userId, role, guestUserId);
        tokenSessionService.setRefreshTokenCookie(response, tokenInfo.refreshToken());
        return tokenInfo;
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
                ? requestedNickname.trim()
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
                || (profile.email() != null && profile.email().length() > MAX_SOCIAL_EMAIL_LENGTH)) {
            throw new RestApiException(AuthErrorStatus.INVALID_SOCIAL_LOGIN);
        }
    }

    private record SocialProfileAuthentication(Long guestUserId, SocialUserProfile profile) {
    }
}
