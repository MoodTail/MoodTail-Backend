package com.example.moodtail.domain.user.service;

import com.example.moodtail.domain.user.client.OAuthClient;
import com.example.moodtail.domain.user.client.SocialUserProfile;
import com.example.moodtail.domain.user.config.AuthProperties;
import com.example.moodtail.domain.user.dto.request.GuestLoginRequest;
import com.example.moodtail.domain.user.dto.request.SocialLoginRequest;
import com.example.moodtail.domain.user.dto.request.SocialSignupRequest;
import com.example.moodtail.domain.user.dto.response.GuestLoginResponse;
import com.example.moodtail.domain.user.dto.response.OAuthStateResponse;
import com.example.moodtail.domain.user.dto.response.SocialLoginResponse;
import com.example.moodtail.domain.user.dto.response.SocialSignupResponse;
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
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

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
    private final AuthProperties authProperties;

    @Value("${jwt.refreshExpiration}")
    private long jwtRefreshExpirationMillis;

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

        SocialLoginUser socialLoginUser = socialAccountRegistrationService.login(authentication.profile());
        if (!authentication.guestUserId().equals(socialLoginUser.userId())) {
            redisRepository.deleteRefreshJti(authentication.guestUserId());
        }
        TokenInfo tokenInfo = issueToken(socialLoginUser.userId(), socialLoginUser.role(), response);
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
    public SocialSignupResponse socialSignup(
            SocialSignupRequest request,
            HttpServletResponse response
    ) {
        SocialProvider provider = parseSocialProvider(request.provider());
        SocialAuthentication authentication = authenticateSocial(
                provider,
                request.authorizationCode(),
                request.redirectUri(),
                request.state()
        );
        SocialUserProfile signupProfile = withRequestedNickname(authentication.profile(), request.nickname());
        List<SocialAccountRegistrationService.TermAgreementConsent> consents = request.agreements().stream()
                .map(agreement -> new SocialAccountRegistrationService.TermAgreementConsent(
                        agreement.termId(),
                        Boolean.TRUE.equals(agreement.agreed())
                ))
                .toList();

        SocialLoginUser signupUser = socialAccountRegistrationService.register(
                signupProfile,
                authentication.guestUserId(),
                consents
        );
        TokenInfo tokenInfo = issueToken(signupUser.userId(), signupUser.role(), response);
        return SocialSignupResponse.of(signupUser, tokenInfo);
    }

    @Transactional
    public TokenResponse reissue(HttpServletRequest request, HttpServletResponse response) {
        try {
            String refreshToken = resolveRefreshToken(request)
                    .orElseThrow(() -> new RestApiException(AuthErrorStatus.EMPTY_JWT));

            Claims refreshClaims = jwtProvider.getRefreshTokenClaims(refreshToken);
            if (!jwtProvider.validateRefreshToken(refreshToken)) {
                throw new RestApiException(AuthErrorStatus.INVALID_REFRESH_TOKEN);
            }

            Long userId = parseUserId(refreshClaims, AuthErrorStatus.INVALID_REFRESH_TOKEN);
            String refreshJti = refreshClaims.getId();
            String storedRefreshJti = redisRepository.findRefreshJtiByUserId(userId)
                    .orElseThrow(() -> new RestApiException(AuthErrorStatus.INVALID_REFRESH_TOKEN));

            if (!StringUtils.hasText(refreshJti) || !storedRefreshJti.equals(refreshJti)) {
                throw new RestApiException(AuthErrorStatus.INVALID_REFRESH_TOKEN);
            }

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> {
                        redisRepository.deleteRefreshJti(userId);
                        return new RestApiException(AuthErrorStatus.USER_NOT_FOUND);
                    });
            if (!user.isActive()) {
                redisRepository.deleteRefreshJti(userId);
                throw new RestApiException(AuthErrorStatus.INACTIVE_USER);
            }
            user.updateLastAccessedAt(LocalDateTime.now());

            TokenInfo tokenInfo = rotateRefreshToken(user, refreshJti, response);
            return TokenResponse.from(tokenInfo);
        } catch (RestApiException e) {
            clearRefreshTokenCookie(response);
            throw e;
        }
    }

    public void logout(HttpServletRequest request, HttpServletResponse response) {
        String accessToken = jwtProvider.resolveToken(request);
        if (!StringUtils.hasText(accessToken)) {
            throw new RestApiException(AuthErrorStatus.EMPTY_JWT);
        }
        if (!jwtProvider.validateAccessToken(accessToken)) {
            throw new RestApiException(AuthErrorStatus.INVALID_ACCESS_TOKEN);
        }

        Claims accessClaims = jwtProvider.getAccessTokenClaims(accessToken);
        Long userId = parseUserId(accessClaims, AuthErrorStatus.INVALID_ACCESS_TOKEN);

        redisRepository.blockAccessToken(accessToken, accessClaims);
        redisRepository.deleteRefreshJti(userId);
        clearRefreshTokenCookie(response);
        SecurityContextHolder.clearContext();
    }

    private TokenInfo issueToken(Long userId, UserRole role, HttpServletResponse response) {
        TokenInfo tokenInfo = jwtProvider.generateToken(userId, role);
        Claims refreshClaims = jwtProvider.getRefreshTokenClaims(tokenInfo.refreshToken());

        redisRepository.saveRefreshJti(userId, refreshClaims.getId());
        addRefreshTokenCookie(response, tokenInfo.refreshToken());

        return tokenInfo;
    }

    private TokenInfo rotateRefreshToken(User user, String expectedRefreshJti, HttpServletResponse response) {
        TokenInfo tokenInfo = jwtProvider.generateToken(user.getId(), user.getRole());
        Claims newRefreshClaims = jwtProvider.getRefreshTokenClaims(tokenInfo.refreshToken());
        boolean rotated = redisRepository.replaceRefreshJti(
                user.getId(),
                expectedRefreshJti,
                newRefreshClaims.getId()
        );

        if (!rotated) {
            throw new RestApiException(AuthErrorStatus.INVALID_REFRESH_TOKEN);
        }

        addRefreshTokenCookie(response, tokenInfo.refreshToken());
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

    private OAuthClient findOAuthClient(SocialProvider provider) {
        return oAuthClients.stream()
                .filter(oAuthClient -> oAuthClient.provider() == provider)
                .findFirst()
                .orElseThrow(() -> new RestApiException(AuthErrorStatus.SOCIAL_LOGIN_CONFIGURATION_ERROR));
    }

    private SocialAuthentication authenticateSocial(
            SocialProvider provider,
            String authorizationCode,
            String redirectUri,
            String state
    ) {
        Long guestUserId = oAuthStateService.consume(state, provider);
        OAuthClient oAuthClient = findOAuthClient(provider);
        SocialUserProfile profile = oAuthClient.requestUserProfile(authorizationCode, redirectUri);
        validateSocialUserProfile(provider, profile);
        return new SocialAuthentication(guestUserId, profile);
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
}
