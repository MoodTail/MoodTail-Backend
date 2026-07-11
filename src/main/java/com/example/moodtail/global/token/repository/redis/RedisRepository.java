package com.example.moodtail.global.token.repository.redis;

import io.jsonwebtoken.Claims;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

public interface RedisRepository {
	void save(Long userId, String refreshToken);

	Optional<Long> findUserIdByToken(String refreshToken);

	Boolean delete(String refreshToken);

	Boolean blockAccessToken(String accessToken, Claims claims);

	Boolean isJtiBlocked(String jti);

	void saveRefreshJti(Long userId, String refreshJti);

	boolean replaceRefreshJti(Long userId, String expectedRefreshJti, String newRefreshJti);

	Optional<String> findRefreshJtiByUserId(Long userId);

	void deleteRefreshJti(Long userId);

	void saveOAuthState(String state, Long guestUserId, String provider, String codeVerifier, Duration ttl);

	Optional<OAuthStateSession> consumeOAuthStateSession(String state, String provider);

	default void saveOAuthState(String state, Long guestUserId, String provider, Duration ttl) {
		saveOAuthState(state, guestUserId, provider, "", ttl);
	}

	default Optional<Long> consumeOAuthState(String state, String provider) {
		return consumeOAuthStateSession(state, provider).map(OAuthStateSession::guestUserId);
	}

	boolean acquireOAuthStateSlot(Long guestUserId, String provider, int maxAttempts, Duration window);

	boolean acquireGuestLoginSlot(String fingerprint, int maxAttempts, Duration window);

	boolean acquirePasswordResetClientSlot(String fingerprint, int maxAttempts, Duration window);

	boolean acquirePasswordResetCooldown(String emailFingerprint, Duration ttl);

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

	Optional<PasswordResetTokenSession> consumePasswordResetToken(String token);

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

	record OAuthStateSession(Long guestUserId, String codeVerifier) {
	}

	record PasswordResetTokenSession(Long localAccountId, int passwordVersion) {
	}
}
