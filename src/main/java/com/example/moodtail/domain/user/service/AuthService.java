package com.example.moodtail.domain.user.service;

import com.example.moodtail.domain.user.client.OAuthClient;
import com.example.moodtail.domain.user.client.SocialUserProfile;
import com.example.moodtail.domain.user.config.AuthProperties;
import com.example.moodtail.domain.user.dto.request.GuestLoginRequest;
import com.example.moodtail.domain.user.dto.request.LocalLoginRequest;
import com.example.moodtail.domain.user.dto.request.LocalSignupRequest;
import com.example.moodtail.domain.user.dto.request.PasswordChangeRequest;
import com.example.moodtail.domain.user.dto.request.PasswordResetCodeRequest;
import com.example.moodtail.domain.user.dto.request.PasswordResetCodeVerifyRequest;
import com.example.moodtail.domain.user.dto.request.SocialLoginRequest;
import com.example.moodtail.domain.user.dto.request.TermAgreementRequest;
import com.example.moodtail.domain.user.dto.response.GuestLoginResponse;
import com.example.moodtail.domain.user.dto.response.LocalAuthResponse;
import com.example.moodtail.domain.user.dto.response.OAuthStateResponse;
import com.example.moodtail.domain.user.dto.response.PasswordResetCodeResponse;
import com.example.moodtail.domain.user.dto.response.PasswordResetVerificationResponse;
import com.example.moodtail.domain.user.dto.response.SocialLoginResponse;
import com.example.moodtail.domain.user.dto.response.TokenResponse;
import com.example.moodtail.domain.user.entity.User;
import com.example.moodtail.domain.user.enums.SocialProvider;
import com.example.moodtail.domain.user.enums.UserRole;
import com.example.moodtail.domain.user.repository.UserRepository;
import com.example.moodtail.global.common.exception.RestApiException;
import com.example.moodtail.global.common.exception.code.status.AuthErrorStatus;
import com.example.moodtail.global.common.exception.code.status.GlobalErrorStatus;
import com.example.moodtail.global.config.security.jwt.JwtProvider;
import com.example.moodtail.global.config.security.jwt.TokenInfo;
import com.example.moodtail.global.token.repository.redis.RedisRepository;
import io.jsonwebtoken.Claims;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import static com.example.moodtail.global.common.exception.code.status.AuthErrorStatus.AUTH_INFRASTRUCTURE_UNAVAILABLE;
import static com.example.moodtail.global.token.redis.AuthRedisFailurePolicy.required;
import static com.example.moodtail.global.token.redis.AuthRedisFailurePolicy.bestEffort;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private static final int MAX_PROVIDER_USER_ID_LENGTH = 255;
    private static final int MAX_SOCIAL_EMAIL_LENGTH = 320;

    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final RedisRepository redisRepository;
    private final List<OAuthClient> oAuthClients;
    private final SocialAccountRegistrationService socialAccountRegistrationService;
    private final GuestUserRegistrationService guestUserRegistrationService;
    private final OAuthStateService oAuthStateService;
    private final GuestLoginRateLimiter guestLoginRateLimiter;
    private final LocalAccountService localAccountService;
    private final PasswordResetService passwordResetService;
    private final AuthRequestOriginValidator authRequestOriginValidator;
    private final AuthProperties authProperties;

    @Value("${jwt.refreshExpiration}")
    private long jwtRefreshExpirationMillis;

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

    public GuestLoginResponse guestLogin(
            GuestLoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse response
    ) {
        guestLoginRateLimiter.check(request.guestUuid(), httpRequest);
        GuestLoginUser guestLoginUser = guestUserRegistrationService.findOrCreate(request.guestUuid());
        TokenInfo tokenInfo = issueToken(guestLoginUser.userId(), guestLoginUser.role(), response);

        return GuestLoginResponse.of(guestLoginUser, tokenInfo);
    }

    public OAuthStateResponse createOAuthState(String providerName, Long guestUserId) {
        SocialProvider provider = parseSocialProvider(providerName);
        findEnabledOAuthClient(provider);
        return OAuthStateResponse.from(oAuthStateService.issue(guestUserId, provider));
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public SocialLoginResponse socialLogin(
            String providerName,
            SocialLoginRequest request,
            HttpServletResponse response
    ) {
        SocialProvider provider = parseSocialProvider(providerName);
        SocialAuthentication authentication = authenticateSocial(
                provider,
                request.authorizationCode(),
                request.redirectUri(),
                request.state()
        );

        SocialUserProfile requestedProfile = withRequestedNickname(authentication.profile(), request.nickname());
        CompletedSocialLogin completedLogin = socialAccountRegistrationService.authenticateAndComplete(
                requestedProfile,
                authentication.guestUserId(),
                toConsents(request.agreements()),
                socialLoginUser -> {
                    TokenInfo tokenInfo = issueTokenSession(socialLoginUser.userId(), socialLoginUser.role());
                    if (!authentication.guestUserId().equals(socialLoginUser.userId())) {
                        deleteRefreshSessionWithRollback(authentication.guestUserId());
                    }
                    return new CompletedSocialLogin(socialLoginUser, tokenInfo);
                }
        );
        SocialLoginUser socialLoginUser = completedLogin.user();
        TokenInfo tokenInfo = completedLogin.tokenInfo();
        addRefreshTokenCookie(response, tokenInfo.refreshToken());
        return SocialLoginResponse.of(
                socialLoginUser.userId(),
                socialLoginUser.nickname(),
                socialLoginUser.provider(),
                socialLoginUser.socialEmail(),
                tokenInfo,
                socialLoginUser.isNewUser()
        );
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public LocalAuthResponse localSignup(
            LocalSignupRequest request,
            Long guestUserId,
            HttpServletResponse response
    ) {
        CompletedLocalLogin completed = localAccountService.signupAndComplete(
                request.email(),
                request.password(),
                request.passwordConfirm(),
                request.nickname(),
                toConsents(request.agreements()),
                guestUserId,
                user -> new CompletedLocalLogin(
                        user,
                        issueTokenSession(user.userId(), user.role())
                )
        );
        addRefreshTokenCookie(response, completed.tokenInfo().refreshToken());
        return LocalAuthResponse.of(
                completed.user().userId(),
                completed.user().email(),
                completed.user().nickname(),
                true,
                completed.tokenInfo()
        );
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public LocalAuthResponse localLogin(
            LocalLoginRequest request,
            Long guestUserId,
            HttpServletResponse response
    ) {
        CompletedLocalLogin completed = localAccountService.loginAndComplete(
                request.email(),
                request.password(),
                guestUserId,
                user -> {
                    TokenInfo tokenInfo = issueTokenSession(user.userId(), user.role());
                    if (guestUserId != null && !guestUserId.equals(user.userId())) {
                        deleteRefreshSessionWithRollback(guestUserId);
                    }
                    return new CompletedLocalLogin(user, tokenInfo);
                }
        );
        addRefreshTokenCookie(response, completed.tokenInfo().refreshToken());
        return LocalAuthResponse.of(
                completed.user().userId(),
                completed.user().email(),
                completed.user().nickname(),
                false,
                completed.tokenInfo()
        );
    }

    public PasswordResetCodeResponse requestPasswordResetCode(
            PasswordResetCodeRequest request,
            HttpServletRequest httpRequest
    ) {
        return passwordResetService.requestCode(request.email(), httpRequest);
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

    private List<TermAgreementService.Consent> toConsents(List<TermAgreementRequest> agreements) {
        if (agreements == null) {
            return null;
        }
        return agreements.stream()
                .map(agreement -> new TermAgreementService.Consent(
                        agreement.termId(),
                        Boolean.TRUE.equals(agreement.agreed())
                ))
                .toList();
    }

    @Transactional
    public TokenResponse reissue(HttpServletRequest request, HttpServletResponse response) {
        authRequestOriginValidator.validateCookieAuthenticatedRequest(request);
        try {
            String refreshToken = resolveRefreshToken(request)
                    .orElseThrow(() -> new RestApiException(AuthErrorStatus.EMPTY_JWT));

            Claims refreshClaims = jwtProvider.getRefreshTokenClaims(refreshToken);
            if (!jwtProvider.validateRefreshToken(refreshToken)) {
                throw new RestApiException(AuthErrorStatus.INVALID_REFRESH_TOKEN);
            }

            Long userId = parseUserId(refreshClaims, AuthErrorStatus.INVALID_REFRESH_TOKEN);
            String refreshJti = refreshClaims.getId();
            String storedRefreshJti = required(
                    "find refresh session",
                    () -> redisRepository.findRefreshJtiByUserId(userId)
            )
                    .orElseThrow(() -> new RestApiException(AuthErrorStatus.INVALID_REFRESH_TOKEN));

            if (!StringUtils.hasText(refreshJti) || !storedRefreshJti.equals(refreshJti)) {
                throw new RestApiException(AuthErrorStatus.INVALID_REFRESH_TOKEN);
            }

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> {
                        required("delete orphaned refresh session", () -> redisRepository.deleteRefreshJti(userId));
                        return new RestApiException(AuthErrorStatus.USER_NOT_FOUND);
                    });
            if (!user.isActive() || user.isDeleted()) {
                required("delete inactive user refresh session", () -> redisRepository.deleteRefreshJti(userId));
                throw new RestApiException(AuthErrorStatus.INACTIVE_USER);
            }
            user.updateLastAccessedAt(LocalDateTime.now());

            TokenInfo tokenInfo = rotateRefreshToken(user, refreshJti, response);
            return TokenResponse.from(tokenInfo);
        } catch (RestApiException e) {
            if (!isAuthInfrastructureUnavailable(e)) {
                clearRefreshTokenCookie(response);
            }
            throw e;
        }
    }

    public void logout(HttpServletRequest request, HttpServletResponse response) {
        authRequestOriginValidator.validateCookieAuthenticatedRequest(request);
        try {
            Optional<Long> accessTokenUserId = revokeAccessToken(request);
            Optional<Long> refreshTokenUserId = resolveCurrentRefreshSessionUser(request);
            Set<Long> sessionUserIds = new LinkedHashSet<>();
            accessTokenUserId.ifPresent(sessionUserIds::add);
            refreshTokenUserId.ifPresent(sessionUserIds::add);
            for (Long userId : sessionUserIds) {
                required(
                        "delete logout refresh session",
                        () -> redisRepository.deleteRefreshJti(userId)
                );
            }
        } finally {
            clearRefreshTokenCookie(response);
            SecurityContextHolder.clearContext();
        }
    }

    private Optional<Long> revokeAccessToken(HttpServletRequest request) {
        String accessToken = jwtProvider.resolveToken(request);
        if (!StringUtils.hasText(accessToken) || !jwtProvider.validateAccessToken(accessToken)) {
            return Optional.empty();
        }

        Claims accessClaims;
        Long userId;
        try {
            accessClaims = jwtProvider.getAccessTokenClaims(accessToken);
            userId = parseUserId(accessClaims, AuthErrorStatus.INVALID_ACCESS_TOKEN);
        } catch (RestApiException ignored) {
            return Optional.empty();
        }

        required(
                "blacklist logout access token",
                () -> redisRepository.blockAccessToken(accessToken, accessClaims)
        );
        return Optional.of(userId);
    }

    private Optional<Long> resolveCurrentRefreshSessionUser(HttpServletRequest request) {
        Optional<String> refreshToken = resolveRefreshToken(request);
        if (refreshToken.isEmpty()) {
            return Optional.empty();
        }

        Claims refreshClaims;
        Long userId;
        try {
            refreshClaims = jwtProvider.getRefreshTokenClaims(refreshToken.get());
            userId = parseUserId(refreshClaims, AuthErrorStatus.INVALID_REFRESH_TOKEN);
        } catch (RestApiException ignored) {
            // Logout is idempotent: an invalid or expired cookie is cleared without exposing token details.
            return Optional.empty();
        }

        String refreshJti = refreshClaims.getId();
        if (!StringUtils.hasText(refreshJti)) {
            return Optional.empty();
        }

        boolean currentSession = required(
                "find logout refresh session",
                () -> redisRepository.findRefreshJtiByUserId(userId)
        )
                .filter(refreshJti::equals)
                .isPresent();
        return currentSession ? Optional.of(userId) : Optional.empty();
    }

    private TokenInfo issueToken(Long userId, UserRole role, HttpServletResponse response) {
        TokenInfo tokenInfo = issueTokenSession(userId, role);
        addRefreshTokenCookie(response, tokenInfo.refreshToken());
        return tokenInfo;
    }

    private TokenInfo issueTokenSession(Long userId, UserRole role) {
        Optional<String> previousRefreshJti = required(
                "find previous refresh session",
                () -> redisRepository.findRefreshJtiByUserId(userId)
        );
        TokenInfo tokenInfo = jwtProvider.generateToken(userId, role);
        Claims refreshClaims = jwtProvider.getRefreshTokenClaims(tokenInfo.refreshToken());

        required("save refresh session", () -> redisRepository.saveRefreshJti(userId, refreshClaims.getId()));
        registerRollbackAction(() -> restoreRefreshSessionBestEffort(userId, previousRefreshJti));

        return tokenInfo;
    }

    private void deleteRefreshSessionWithRollback(Long userId) {
        Optional<String> previousRefreshJti = required(
                "find refresh session before deletion",
                () -> redisRepository.findRefreshJtiByUserId(userId)
        );
        required("delete merged guest refresh session", () -> redisRepository.deleteRefreshJti(userId));
        registerRollbackAction(() -> restoreRefreshSessionBestEffort(userId, previousRefreshJti));
    }

    private void restoreRefreshSessionBestEffort(Long userId, Optional<String> previousRefreshJti) {
        bestEffort("restore refresh session after database rollback", () -> {
            if (previousRefreshJti.isPresent()) {
                redisRepository.saveRefreshJti(userId, previousRefreshJti.get());
            } else {
                redisRepository.deleteRefreshJti(userId);
            }
        });
    }

    private void registerRollbackAction(Runnable rollbackAction) {
        if (!TransactionSynchronizationManager.isActualTransactionActive()
                || !TransactionSynchronizationManager.isSynchronizationActive()) {
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status != TransactionSynchronization.STATUS_COMMITTED) {
                    rollbackAction.run();
                }
            }
        });
    }

    private void runAfterCommit(Runnable action) {
        if (!TransactionSynchronizationManager.isActualTransactionActive()
                || !TransactionSynchronizationManager.isSynchronizationActive()) {
            action.run();
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                action.run();
            }
        });
    }

    private TokenInfo rotateRefreshToken(User user, String expectedRefreshJti, HttpServletResponse response) {
        TokenInfo tokenInfo = jwtProvider.generateToken(user.getId(), user.getRole());
        Claims newRefreshClaims = jwtProvider.getRefreshTokenClaims(tokenInfo.refreshToken());
        boolean rotated = required(
                "rotate refresh session",
                () -> redisRepository.replaceRefreshJti(
                        user.getId(),
                        expectedRefreshJti,
                        newRefreshClaims.getId()
                )
        );

        if (!rotated) {
            throw new RestApiException(AuthErrorStatus.INVALID_REFRESH_TOKEN);
        }

        registerRollbackAction(() -> bestEffort(
                "restore rotated refresh session after database rollback",
                () -> redisRepository.saveRefreshJti(user.getId(), expectedRefreshJti)
        ));

        runAfterCommit(() -> addRefreshTokenCookie(response, tokenInfo.refreshToken()));
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

    private SocialAuthentication authenticateSocial(
            SocialProvider provider,
            String authorizationCode,
            String redirectUri,
            String state
    ) {
        OAuthClient oAuthClient = findEnabledOAuthClient(provider);
        OAuthStateService.ConsumedOAuthState consumedState = oAuthStateService.consumeForAuthentication(
                state,
                provider
        );
        SocialUserProfile profile = oAuthClient.requestUserProfile(
                authorizationCode,
                redirectUri,
                consumedState.codeVerifier()
        );
        validateSocialUserProfile(provider, profile);
        return new SocialAuthentication(consumedState.guestUserId(), profile);
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

    private Optional<String> resolveRefreshToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return Optional.empty();
        }

        return Arrays.stream(cookies)
                .filter(cookie -> authProperties.refreshCookie().name().equals(cookie.getName()))
                .map(Cookie::getValue)
                .filter(StringUtils::hasText)
                .findFirst();
    }

    private Long parseUserId(Claims claims, AuthErrorStatus errorStatus) {
        try {
            return Long.valueOf(claims.getSubject());
        } catch (NumberFormatException e) {
            throw new RestApiException(errorStatus);
        }
    }

    private boolean isAuthInfrastructureUnavailable(RestApiException exception) {
        return AUTH_INFRASTRUCTURE_UNAVAILABLE.getCode().getCode()
                .equals(exception.getErrorCode().getCode());
    }

    private void addRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        response.addHeader(
                HttpHeaders.SET_COOKIE,
                createRefreshTokenCookie(refreshToken, Duration.ofMillis(jwtRefreshExpirationMillis)).toString()
        );
    }

    private void clearRefreshTokenCookie(HttpServletResponse response) {
        response.addHeader(
                HttpHeaders.SET_COOKIE,
                createRefreshTokenCookie("", Duration.ZERO).toString()
        );
    }

    private ResponseCookie createRefreshTokenCookie(String value, Duration maxAge) {
        AuthProperties.RefreshCookie properties = authProperties.refreshCookie();
        ResponseCookie.ResponseCookieBuilder cookieBuilder = ResponseCookie.from(properties.name(), value)
                .httpOnly(true)
                .secure(properties.secure())
                .sameSite(properties.sameSite())
                .path(properties.path())
                .maxAge(maxAge);
        if (StringUtils.hasText(properties.domain())) {
            cookieBuilder.domain(properties.domain());
        }
        return cookieBuilder.build();
    }

    private record SocialAuthentication(Long guestUserId, SocialUserProfile profile) {
    }

    private record CompletedSocialLogin(SocialLoginUser user, TokenInfo tokenInfo) {
    }

    private record CompletedLocalLogin(LocalAccountService.LocalAuthUser user, TokenInfo tokenInfo) {
    }
}
