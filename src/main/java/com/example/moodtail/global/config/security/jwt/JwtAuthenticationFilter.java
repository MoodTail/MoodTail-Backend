package com.example.moodtail.global.config.security.jwt;

import com.example.moodtail.domain.user.entity.User;
import com.example.moodtail.domain.user.entity.UserRole;
import com.example.moodtail.domain.user.repository.UserRepository;
import com.example.moodtail.global.common.exception.RestApiException;
import com.example.moodtail.global.common.exception.code.status.AuthErrorStatus;
import com.example.moodtail.global.config.security.auth.PrincipalDetails;
import com.example.moodtail.global.token.repository.redis.RedisRepository;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Locale;

import static com.example.moodtail.global.token.redis.AuthRedisFailurePolicy.bestEffort;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
	// 오직 인증 정보를 설정하는 역할만 수행

	private final JwtProvider jwtTokenProvider;
	private final RedisRepository redisRepository;
	private final UserRepository userRepository;
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws
		ServletException,
		IOException {
		String uri = request.getRequestURI();
		if (isActuatorRequest(uri) || isLogoutRequest(uri)) {
			chain.doFilter(request, response);
			return;
		}
		String token = jwtTokenProvider.resolveToken(request); // 헤더에서 토큰을 받아옴

		if (token != null && jwtTokenProvider.validateAccessToken(token)) { // 토큰이 유효하다면
			Authentication authentication = getAuthentication(token); // 인증 정보를 받아옴
			SecurityContextHolder.getContext().setAuthentication(authentication); // 인증 정보를 설정

			if (!isUserOutRequest(uri)) {
				PrincipalDetails principalDetails = (PrincipalDetails) authentication.getPrincipal();
				Long userId = principalDetails.getUserId();
				bestEffort("extend user activity timer", () -> redisRepository.extendUserTimer(userId));
			}
		}
		chain.doFilter(request, response); // 다음 필터로 넘김
	}

	private boolean isActuatorRequest(String uri) {
		if (uri == null) {
			return false;
		}
		return uri.equals("/actuator") || uri.startsWith("/actuator/");
	}

	private boolean isLogoutRequest(String uri) {
		return "/api/v1/auth/logout".equals(uri);
	}

	private boolean isUserOutRequest(String uri) {
		return "/api/v1/users".equals(uri);
	}

	private Authentication getAuthentication(String token) {
		Claims claims = jwtTokenProvider.getAccessTokenClaims(token);

		Long userId = parseUserId(claims.getSubject());
		String roleClaim = claims.get("role", String.class);
		UserRole tokenRole = parseRole(roleClaim);
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new RestApiException(AuthErrorStatus.USER_NOT_FOUND));
		if (!user.isActive() || user.isDeleted()) {
			throw new RestApiException(AuthErrorStatus.INACTIVE_USER);
		}
		if (user.getRole() != tokenRole) {
			throw new RestApiException(AuthErrorStatus.INVALID_ROLE);
		}

		PrincipalDetails principalDetails = new PrincipalDetails(userId, user.getRole());

		return new UsernamePasswordAuthenticationToken(principalDetails, "", principalDetails.getAuthorities());
	}

	private UserRole parseRole(String roleClaim) {
		if (roleClaim == null || roleClaim.isBlank()) {
			throw new RestApiException(AuthErrorStatus.INVALID_ROLE);
		}
		String normalizedRole = roleClaim.toUpperCase(Locale.ROOT);
		if (normalizedRole.startsWith("ROLE_")) {
			normalizedRole = normalizedRole.substring("ROLE_".length());
		}
		try {
			return UserRole.valueOf(normalizedRole);
		} catch (IllegalArgumentException e) {
			throw new RestApiException(AuthErrorStatus.INVALID_ROLE);
		}
	}

	private Long parseUserId(String subject) {
		try {
			return Long.valueOf(subject);
		} catch (NumberFormatException e) {
			throw new RestApiException(AuthErrorStatus.INVALID_ACCESS_TOKEN);
		}
	}
}
