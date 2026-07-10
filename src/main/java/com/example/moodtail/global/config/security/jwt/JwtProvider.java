package com.example.moodtail.global.config.security.jwt;

import com.example.moodtail.domain.user.enums.UserRole;
import com.example.moodtail.global.common.exception.RestApiException;
import com.example.moodtail.global.token.repository.redis.RedisRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.WeakKeyException;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

import static com.example.moodtail.global.common.exception.code.status.AuthErrorStatus.*;

@Component
@RequiredArgsConstructor
public class JwtProvider {

	private static final String ROLE_CLAIM = "role";
	private static final String TOKEN_TYPE_CLAIM = "tokenType";

	@Value("${jwt.secret}")
	private String jwtSecretKey;

	@Value("${jwt.accessExpiration}")
	private long jwtAccessExpiration;

	@Value("${jwt.refreshExpiration}")
	private long jwtRefreshExpiration;

	private SecretKey key;

	private final RedisRepository redisRepository;

	@PostConstruct
	protected void init() {
		if (!StringUtils.hasText(jwtSecretKey)) {
			throw new IllegalStateException("JWT secret is required");
		}
		if (jwtAccessExpiration <= 0 || jwtRefreshExpiration <= jwtAccessExpiration) {
			throw new IllegalStateException("JWT refresh expiration must be greater than access expiration");
		}
		try {
			byte[] keyBytes = Base64.getDecoder().decode(jwtSecretKey);
			this.key = Keys.hmacShaKeyFor(keyBytes);
		} catch (IllegalArgumentException | WeakKeyException e) {
			throw new IllegalStateException("JWT secret must be a valid Base64-encoded key of at least 256 bits", e);
		}
	}

	public String generateToken(Long userId, UserRole role, TokenType tokenType) {
		Date now = new Date();
		Date expiration;
		if (TokenType.ACCESS.equals(tokenType)) {
			expiration = calculateExpirationDate(now, jwtAccessExpiration);
		} else {
			expiration = calculateExpirationDate(now, jwtRefreshExpiration);
		}

		String jti = UUID.randomUUID().toString();

		Claims claims = Jwts.claims().setSubject(String.valueOf(userId));
		claims.put(ROLE_CLAIM, role.name());
		claims.put(TOKEN_TYPE_CLAIM, tokenType.name());

		return Jwts.builder()
		           .setClaims(claims)
		           .setIssuedAt(now)
		           .setExpiration(expiration)
		           .setId(jti)
			           .signWith(key, SignatureAlgorithm.HS256)
		           .compact();
	}

	public TokenInfo generateToken(Long userId, UserRole role) {
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
		return validateToken(token, null);
	}

	public boolean validateAccessToken(String token) {
		return validateToken(token, TokenType.ACCESS);
	}

	public boolean validateRefreshToken(String token) {
		return validateToken(token, TokenType.REFRESH);
	}

	private boolean validateToken(String token, TokenType expectedTokenType) {
		try {
			Claims claims = parseClaims(token);
			String jti = claims.getId();
			if (!StringUtils.hasText(jti) || Boolean.TRUE.equals(redisRepository.isJtiBlocked(jti))) {
				return false;
			}

			return expectedTokenType == null || expectedTokenType.equals(resolveTokenType(claims));

		} catch (JwtException | IllegalArgumentException e) {
			return false;
		}
	}

	public Claims getAccessTokenClaims(String token) {
		try {
			Claims claims = parseClaims(token);
			if (!TokenType.ACCESS.equals(resolveTokenType(claims))) {
				throw new RestApiException(INVALID_ACCESS_TOKEN);
			}
			return claims;
		} catch (ExpiredJwtException e) {
			throw new RestApiException(EXPIRED_USER_JWT);
		} catch (RestApiException e) {
			throw e;
		} catch (JwtException | IllegalArgumentException e) {
			throw new RestApiException(INVALID_ACCESS_TOKEN);
		}
	}

	public Claims getRefreshTokenClaims(String token) {
		try {
			Claims claims = parseClaims(token);
			if (!TokenType.REFRESH.equals(resolveTokenType(claims))) {
				throw new RestApiException(INVALID_REFRESH_TOKEN);
			}
			return claims;
		} catch (ExpiredJwtException e) {
			throw new RestApiException(EXPIRED_REFRESH_TOKEN);
		} catch (RestApiException e) {
			throw e;
		} catch (JwtException | IllegalArgumentException e) {
			throw new RestApiException(INVALID_REFRESH_TOKEN);
		}
	}

	private Claims parseClaims(String token) {
		return Jwts.parserBuilder()
		           .setSigningKey(key)
		           .build()
		           .parseClaimsJws(token)
		           .getBody();
	}

	private TokenType resolveTokenType(Claims claims) {
		String tokenType = claims.get(TOKEN_TYPE_CLAIM, String.class);
		if (!StringUtils.hasText(tokenType)) {
			return null;
		}
		try {
			return TokenType.valueOf(tokenType);
		} catch (IllegalArgumentException e) {
			return null;
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
