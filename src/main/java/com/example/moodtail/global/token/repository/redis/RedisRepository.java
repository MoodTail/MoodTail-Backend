package com.example.moodtail.global.token.repository.redis;

import io.jsonwebtoken.Claims;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RedisRepository {
	void save(Long userId, String refreshToken);

	Optional<Long> findUserIdByToken(String refreshToken);

	Boolean delete(String refreshToken);

	Boolean blockAccessToken(String accessToken, Claims claims);

	Boolean isJtiBlocked(String jti);

	void saveRefreshJti(Long userId, String refreshJti);

	Optional<String> findRefreshJtiByUserId(Long userId);

	void deleteRefreshJti(Long userId);

	void saveLastLogin(String email, LocalDateTime lastLogin);

	LocalDateTime getLastLogin(String email);

	// 사용자 입장(in) 시간 저장
	void saveUserInTime(Long userId, LocalDateTime inTime);

	// 사용자 입장 시간 조회
	Optional<LocalDateTime> getUserInTime(Long userId);

	// 사용자 입장 기록 삭제
	void deleteUserInTime(Long userId);

	void deleteUserTrigger(Long userId);

	void extendUserTimer(Long userId);

	// 현재 활동중인 사용자 ID 목록 조회 (usage:in:* 키가 있는 userId)
	List<Long> getAllActiveUserIds();
}
