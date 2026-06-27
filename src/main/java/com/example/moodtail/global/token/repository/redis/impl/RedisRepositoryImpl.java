package com.example.moodtail.global.token.repository.redis.impl;

import com.example.moodtail.global.token.repository.redis.RedisRepository;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class RedisRepositoryImpl implements RedisRepository {

	private final RedisTemplate<String, String> redisTemplate;
	private static final long USAGE_EXPIRATION_TIME = 60 * 60 * 24;		// in이 찍히고 24시간동안 out이 안된다면 삭제
	private static final long REFRESH_EXPIRATION_TIME = 60 * 60 * 24 * 14;

	@Value("${app.usage.timeout}")
	private long usageTimeout;

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
			// key = 토큰 문자열 그대로, value = 상태값 (blacklisted 라는 값은 그냥 value 채우기 용)
			redisTemplate.opsForValue()
			             .set("blacklist_access_token:" + accessToken, "blacklisted", ttl, TimeUnit.MILLISECONDS);
		}

		return true;
	}

	@Override
	public Boolean isJtiBlocked(String accessToken) {
		return redisTemplate.hasKey("blacklist_access_token:" + accessToken);
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
				"RT:" + userId,
				refreshJti,
				Duration.ofMillis(REFRESH_EXPIRATION_TIME)
		);
	}

	@Override
	public Optional<String> findRefreshJtiByUserId(Long userId) {
		String key = "RT:" + userId;
		String refreshJti = redisTemplate.opsForValue().get(key);
		return Optional.ofNullable(refreshJti);
	}

	@Override
	public void deleteRefreshJti(Long userId) {
		String key = "RT:" + userId;
		redisTemplate.delete(key);
	}

}
