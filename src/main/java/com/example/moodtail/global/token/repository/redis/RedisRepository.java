package com.example.moodtail.global.token.repository.redis;

import java.time.Duration;
import java.util.Optional;

public interface RedisRepository {
	void saveRefreshJti(Long userId, String refreshJti);

	boolean replaceRefreshJti(Long userId, String expectedRefreshJti, String newRefreshJti);

	Optional<String> findRefreshJtiByUserId(Long userId);

	void deleteRefreshJti(Long userId);

	void saveOAuthState(
			String state,
			String provider,
			String codeVerifier,
			Duration ttl
	);

	Optional<OAuthStateSession> consumeOAuthStateSession(String state, String provider);

	boolean acquireOAuthStateSlot(String ownerKey, String provider, int maxAttempts, Duration window);

	void saveSocialSignupToken(String token, SocialSignupSession session, Duration ttl);

	Optional<SocialSignupSession> consumeSocialSignupToken(String token);

	boolean acquireGuestLoginSlot(String fingerprint, int maxAttempts, Duration window);

	boolean acquireLocalAuthSlot(String purpose, String fingerprint, int maxAttempts, Duration window);

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

	record OAuthStateSession(String codeVerifier) {
	}

	record SocialSignupSession(
			String provider,
			String providerUserId,
			String email
	) {
	}

	record PasswordResetTokenSession(Long localAccountId, int passwordVersion) {
	}
}
