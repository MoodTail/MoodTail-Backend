package com.example.moodtail.domain.weather.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "open-weather")
public record OpenWeatherProperties(
    String baseUrl,
    String apiKey,
    long connectTimeoutMillis,
    long readTimeoutMillis
) {
}
