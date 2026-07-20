package com.example.moodtail.domain.weather.dto.response;

import com.example.moodtail.domain.weather.entity.WeatherCondition;

public record WeatherResponse(
        double temperature,
        int humidity,
        WeatherCondition weather
) {
}
