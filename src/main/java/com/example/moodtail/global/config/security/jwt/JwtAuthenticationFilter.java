package com.example.moodtail.global.config.security.jwt;

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
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
	// 오직 인증 정보를 설정하는 역할만 수행

	private final JwtProvider jwtTokenProvider;
	private final RedisRepository redisRepository;
	private final AntPathMatcher pathMatcher = new AntPathMatcher();
	private static final List<String> WHITELIST = List.of(
		"/api/v1/auth/**"
	);

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		String uri = request.getRequestURI();
		return WHITELIST.stream().anyMatch(p -> pathMatcher.match(p, uri));
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws
		ServletException,
		IOException {
		String uri = request.getRequestURI();
		if (isActuatorRequest(uri)) {
			chain.doFilter(request, response);
			return;
		}
		
		String token = jwtTokenProvider.resolveToken(request); // 헤더에서 토큰을 받아옴

		if (token != null && jwtTokenProvider.validateToken(token)) { // 토큰이 유효하다면
			Authentication authentication = getAuthentication(token); // 인증 정보를 받아옴
			SecurityContextHolder.getContext().setAuthentication(authentication); // 인증 정보를 설정

			if(!isLogoutOrOutRequest(uri)){
				PrincipalDetails principalDetails = (PrincipalDetails) authentication.getPrincipal();
				Long userId = principalDetails.getUserId();
				redisRepository.extendUserTimer(userId);
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

	private boolean isLogoutOrOutRequest(String uri) {
		return uri.equals("/api/v1/users/logout") || uri.equals("/api/v1/users");
	}

	private Authentication getAuthentication(String token) {
		Claims claims = jwtTokenProvider.getClaims(token);

		Long userId = Long.valueOf(claims.getSubject());
		String role = claims.get("role", String.class); // role 정보 추출 (기존 토큰 호환성을 위해 null 체크)

		PrincipalDetails principalDetails = new PrincipalDetails(userId, role);

		return new UsernamePasswordAuthenticationToken(principalDetails, "", principalDetails.getAuthorities());
	}
}
