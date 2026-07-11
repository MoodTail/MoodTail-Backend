package com.example.moodtail.global.token.repository.redis.impl;

import com.example.moodtail.domain.user.config.AuthProperties;
import com.example.moodtail.global.token.repository.redis.RedisRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
@Slf4j
public class RedisRepositoryImpl implements RedisRepository {

	private static final String REFRESH_TOKEN_KEY_PREFIX = "refresh:";
	private static final String ACCESS_BLACKLIST_KEY_PREFIX = "access-blacklist:";
	private static final String OAUTH_STATE_KEY_PREFIX = "oauth-state:";
	private static final String OAUTH_STATE_OWNER_KEY_PREFIX = "oauth-state-owner:";
	private static final String OAUTH_STATE_RATE_KEY_PREFIX = "oauth-state-rate:";
	private static final String GUEST_LOGIN_RATE_KEY_PREFIX = "guest-rate:";
	private static final String PASSWORD_RESET_CLIENT_RATE_KEY_PREFIX = "password-reset-client-rate:";
	private static final String PASSWORD_RESET_COOLDOWN_KEY_PREFIX = "password-reset-cooldown:";
	private static final String PASSWORD_RESET_CODE_KEY_PREFIX = "password-reset-code:";
	private static final String PASSWORD_RESET_TOKEN_KEY_PREFIX = "password-reset-token:";
	private static final DefaultRedisScript<Long> SAVE_SINGLE_ACTIVE_VALUE_SCRIPT = new DefaultRedisScript<>(
			"local previous = redis.call('get', KEYS[1]); "
					+ "if previous then redis.call('del', ARGV[1] .. previous); end; "
					+ "redis.call('set', KEYS[2], ARGV[2], 'PX', ARGV[3]); "
					+ "redis.call('set', KEYS[1], ARGV[4], 'PX', ARGV[3]); "
					+ "return 1;",
			Long.class
	);
	private static final DefaultRedisScript<String> CONSUME_VALUE_SCRIPT = new DefaultRedisScript<>(
			"local value = redis.call('get', KEYS[1]); "
					+ "if value then redis.call('del', KEYS[1]); end; "
					+ "return value;",
			String.class
	);
	private static final DefaultRedisScript<Long> REPLACE_REFRESH_JTI_SCRIPT = new DefaultRedisScript<>(
			"if redis.call('get', KEYS[1]) == ARGV[1] then "
					+ "redis.call('set', KEYS[1], ARGV[2], 'PX', ARGV[3]); return 1; "
					+ "end; return 0",
			Long.class
	);
	private static final DefaultRedisScript<Long> GUEST_LOGIN_RATE_SCRIPT = new DefaultRedisScript<>(
			"local current = redis.call('incr', KEYS[1]); "
					+ "if current == 1 then redis.call('pexpire', KEYS[1], ARGV[1]); end; "
					+ "return current;",
			Long.class
	);
	private static final DefaultRedisScript<String> VERIFY_PASSWORD_RESET_CODE_SCRIPT = new DefaultRedisScript<>(
			"local value = redis.call('get', KEYS[1]); "
					+ "if not value then return nil; end; "
					+ "local accountId, version, digest, attempts = string.match(value, '([^:]+):([^:]+):([^:]+):([^:]+)'); "
					+ "if not accountId then redis.call('del', KEYS[1]); return nil; end; "
					+ "attempts = tonumber(attempts); "
					+ "if digest == ARGV[1] then redis.call('del', KEYS[1]); return accountId .. ':' .. version; end; "
					+ "attempts = attempts + 1; "
					+ "if attempts >= tonumber(ARGV[2]) then redis.call('del', KEYS[1]); return nil; end; "
					+ "redis.call('set', KEYS[1], accountId .. ':' .. version .. ':' .. digest .. ':' .. attempts, 'KEEPTTL'); "
					+ "return nil;",
			String.class
	);

	private final RedisTemplate<String, String> redisTemplate;
	private final AuthProperties authProperties;
	private final ObjectMapper objectMapper;
	private static final long USAGE_EXPIRATION_TIME = 60 * 60 * 24;		// in이 찍히고 24시간동안 out이 안된다면 삭제
	private static final long REFRESH_EXPIRATION_TIME = 60 * 60 * 24 * 14;

	@Value("${app.usage.timeout}")
	private long usageTimeout;

	@Value("${jwt.refreshExpiration}")
	private long jwtRefreshExpirationMillis;

	// refresh token을 key로 저장 (rotation 시 유리)
	@Override
	public void save(Long userId, String refreshToken) {
		redisTemplate.opsForValue()
				.set("refresh:" + refreshToken, userId.toString(), REFRESH_EXPIRATION_TIME, TimeUnit.SECONDS);
	}

	@Override
	public Optional<Long> findUserIdByToken(String refreshToken) {
		String userId = redisTemplate.opsForValue().get("refresh:" + refreshToken);
		return Optional.ofNullable(userId).map(Long::valueOf);
	}

	@Override
	public Boolean delete(String refreshToken) {
		return redisTemplate.delete("refresh:" + refreshToken);
	}

	@Override
	public Boolean blockAccessToken(String accessToken, Claims claims) {
		// TTL = 토큰 만료 시각 - 현재 시각
		Date expiration = claims.getExpiration();
		long ttl = expiration.getTime() - System.currentTimeMillis();

		if (ttl > 0) {
			String jti = claims.getId();
			if (jti == null || jti.isBlank()) {
				return false;
			}
			redisTemplate.opsForValue()
			             .set(createAccessBlacklistKey(jti), "blacklisted", ttl, TimeUnit.MILLISECONDS);
		}

		return true;
	}

	@Override
	public Boolean isJtiBlocked(String jti) {
		return redisTemplate.hasKey(createAccessBlacklistKey(jti));
	}

	@Override
	public void saveUserInTime(Long userId, LocalDateTime inTime) {
		String dataKey = "usage:in:" + userId;		// 예시: usage:in:12, value: 2024-02-10T14:30:00
		String triggerKey = "usage:trigger:" + userId;

		redisTemplate.opsForValue().set(dataKey, inTime.toString(), USAGE_EXPIRATION_TIME, TimeUnit.SECONDS);
		redisTemplate.opsForValue().set(triggerKey, "", usageTimeout, TimeUnit.MINUTES);
	}

	@Override
	public Optional<LocalDateTime> getUserInTime(Long userId) {
		String key = "usage:in:" + userId;
		String value = redisTemplate.opsForValue().get(key);
		return Optional.ofNullable(value).map(LocalDateTime::parse);
	}

	@Override
	public void extendUserTimer(Long userId) {
		String triggerKey = "usage:trigger:" + userId;
		Boolean exists = redisTemplate.hasKey(triggerKey);

		if (Boolean.TRUE.equals(exists)) {
			// 트리거가 만료되지 않고 api호출이 됐을 때.
			redisTemplate.expire(triggerKey, usageTimeout, TimeUnit.MINUTES);
		} else {
			// 트리거가 만료된 뒤에 api호출이 됐을 때.
			LocalDateTime now = LocalDateTime.now();
			saveUserInTime(userId, now);
		}
	}

	@Override
	public void deleteUserInTime(Long userId) {
		String key = "usage:in:" + userId;
		redisTemplate.delete(key);
	}

	@Override
	public void deleteUserTrigger(Long userId) {
		String key = "usage:trigger:" + userId;
		redisTemplate.delete(key);
	}

	@Override
	public List<Long> getAllActiveUserIds() {
		Set<String> keys = redisTemplate.keys("usage:in:*");
		if (keys == null || keys.isEmpty()) return Collections.emptyList();
		return keys.stream()
				.map(key -> {
					String userIdStr = key.replace("usage:in:", "");
					try {
						return Long.parseLong(userIdStr);
					} catch (NumberFormatException e) {
						return null;
					}
				})
				.filter(id -> id != null)
				.collect(Collectors.toList());
	}

	@Override
	public void saveRefreshJti(Long userId, String refreshJti){
		redisTemplate.opsForValue().set(
				createRefreshTokenKey(userId),
				refreshJti,
				Duration.ofMillis(jwtRefreshExpirationMillis)
		);
	}

	@Override
	public boolean replaceRefreshJti(Long userId, String expectedRefreshJti, String newRefreshJti) {
		Long replaced = redisTemplate.execute(
				REPLACE_REFRESH_JTI_SCRIPT,
				List.of(createRefreshTokenKey(userId)),
				expectedRefreshJti,
				newRefreshJti,
				String.valueOf(jwtRefreshExpirationMillis)
		);
		return Long.valueOf(1L).equals(replaced);
	}

	@Override
	public Optional<String> findRefreshJtiByUserId(Long userId) {
		String key = createRefreshTokenKey(userId);
		String refreshJti = redisTemplate.opsForValue().get(key);
		return Optional.ofNullable(refreshJti);
	}

	@Override
	public void deleteRefreshJti(Long userId) {
		String key = createRefreshTokenKey(userId);
		redisTemplate.delete(key);
	}

	@Override
	public void saveOAuthState(
			String state,
			Long guestUserId,
			String provider,
			String codeVerifier,
			Duration ttl
	) {
		String normalizedProvider = normalizeProvider(provider);
		String stateKeyPrefix = createOAuthStateKeyPrefix(normalizedProvider);
		redisTemplate.execute(
				SAVE_SINGLE_ACTIVE_VALUE_SCRIPT,
				List.of(
						createAuthKey(OAUTH_STATE_OWNER_KEY_PREFIX + normalizedProvider + ":" + guestUserId),
						stateKeyPrefix + state
				),
				stateKeyPrefix,
				serialize(new OAuthStateSession(guestUserId, codeVerifier)),
				String.valueOf(ttl.toMillis()),
				state
		);
	}

	@Override
	public Optional<OAuthStateSession> consumeOAuthStateSession(String state, String provider) {
		String value = redisTemplate.execute(
				CONSUME_VALUE_SCRIPT,
				List.of(createOAuthStateKeyPrefix(normalizeProvider(provider)) + state)
		);
		return deserialize(value, OAuthStateSession.class);
	}

	@Override
	public boolean acquireOAuthStateSlot(
			Long guestUserId,
			String provider,
			int maxAttempts,
			Duration window
	) {
		Long attempts = redisTemplate.execute(
				GUEST_LOGIN_RATE_SCRIPT,
				List.of(createAuthKey(
						OAUTH_STATE_RATE_KEY_PREFIX + normalizeProvider(provider) + ":" + guestUserId
				)),
				String.valueOf(window.toMillis())
		);
		return attempts != null && attempts <= maxAttempts;
	}

	@Override
	public boolean acquireGuestLoginSlot(String fingerprint, int maxAttempts, Duration window) {
		Long attempts = redisTemplate.execute(
				GUEST_LOGIN_RATE_SCRIPT,
				List.of(createAuthKey(GUEST_LOGIN_RATE_KEY_PREFIX + fingerprint)),
				String.valueOf(window.toMillis())
		);
		return attempts != null && attempts <= maxAttempts;
	}

	@Override
	public boolean acquirePasswordResetClientSlot(String fingerprint, int maxAttempts, Duration window) {
		Long attempts = redisTemplate.execute(
				GUEST_LOGIN_RATE_SCRIPT,
				List.of(createAuthKey(PASSWORD_RESET_CLIENT_RATE_KEY_PREFIX + fingerprint)),
				String.valueOf(window.toMillis())
		);
		return attempts != null && attempts <= maxAttempts;
	}

	@Override
	public boolean acquirePasswordResetCooldown(String emailFingerprint, Duration ttl) {
		return Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(
				createAuthKey(PASSWORD_RESET_COOLDOWN_KEY_PREFIX + emailFingerprint),
				"1",
				ttl
		));
	}

	@Override
	public void savePasswordResetCode(
			String emailFingerprint,
			Long localAccountId,
			int passwordVersion,
			String codeDigest,
			Duration ttl
	) {
		redisTemplate.opsForValue().set(
				createAuthKey(PASSWORD_RESET_CODE_KEY_PREFIX + emailFingerprint),
				localAccountId + ":" + passwordVersion + ":" + codeDigest + ":0",
				ttl
		);
	}

	@Override
	public Optional<PasswordResetTokenSession> verifyPasswordResetCode(
			String emailFingerprint,
			String codeDigest,
			int maxAttempts
	) {
		String result = redisTemplate.execute(
				VERIFY_PASSWORD_RESET_CODE_SCRIPT,
				List.of(createAuthKey(PASSWORD_RESET_CODE_KEY_PREFIX + emailFingerprint)),
				codeDigest,
				String.valueOf(maxAttempts)
		);
		if (result == null) {
			return Optional.empty();
		}
		String[] parts = result.split(":", 2);
		try {
			return Optional.of(new PasswordResetTokenSession(
					Long.valueOf(parts[0]),
					Integer.parseInt(parts[1])
			));
		} catch (RuntimeException e) {
			return Optional.empty();
		}
	}

	@Override
	public void deletePasswordResetCode(String emailFingerprint) {
		redisTemplate.delete(createAuthKey(PASSWORD_RESET_CODE_KEY_PREFIX + emailFingerprint));
	}

	@Override
	public void savePasswordResetToken(String token, PasswordResetTokenSession session, Duration ttl) {
		redisTemplate.opsForValue().set(
				createAuthKey(PASSWORD_RESET_TOKEN_KEY_PREFIX + token),
				serialize(session),
				ttl
		);
	}

	@Override
	public Optional<PasswordResetTokenSession> consumePasswordResetToken(String token) {
		String value = redisTemplate.execute(
				CONSUME_VALUE_SCRIPT,
				List.of(createAuthKey(PASSWORD_RESET_TOKEN_KEY_PREFIX + token))
		);
		return deserialize(value, PasswordResetTokenSession.class);
	}

	@Override
	public void saveLastLogin(String email, LocalDateTime lastLogin){
		String key = "last_login:" + email;
		redisTemplate.opsForValue().set(key, lastLogin.toString(), REFRESH_EXPIRATION_TIME, TimeUnit.SECONDS);
	}

	@Override
	public LocalDateTime getLastLogin(String email){
		String key = "last_login:" + email;
		String value = redisTemplate.opsForValue().get(key);
		if(value == null){
			return null;
		}
		return LocalDateTime.parse(value);
	}

	private String createRefreshTokenKey(Long userId) {
		return createAuthKey(REFRESH_TOKEN_KEY_PREFIX + userId);
	}

	private String createOAuthStateKeyPrefix(String provider) {
		return createAuthKey(OAUTH_STATE_KEY_PREFIX + provider + ":");
	}

	private String createAccessBlacklistKey(String jti) {
		return createAuthKey(ACCESS_BLACKLIST_KEY_PREFIX + jti);
	}

	private String createAuthKey(String suffix) {
		return authProperties.redis().keyPrefix() + suffix;
	}

	private String normalizeProvider(String provider) {
		return provider.toLowerCase(Locale.ROOT);
	}

	private String serialize(Object value) {
		try {
			return objectMapper.writeValueAsString(value);
		} catch (JsonProcessingException e) {
			throw new IllegalStateException("Failed to serialize auth Redis value", e);
		}
	}

	private <T> Optional<T> deserialize(String value, Class<T> type) {
		if (value == null) {
			return Optional.empty();
		}
		try {
			return Optional.of(objectMapper.readValue(value, type));
		} catch (JsonProcessingException e) {
			log.warn("Discarding an invalid auth Redis value for type {}", type.getSimpleName());
			return Optional.empty();
		}
	}

}
