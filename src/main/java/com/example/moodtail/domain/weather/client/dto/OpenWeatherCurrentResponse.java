package com.example.moodtail.domain.weather.client.dto;

import java.util.List;

public record OpenWeatherCurrentResponse(
        List<Weather> weather,
        Main main
) {
    public record Weather(
            String main
    ) {
    }

    public record Main(
            double temp,
            int humidity
    ) {
    }
}
