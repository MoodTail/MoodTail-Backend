package com.example.moodtail.domain.auth.service;

import com.example.moodtail.domain.auth.dto.response.TokenResponse;
import com.example.moodtail.domain.auth.validator.AuthRequestOriginValidator;
import com.example.moodtail.domain.user.entity.User;
import com.example.moodtail.domain.user.entity.UserRole;
import com.example.moodtail.domain.user.repository.UserRepository;
import com.example.moodtail.global.auth.config.AuthProperties;
import com.example.moodtail.global.common.exception.RestApiException;
import com.example.moodtail.global.common.exception.code.status.AuthErrorStatus;
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
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import static com.example.moodtail.global.common.exception.code.status.AuthErrorStatus.AUTH_INFRASTRUCTURE_UNAVAILABLE;
import static com.example.moodtail.global.token.redis.AuthRedisFailurePolicy.bestEffort;
import static com.example.moodtail.global.token.redis.AuthRedisFailurePolicy.required;

@Service
@RequiredArgsConstructor
public class TokenSessionService {

    private final UserRepository userRepository;
    private final PlatformTransactionManager transactionManager;
    private final JwtProvider jwtProvider;
    private final RedisRepository redisRepository;
    private final AuthRequestOriginValidator authRequestOriginValidator;
    private final AuthProperties authProperties;

    @Value("${jwt.refreshExpiration}")
    private long jwtRefreshExpirationMillis;

    public TokenInfo issueAndSetCookie(Long userId, UserRole role, HttpServletResponse response) {
        TokenInfo tokenInfo = issueSession(userId, role);
        setRefreshTokenCookie(response, tokenInfo.refreshToken());
        return tokenInfo;
    }

    public void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        response.addHeader(
                HttpHeaders.SET_COOKIE,
                createRefreshTokenCookie(refreshToken, Duration.ofMillis(jwtRefreshExpirationMillis)).toString()
        );
    }

    public TokenInfo issueSession(Long userId, UserRole role) {
        requireNoActiveDatabaseTransaction("issue authentication session");
        return createAndStoreSession(userId, role);
    }

    public TokenInfo issueSessionReplacingGuest(Long userId, UserRole role, Long guestUserId) {
        requireNoActiveDatabaseTransaction("issue authentication session and revoke guest session");
        if (guestUserId == null || guestUserId.equals(userId)) {
            return createAndStoreSession(userId, role);
        }

        TokenInfo tokenInfo = createAndStoreSession(userId, role);
        bestEffort(
                "delete merged guest refresh session",
                () -> redisRepository.deleteRefreshJti(guestUserId)
        );
        return tokenInfo;
    }

    private TokenInfo createAndStoreSession(Long userId, UserRole role) {
        TokenInfo tokenInfo = jwtProvider.generateToken(userId, role);
        Claims refreshClaims = jwtProvider.getRefreshTokenClaims(tokenInfo.refreshToken());
        String refreshJti = requireRefreshJti(refreshClaims);

        required("save refresh session", () -> redisRepository.saveRefreshJti(userId, refreshJti));
        return tokenInfo;
    }

    public void revokeSession(Long userId) {
        requireNoActiveDatabaseTransaction("revoke authentication session");
        required("delete refresh session", () -> redisRepository.deleteRefreshJti(userId));
    }

    public TokenResponse reissue(HttpServletRequest request, HttpServletResponse response) {
        requireNoActiveDatabaseTransaction("reissue authentication session");
        authRequestOriginValidator.validateCookieAuthenticatedRequest(request);
        try {
            String refreshToken = resolveRefreshToken(request)
                    .orElseThrow(() -> new RestApiException(AuthErrorStatus.EMPTY_JWT));

            Claims refreshClaims = jwtProvider.getRefreshTokenClaims(refreshToken);
            Long userId = parseUserId(refreshClaims, AuthErrorStatus.INVALID_REFRESH_TOKEN);
            String refreshJti = refreshClaims.getId();
            String storedRefreshJti = required(
                    "find refresh session",
                    () -> redisRepository.findRefreshJtiByUserId(userId)
            ).orElseThrow(() -> new RestApiException(AuthErrorStatus.INVALID_REFRESH_TOKEN));

            if (!StringUtils.hasText(refreshJti) || !storedRefreshJti.equals(refreshJti)) {
                throw new RestApiException(AuthErrorStatus.INVALID_REFRESH_TOKEN);
            }

            RefreshUser refreshUser = loadRefreshUser(userId);
            TokenInfo tokenInfo = rotateRefreshToken(
                    refreshUser.userId(),
                    refreshUser.role(),
                    refreshJti,
                    response
            );
            return TokenResponse.from(tokenInfo);
        } catch (RestApiException exception) {
            if (!isAuthInfrastructureUnavailable(exception)) {
                clearRefreshTokenCookie(response);
            }
            throw exception;
        }
    }

    public void logout(HttpServletRequest request, HttpServletResponse response) {
        authRequestOriginValidator.validateCookieAuthenticatedRequest(request);
        boolean revocationCompleted = false;
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
            revocationCompleted = true;
        } finally {
            if (revocationCompleted) {
                clearRefreshTokenCookie(response);
            }
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
                () -> redisRepository.blockAccessToken(accessClaims)
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
            return Optional.empty();
        }

        String refreshJti = refreshClaims.getId();
        if (!StringUtils.hasText(refreshJti)) {
            return Optional.empty();
        }

        boolean currentSession = required(
                "find logout refresh session",
                () -> redisRepository.findRefreshJtiByUserId(userId)
        ).filter(refreshJti::equals).isPresent();
        return currentSession ? Optional.of(userId) : Optional.empty();
    }

    private RefreshUser loadRefreshUser(Long userId) {
        RefreshUser refreshUser = requiresNewTransaction().execute(status -> {
            Optional<User> userOptional = userRepository.findById(userId);
            if (userOptional.isEmpty()) {
                return RefreshUser.failure(AuthErrorStatus.USER_NOT_FOUND);
            }
            User user = userOptional.get();
            if (!user.isActive() || user.isDeleted()) {
                return RefreshUser.failure(AuthErrorStatus.INACTIVE_USER);
            }
            user.updateLastAccessedAt(LocalDateTime.now());
            return RefreshUser.success(user.getId(), user.getRole());
        });
        if (refreshUser == null) {
            throw new RestApiException(AuthErrorStatus.AUTH_INFRASTRUCTURE_UNAVAILABLE);
        }
        if (refreshUser.errorStatus() != null) {
            required("delete unusable refresh session", () -> redisRepository.deleteRefreshJti(userId));
            throw new RestApiException(refreshUser.errorStatus());
        }
        return refreshUser;
    }

    private TokenInfo rotateRefreshToken(
            Long userId,
            UserRole role,
            String expectedRefreshJti,
            HttpServletResponse response
    ) {
        TokenInfo tokenInfo = jwtProvider.generateToken(userId, role);
        Claims newRefreshClaims = jwtProvider.getRefreshTokenClaims(tokenInfo.refreshToken());
        String newRefreshJti = requireRefreshJti(newRefreshClaims);
        boolean rotated = required(
                "rotate refresh session",
                () -> redisRepository.replaceRefreshJti(
                        userId,
                        expectedRefreshJti,
                        newRefreshJti
                )
        );

        if (!rotated) {
            throw new RestApiException(AuthErrorStatus.INVALID_REFRESH_TOKEN);
        }
        setRefreshTokenCookie(response, tokenInfo.refreshToken());
        return tokenInfo;
    }

    private void requireNoActiveDatabaseTransaction(String operation) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            throw new IllegalStateException(operation + " must run after the database transaction commits");
        }
    }

    private TransactionTemplate requiresNewTransaction() {
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        template.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        return template;
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
        } catch (NumberFormatException exception) {
            throw new RestApiException(errorStatus);
        }
    }

    private String requireRefreshJti(Claims claims) {
        String refreshJti = claims.getId();
        if (!StringUtils.hasText(refreshJti)) {
            throw new IllegalStateException("Generated refresh token must contain a JTI");
        }
        return refreshJti;
    }

    private boolean isAuthInfrastructureUnavailable(RestApiException exception) {
        return AUTH_INFRASTRUCTURE_UNAVAILABLE.getCode().getCode()
                .equals(exception.getErrorCode().getCode());
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

    private record RefreshUser(Long userId, UserRole role, AuthErrorStatus errorStatus) {
        static RefreshUser success(Long userId, UserRole role) {
            return new RefreshUser(userId, role, null);
        }

        static RefreshUser failure(AuthErrorStatus errorStatus) {
            return new RefreshUser(null, null, errorStatus);
        }
    }

}
