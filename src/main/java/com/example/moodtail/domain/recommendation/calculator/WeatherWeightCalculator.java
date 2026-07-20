package com.example.moodtail.domain.recommendation.calculator;

import com.example.moodtail.domain.recommendation.model.WeatherTasteVector;
import com.example.moodtail.domain.weather.entity.WeatherCondition;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;

@Component
public class WeatherWeightCalculator {
    public WeatherTasteVector calculate(
            WeatherCondition weather,
            double temperature,
            int humidity,
            DayOfWeek dayOfWeek
    ) {
        MutableWeights weights = new MutableWeights();

        applyWeather(weights, weather);
        applyTemperature(weights, temperature);
        applyDayOfWeek(weights, dayOfWeek);
        applyHumidity(weights, humidity);

        return new WeatherTasteVector(
                weights.alcoholIntensity,
                weights.sweetness,
                weights.sourness,
                weights.refreshing,
                weights.bitterness
        );
    }

    private void applyWeather(
            MutableWeights weights,
            WeatherCondition weather
    ) {
        switch (weather) {
            case CLEAR -> {
                weights.refreshing += 1.5;
                weights.sweetness += 1.0;
            }
            case CLOUDY -> {
                weights.bitterness += 1.5;
                weights.alcoholIntensity += 1.0;
            }
            case RAIN -> {
                weights.alcoholIntensity += 2.0;
                weights.refreshing -= 1.0;
            }
            case SNOW -> {
                weights.sweetness += 1.5;
                weights.alcoholIntensity += 1.5;
            }
        }
    }

    private void applyTemperature(
            MutableWeights weights,
            double temperature
    ) {
        if (temperature >= 30.0) {
            weights.refreshing += 2.0;
        } else if (temperature >= 20.0) {
            weights.refreshing += 1.0;
        } else if (temperature >= 10.0) {
            // 변화 없음
        } else if (temperature >= 0.0) {
            weights.alcoholIntensity += 1.0;
        } else {
            weights.alcoholIntensity += 2.0;
            weights.sweetness += 1.0;
        }
    }

    private void applyDayOfWeek(
            MutableWeights weights,
            DayOfWeek dayOfWeek
    ) {
        switch (dayOfWeek) {
            case FRIDAY, SATURDAY ->
                    weights.alcoholIntensity += 1.5;

            case SUNDAY -> {
                weights.alcoholIntensity -= 0.5;
                weights.refreshing += 0.5;
            }

            default -> {
                // 월요일-목요일 계산 X
            }
        }
    }

    private void applyHumidity(
            MutableWeights weights,
            int humidity
    ) {
        if (humidity >= 80) {
            weights.bitterness += 1.5;
            weights.sourness += 1.0;
        } else if (humidity >= 60) {
            weights.bitterness += 1.0;
            weights.sourness += 0.5;
        }
    }

    private static class MutableWeights {
        private double alcoholIntensity;
        private double sweetness;
        private double sourness;
        private double refreshing;
        private double bitterness;
    }
}
