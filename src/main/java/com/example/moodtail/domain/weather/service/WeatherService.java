package com.example.moodtail.domain.weather.service;

import com.example.moodtail.domain.weather.client.OpenWeatherClient;
import com.example.moodtail.domain.weather.client.dto.OpenWeatherCurrentResponse;
import com.example.moodtail.domain.weather.dto.response.WeatherResponse;
import com.example.moodtail.domain.weather.entity.WeatherCondition;
import com.example.moodtail.global.common.exception.RestApiException;
import com.example.moodtail.global.common.exception.code.status.WeatherErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WeatherService {

    private final OpenWeatherClient openWeatherClient;

    public WeatherResponse getCurrentWeather(
            Double latitude,
            Double longitude
    ) {
        OpenWeatherCurrentResponse response =
                openWeatherClient.getCurrentWeather(latitude, longitude);

        if (response.main() == null
                || response.weather() == null
                || response.weather().isEmpty()
                || response.weather().get(0) == null
                || response.weather().get(0).main() == null) {
            throw new RestApiException(WeatherErrorStatus.INVALID_WEATHER_RESPONSE);
        }

        String openWeatherMain =
                response.weather().get(0).main();

        WeatherCondition weatherCondition =
                WeatherCondition.from(openWeatherMain);

        return new WeatherResponse(
                response.main().temp(),
                response.main().humidity(),
                weatherCondition
        );
    }
}
