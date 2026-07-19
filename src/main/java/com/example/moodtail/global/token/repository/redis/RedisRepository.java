package com.example.moodtail.global.token.repository.redis;

import io.jsonwebtoken.Claims;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

public interface RedisRepository {
	void blockAccessToken(Claims claims);

	boolean isJtiBlocked(String jti);

	void saveRefreshJti(Long userId, String refreshJti);

	boolean saveRefreshJtiIfAbsent(Long userId, String refreshJti);

	boolean replaceRefreshJti(Long userId, String expectedRefreshJti, String newRefreshJti);

	boolean deleteRefreshJtiIfMatches(Long userId, String expectedRefreshJti);

	Optional<String> findRefreshJtiByUserId(Long userId);

	void deleteRefreshJti(Long userId);

	void saveOAuthState(String state, Long guestUserId, String provider, String codeVerifier, Duration ttl);

	Optional<OAuthStateSession> consumeOAuthStateSession(String state, String provider);

	boolean acquireOAuthStateSlot(Long guestUserId, String provider, int maxAttempts, Duration window);

	boolean acquireGuestLoginSlot(String fingerprint, int maxAttempts, Duration window);

	boolean acquirePasswordResetClientSlot(String fingerprint, int maxAttempts, Duration window);

	boolean acquirePasswordResetCooldown(String emailFingerprint, Duration ttl);

	void deletePasswordResetCooldown(String emailFingerprint);

	void savePasswordResetCode(
			String emailFingerprint,
			Long localAccountId,
			int passwordVersion,
			String codeDigest,
			Duration ttl
	);

	Optional<PasswordResetTokenSession> verifyPasswordResetCode(
			String emailFingerprint,
			String codeDigest,
			int maxAttempts
	);

	void deletePasswordResetCode(String emailFingerprint);

	void savePasswordResetToken(String token, PasswordResetTokenSession session, Duration ttl);

	Optional<PasswordResetTokenSession> findPasswordResetToken(String token);

	void deletePasswordResetToken(String token);

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

	record OAuthStateSession(Long guestUserId, String codeVerifier) {
	}

	record PasswordResetTokenSession(Long localAccountId, int passwordVersion) {
	}
}
