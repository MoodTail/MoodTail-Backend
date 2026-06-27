package com.example.moodtail.global.config.security.jwt;

import com.example.moodtail.global.common.exception.RestApiException;
import com.example.moodtail.global.token.repository.redis.RedisRepository;
import io.jsonwebtoken.*;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

import static com.example.moodtail.global.common.exception.code.status.AuthErrorStatus.*;

@Component
@RequiredArgsConstructor
public class JwtProvider {

	@Value("${jwt.secret}")
	private String jwtSecretKey;

	@Value("${jwt.accessExpiration}")
	private long jwtAccessExpiration;

	@Value("${jwt.refreshExpiration}")
	private long jwtRefreshExpiration;

	private Key key;

	private final RedisRepository redisRepository;

	@PostConstruct
	protected void init() {
		byte[] keyBytes = Base64.getDecoder().decode(jwtSecretKey);
		this.key = new SecretKeySpec(keyBytes, SignatureAlgorithm.HS256.getJcaName());
	}

	public String generateToken(Long userId, String role, TokenType tokenType) {
		Date now = new Date();
		Date expiration;
		// 분기 나눠야해, 리프레쉬 토큰과 액세스 토큰의 만료시간이 다르니까
		if (TokenType.ACCESS.equals(tokenType)) { // 액세스 토큰
			expiration = calculateExpirationDate(now, jwtAccessExpiration);
		} else { // 리프레쉬 토큰
			expiration = calculateExpirationDate(now, jwtRefreshExpiration);
		}

		String jti = UUID.randomUUID().toString();

		Claims claims = Jwts.claims().setSubject(String.valueOf(userId)); // JWT payload 에 저장되는 정보단위
		claims.put("role", role);

		return Jwts.builder()
		           .setClaims(claims)
		           .setIssuedAt(now)
		           .setExpiration(expiration)
		           .signWith(key)
					.setId(jti)
		           .compact();
	}

	public TokenInfo generateToken(Long userId, String role) {
		String accessToken = generateToken(userId, role, TokenType.ACCESS);
		String refreshToken = generateToken(userId, role, TokenType.REFRESH);

		return new TokenInfo(accessToken, refreshToken);
	}

	// 만료시간 계산
	private Date calculateExpirationDate(Date createdDate, long jwtExpiration) {
		return new Date(createdDate.getTime() + jwtExpiration);
	}

	// 토큰 정보를 검증하는 메서드
	public boolean validateToken(String token) {
		try {
			Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);

			// 블랙리스트 여부 검사
			// access token 이든, refresh token 이든, 블랙리스트(access token 전용)에만 안 들어가 있으면 되기 때문에, 따로 분기 X
			Claims claims = Jwts.parserBuilder()
					.setSigningKey(key)
					.build()
					.parseClaimsJws(token)
					.getBody();

			String jti = claims.getId();

			return !redisRepository.isJtiBlocked(jti);

		} catch (JwtException | IllegalArgumentException e) {
			return false; // 유효하지 않은 토큰 처리
		}
	}

	public Claims getClaims(String token) {
		try {
			return Jwts.
				parserBuilder().
				setSigningKey(key).
				build().
				parseClaimsJws(token).
				getBody();
		} catch (Exception e) {
			throw new RestApiException(INVALID_REFRESH_TOKEN);
		}
	}

	// Request Header에서 토큰 정보 추출
	public String resolveToken(HttpServletRequest request) {
		String token = request.getHeader("Authorization");
		if (StringUtils.hasText(token) && token.startsWith("Bearer ")) {
			return token.substring(7);
		}
		return null;
	}
}
