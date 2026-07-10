package com.example.moodtail.domain.user.service;

import com.example.moodtail.domain.user.dto.request.LoginRequest;
import com.example.moodtail.domain.user.dto.request.SignupRequest;
import com.example.moodtail.domain.user.dto.response.LoginResponse;
import com.example.moodtail.domain.user.dto.response.SignupResponse;
import com.example.moodtail.domain.user.entity.User;
import com.example.moodtail.domain.user.entity.UserRole;
import com.example.moodtail.domain.user.repository.UserRepository;
import com.example.moodtail.global.common.exception.RestApiException;
import com.example.moodtail.global.common.exception.code.status.AuthErrorStatus;
import com.example.moodtail.global.common.exception.code.status.GlobalErrorStatus;
import com.example.moodtail.global.config.security.jwt.JwtProvider;
import com.example.moodtail.global.config.security.jwt.TokenInfo;
import com.example.moodtail.global.token.repository.redis.RedisRepository;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final RedisRepository redisRepository;

    @Transactional
    public SignupResponse signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new RestApiException(GlobalErrorStatus._BAD_REQUEST);
        }

        User user = userRepository.save(User.builder()
                .email(request.email())
                .nickname(request.nickname())
                .password(passwordEncoder.encode(request.password()))
                .role(UserRole.USER)
                .build());

        return new SignupResponse(user.getId(), user.getEmail(), user.getNickname());
    }

    public LoginResponse login(LoginRequest request, HttpServletResponse response) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new RestApiException(AuthErrorStatus.INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new RestApiException(AuthErrorStatus.INVALID_CREDENTIALS);
        }

        TokenInfo tokenInfo = jwtProvider.generateToken(user.getId(), user.getRole().name());
        Claims refreshClaims = jwtProvider.getClaims(tokenInfo.refreshToken());

        redisRepository.saveRefreshJti(user.getId(), refreshClaims.getId());
        addRefreshTokenCookie(response, tokenInfo.refreshToken());

        return new LoginResponse(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                tokenInfo.accessToken()
        );
    }

    private void addRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        Cookie refreshTokenCookie = new Cookie("refreshToken", refreshToken);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(false);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(14 * 24 * 60 * 60);

        response.addCookie(refreshTokenCookie);
    }
}
