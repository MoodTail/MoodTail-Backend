package com.example.moodtail.domain.weather.controller;

import com.example.moodtail.domain.weather.dto.request.WeatherRequest;
import com.example.moodtail.domain.weather.dto.response.WeatherResponse;
import com.example.moodtail.domain.weather.service.WeatherService;
import com.example.moodtail.global.common.base.BaseEntity;
import com.example.moodtail.global.common.base.BaseResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/weather")
public class WeatherController {

    private final WeatherService weatherService;

    @GetMapping("/current")
    public BaseResponse<WeatherResponse> getCurrentWeather(
            @Valid @ModelAttribute WeatherRequest request
    ) {
        WeatherResponse response =
                weatherService.getCurrentWeather(
                        request.latitude(),
                        request.longitude()
                );

        return BaseResponse.onSuccess(response);
    }
}
