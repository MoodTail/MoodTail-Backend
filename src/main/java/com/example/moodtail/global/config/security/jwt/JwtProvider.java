package com.example.moodtail.global.config.security.jwt;

import com.example.moodtail.domain.user.entity.UserRole;
import com.example.moodtail.global.common.exception.RestApiException;
import com.example.moodtail.global.token.repository.redis.RedisRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.WeakKeyException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static com.example.moodtail.global.common.exception.code.status.AuthErrorStatus.EXPIRED_REFRESH_TOKEN;
import static com.example.moodtail.global.common.exception.code.status.AuthErrorStatus.INVALID_REFRESH_TOKEN;
import static com.example.moodtail.global.token.redis.AuthRedisFailurePolicy.required;

@Component
@RequiredArgsConstructor
public class JwtProvider {

	private static final String ROLE_CLAIM = "role";
	private static final String TOKEN_TYPE_CLAIM = "tokenType";
	private static final String SESSION_ID_CLAIM = "sessionId";

	@Value("${jwt.secret}")
	private String jwtSecretKey;

	@Value("${jwt.accessExpiration}")
	private long jwtAccessExpiration;

	@Value("${jwt.guestAccessExpiration}")
	private long jwtGuestAccessExpiration;

	@Value("${jwt.refreshExpiration}")
	private long jwtRefreshExpiration;

	private SecretKey key;

	private final RedisRepository redisRepository;

	@PostConstruct
	protected void init() {
		if (!StringUtils.hasText(jwtSecretKey)) {
			throw new IllegalStateException("JWT secret is required");
		}
		if (jwtAccessExpiration <= 0 || jwtGuestAccessExpiration <= 0
				|| jwtRefreshExpiration <= jwtAccessExpiration
				|| jwtRefreshExpiration <= jwtGuestAccessExpiration) {
			throw new IllegalStateException("JWT refresh expiration must be greater than access expiration");
		}
		try {
			byte[] keyBytes = Base64.getDecoder().decode(jwtSecretKey);
			this.key = Keys.hmacShaKeyFor(keyBytes);
		} catch (IllegalArgumentException | WeakKeyException e) {
			throw new IllegalStateException("JWT secret must be a valid Base64-encoded key of at least 256 bits", e);
		}
	}

	private String generateToken(Long userId, UserRole role, TokenType tokenType, String sessionId) {
		Date now = new Date();
		Date expiration;
		if (TokenType.ACCESS.equals(tokenType)) {
			long accessExpiration = UserRole.GUEST.equals(role)
					? jwtGuestAccessExpiration
					: jwtAccessExpiration;
			expiration = calculateExpirationDate(now, accessExpiration);
		} else {
			expiration = calculateExpirationDate(now, jwtRefreshExpiration);
		}

		String jti = TokenType.REFRESH.equals(tokenType) ? sessionId : UUID.randomUUID().toString();

		Claims claims = Jwts.claims().setSubject(String.valueOf(userId));
		claims.put(ROLE_CLAIM, role.name());
		claims.put(TOKEN_TYPE_CLAIM, tokenType.name());
		claims.put(SESSION_ID_CLAIM, sessionId);

		return Jwts.builder()
		           .setClaims(claims)
		           .setIssuedAt(now)
		           .setExpiration(expiration)
		           .setId(jti)
			           .signWith(key, SignatureAlgorithm.HS256)
		           .compact();
	}

	public TokenInfo generateToken(Long userId, UserRole role) {
		String sessionId = UUID.randomUUID().toString();
		String accessToken = generateToken(userId, role, TokenType.ACCESS, sessionId);
		String refreshToken = generateToken(userId, role, TokenType.REFRESH, sessionId);

		return new TokenInfo(accessToken, refreshToken);
	}

	// 만료시간 계산
	private Date calculateExpirationDate(Date createdDate, long jwtExpiration) {
		return new Date(createdDate.getTime() + jwtExpiration);
	}

	public Optional<Claims> validateAccessTokenAndGetClaims(String token) {
		return validateAccessTokenClaims(token);
	}

	private Optional<Claims> validateAccessTokenClaims(String token) {
		try {
			Claims claims = parseClaims(token);
			TokenType actualTokenType = resolveTokenType(claims);
			if (!TokenType.ACCESS.equals(actualTokenType)) {
				return Optional.empty();
			}
			if (!StringUtils.hasText(claims.getId()) || !isCurrentSession(claims)) {
				return Optional.empty();
			}
			return Optional.of(claims);

		} catch (JwtException | IllegalArgumentException e) {
			return Optional.empty();
		}
	}

	private boolean isCurrentSession(Claims claims) {
		String sessionId = claims.get(SESSION_ID_CLAIM, String.class);
		if (!StringUtils.hasText(sessionId)) {
			return false;
		}
		Long userId;
		try {
			userId = Long.valueOf(claims.getSubject());
		} catch (NumberFormatException e) {
			return false;
		}
		return required(
				"validate current access-token session",
				() -> redisRepository.findRefreshJtiByUserId(userId)
		).filter(sessionId::equals).isPresent();
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

}
