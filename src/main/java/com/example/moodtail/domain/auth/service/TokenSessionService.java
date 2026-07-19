package com.example.moodtail.domain.auth.service;

import com.example.moodtail.domain.user.entity.User;
import com.example.moodtail.domain.user.entity.UserRole;
import com.example.moodtail.domain.user.repository.UserRepository;
import com.example.moodtail.global.common.exception.RestApiException;
import com.example.moodtail.global.common.exception.code.status.AuthErrorStatus;
import com.example.moodtail.global.config.security.jwt.JwtProvider;
import com.example.moodtail.global.config.security.jwt.TokenInfo;
import com.example.moodtail.global.token.repository.redis.RedisRepository;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import static com.example.moodtail.global.token.redis.AuthRedisFailurePolicy.bestEffort;
import static com.example.moodtail.global.token.redis.AuthRedisFailurePolicy.required;

@Service
@RequiredArgsConstructor
public class TokenSessionService {

    private final UserRepository userRepository;
    private final PlatformTransactionManager transactionManager;
    private final JwtProvider jwtProvider;
    private final RedisRepository redisRepository;

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
                "delete replaced guest refresh session",
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

    public TokenInfo reissue(String refreshToken) {
        requireNoActiveDatabaseTransaction("reissue authentication session");
        if (!StringUtils.hasText(refreshToken)) {
            throw new RestApiException(AuthErrorStatus.EMPTY_JWT);
        }

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
        return rotateRefreshToken(
                refreshUser.userId(),
                refreshUser.role(),
                refreshJti
        );
    }

    public void logout(String accessToken, String refreshToken) {
        Optional<Long> accessTokenUserId = resolveAccessTokenUser(accessToken);
        Optional<Long> refreshTokenUserId = resolveCurrentRefreshSessionUser(refreshToken);
        Set<Long> sessionUserIds = new LinkedHashSet<>();
        accessTokenUserId.ifPresent(sessionUserIds::add);
        refreshTokenUserId.ifPresent(sessionUserIds::add);
        for (Long userId : sessionUserIds) {
            required(
                    "delete logout refresh session",
                    () -> redisRepository.deleteRefreshJti(userId)
            );
        }
    }

    private Optional<Long> resolveAccessTokenUser(String accessToken) {
        if (!StringUtils.hasText(accessToken)) {
            return Optional.empty();
        }

        Optional<Claims> accessClaimsOptional = jwtProvider.validateAccessTokenAndGetClaims(accessToken);
        if (accessClaimsOptional.isEmpty()) {
            return Optional.empty();
        }
        Claims accessClaims = accessClaimsOptional.get();
        Long userId;
        try {
            userId = parseUserId(accessClaims, AuthErrorStatus.INVALID_ACCESS_TOKEN);
        } catch (RestApiException ignored) {
            return Optional.empty();
        }

        return Optional.of(userId);
    }

    private Optional<Long> resolveCurrentRefreshSessionUser(String refreshToken) {
        if (!StringUtils.hasText(refreshToken)) {
            return Optional.empty();
        }

        Claims refreshClaims;
        Long userId;
        try {
            refreshClaims = jwtProvider.getRefreshTokenClaims(refreshToken);
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
            Optional<User> userOptional = userRepository.findAuthUserById(userId);
            if (userOptional.isEmpty()) {
                return RefreshUser.failure(AuthErrorStatus.USER_NOT_FOUND);
            }
            User user = userOptional.get();
            if (!user.isAvailableForAuthentication()) {
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
            String expectedRefreshJti
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

    private record RefreshUser(Long userId, UserRole role, AuthErrorStatus errorStatus) {
        static RefreshUser success(Long userId, UserRole role) {
            return new RefreshUser(userId, role, null);
        }

        static RefreshUser failure(AuthErrorStatus errorStatus) {
            return new RefreshUser(null, null, errorStatus);
        }
    }

}
