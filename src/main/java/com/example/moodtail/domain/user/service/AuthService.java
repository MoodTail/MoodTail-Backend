package com.example.moodtail.domain.user.service;

import com.example.moodtail.domain.user.dto.response.AuthResponse;
import com.example.moodtail.domain.user.entity.SocialAccount;
import com.example.moodtail.domain.user.entity.SocialProvider;
import com.example.moodtail.domain.user.entity.User;
import com.example.moodtail.domain.user.model.SocialUserProfile;
import com.example.moodtail.domain.user.repository.SocialAccountRepository;
import com.example.moodtail.domain.user.repository.UserRepository;
import com.example.moodtail.global.common.exception.RestApiException;
import com.example.moodtail.global.common.exception.code.status.AuthErrorStatus;
import com.example.moodtail.global.config.security.jwt.JwtProvider;
import com.example.moodtail.global.config.security.jwt.TokenInfo;
import com.example.moodtail.global.token.repository.redis.RedisRepository;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";
    private static final int REFRESH_TOKEN_COOKIE_MAX_AGE_SECONDS = 14 * 24 * 60 * 60;
    private static final String DEFAULT_NICKNAME = "moodtail-user";

    private final UserRepository userRepository;
    private final SocialAccountRepository socialAccountRepository;
    private final JwtProvider jwtProvider;
    private final RedisRepository redisRepository;

    @Transactional
    public AuthResponse loginOrSignupSocial(
            SocialProvider provider,
            SocialUserProfile profile,
            HttpServletResponse response
    ) {
        validateSocialProfile(provider, profile);

        SocialAccount socialAccount = socialAccountRepository
                .findByProviderAndProviderUserId(provider, profile.providerUserId())
                .orElse(null);

        boolean isNewUser = socialAccount == null;
        User user = isNewUser ? createSocialUser(provider, profile) : socialAccount.getUser();
        if (user.isWithdrawn()) {
            throw new RestApiException(AuthErrorStatus.USER_NOT_FOUND);
        }

        TokenInfo tokenInfo = jwtProvider.generateToken(user.getId(), user.getRoleAuthority());
        Claims refreshClaims = jwtProvider.getClaims(tokenInfo.refreshToken());

        redisRepository.saveRefreshJti(user.getId(), refreshClaims.getId());
        addRefreshTokenCookie(response, tokenInfo.refreshToken());

        return AuthResponse.of(user, tokenInfo, isNewUser);
    }

    private void validateSocialProfile(SocialProvider provider, SocialUserProfile profile) {
        if (provider == null
                || profile == null
                || !StringUtils.hasText(profile.providerUserId())
                || !StringUtils.hasText(profile.email())) {
            throw new RestApiException(AuthErrorStatus.FAILED_SOCIAL_LOGIN);
        }
    }

    private User createSocialUser(SocialProvider provider, SocialUserProfile profile) {
        User user = userRepository.save(User.createSocialUser(resolveNickname(profile)));
        socialAccountRepository.save(SocialAccount.create(
                user,
                profile.email(),
                provider,
                profile.providerUserId()
        ));
        return user;
    }

    private String resolveNickname(SocialUserProfile profile) {
        if (StringUtils.hasText(profile.nickname())) {
            return profile.nickname();
        }
        return DEFAULT_NICKNAME;
    }

    private void addRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        Cookie refreshTokenCookie = new Cookie(REFRESH_TOKEN_COOKIE_NAME, refreshToken);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(false);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(REFRESH_TOKEN_COOKIE_MAX_AGE_SECONDS);

        response.addCookie(refreshTokenCookie);
    }
}
