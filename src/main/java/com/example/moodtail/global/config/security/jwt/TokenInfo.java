package com.example.moodtail.global.config.security.jwt;

import lombok.Builder;

@Builder
public record TokenInfo(
	String accessToken,
	String refreshToken
) {

}
