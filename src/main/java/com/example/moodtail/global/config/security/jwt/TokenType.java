package com.example.moodtail.global.config.security.jwt;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TokenType {
	ACCESS("액세스 토큰"), REFRESH("리프레쉬 토큰"),
	;
	final String value;
}
