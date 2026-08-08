package com.example.moodtail.domain.weather.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "kakao.local")
public record KakaoLocalProperties(
        String baseUrl,
        String restApiKey,
        long connectTimeoutMillis,
        long readTimeoutMillis
) {
}
