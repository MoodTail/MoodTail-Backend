package com.example.moodtail.domain.weather.entity;

public enum WeatherCondition {
    CLEAR,
    CLOUDY,
    RAIN,
    SNOW;

    public static WeatherCondition from(String openWeatherMain) {
        return switch (openWeatherMain) {
            case "Clear" -> CLEAR;
            case "Snow" -> SNOW;
            case "Rain", "Drizzle", "Thunderstorm" -> RAIN;
            case "Clouds", "Mist", "Fog", "Haze",
                 "Smoke", "Dust", "Sand", "Ash",
                 "Squall", "Tornado" -> CLOUDY;
            default -> CLOUDY;
        };
    }
}
