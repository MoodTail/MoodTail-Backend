package com.example.moodtail.global.config.security.jwt;

import com.example.moodtail.domain.user.entity.User;
import com.example.moodtail.domain.user.entity.UserRole;
import com.example.moodtail.domain.user.repository.UserRepository;
import com.example.moodtail.global.common.exception.RestApiException;
import com.example.moodtail.global.common.exception.code.status.AuthErrorStatus;
import com.example.moodtail.global.config.security.auth.PrincipalDetails;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Locale;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtProvider jwtTokenProvider;
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
		String token = resolveAccessToken(request);
		Claims claims = token == null
				? null
				: jwtTokenProvider.validateAccessTokenAndGetClaims(token).orElse(null);

		if (claims != null) {
			Authentication authentication = getAuthentication(claims);
			SecurityContextHolder.getContext().setAuthentication(authentication);
		}
		chain.doFilter(request, response);
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

	private Authentication getAuthentication(Claims claims) {
		Long userId = parseUserId(claims.getSubject());
		String roleClaim = claims.get("role", String.class);
		UserRole tokenRole = parseRole(roleClaim);
		User user;
		try {
			user = userRepository.findAuthUserById(userId)
					.orElseThrow(() -> new RestApiException(AuthErrorStatus.USER_NOT_FOUND));
		} catch (DataAccessException exception) {
			throw new RestApiException(AuthErrorStatus.AUTH_INFRASTRUCTURE_UNAVAILABLE);
		}
		if (!user.isAvailableForAuthentication()) {
			throw new RestApiException(AuthErrorStatus.INACTIVE_USER);
		}
		if (user.getRole() != tokenRole) {
			throw new RestApiException(AuthErrorStatus.INVALID_ROLE);
		}

		PrincipalDetails principalDetails = new PrincipalDetails(userId, user.getRole());

		return new UsernamePasswordAuthenticationToken(principalDetails, "", principalDetails.getAuthorities());
	}

	private String resolveAccessToken(HttpServletRequest request) {
		String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
		if (StringUtils.hasText(authorization) && authorization.startsWith("Bearer ")) {
			return authorization.substring(7);
		}
		return null;
	}

	private UserRole parseRole(String roleClaim) {
		if (roleClaim == null || roleClaim.isBlank()) {
			throw new RestApiException(AuthErrorStatus.INVALID_ROLE);
		}
		String normalizedRole = roleClaim.toUpperCase(Locale.ROOT);
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
