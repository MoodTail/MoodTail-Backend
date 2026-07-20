package com.example.moodtail.domain.weather.client;

import com.example.moodtail.domain.weather.client.dto.OpenWeatherCurrentResponse;
import com.example.moodtail.domain.weather.config.OpenWeatherProperties;
import com.example.moodtail.global.common.exception.RestApiException;
import com.example.moodtail.global.common.exception.code.status.WeatherErrorStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.ResourceAccessException;

@Component
public class OpenWeatherClient {
    private final RestClient restClient;
    private final String apiKey;

    public OpenWeatherClient(
            RestClient.Builder restClientBuilder,
            OpenWeatherProperties properties
    ) {
        if (!StringUtils.hasText(properties.baseUrl()) || !StringUtils.hasText(properties.apiKey())) {
            throw new RestApiException(WeatherErrorStatus.WEATHER_CONFIGURATION_ERROR);
        }

        this.restClient = restClientBuilder
                .baseUrl(properties.baseUrl())
                .build();

        this.apiKey = properties.apiKey();
    }

    public OpenWeatherCurrentResponse getCurrentWeather(
            double latitude,
            double longitude
    ) {
        try {
            OpenWeatherCurrentResponse response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/data/2.5/weather")
                            .queryParam("lat", latitude)
                            .queryParam("lon", longitude)
                            .queryParam("appid", apiKey)
                            .queryParam("units", "metric")
                            .build())
                    .retrieve()
                    .body(OpenWeatherCurrentResponse.class);

            if (response == null) {
                throw new RestApiException(WeatherErrorStatus.INVALID_WEATHER_RESPONSE);
            }
            return response;
        } catch (RestApiException e) {
            throw e;
        } catch (RestClientResponseException e) {
            throw mapResponseException(e);
        } catch (ResourceAccessException e) {
            throw new RestApiException(WeatherErrorStatus.WEATHER_PROVIDER_UNAVAILABLE);
        } catch (IllegalArgumentException e) {
            throw new RestApiException(WeatherErrorStatus.WEATHER_CONFIGURATION_ERROR);
        } catch (RestClientException e) {
            throw new RestApiException(WeatherErrorStatus.INVALID_WEATHER_RESPONSE);
        }
    }

    private RestApiException mapResponseException(RestClientResponseException e) {
        int status = e.getStatusCode().value();

        if (status == 401 || status == 403) {
            return new RestApiException(WeatherErrorStatus.WEATHER_CONFIGURATION_ERROR);
        }
        if (status == 429) {
            return new RestApiException(WeatherErrorStatus.WEATHER_RATE_LIMIT_EXCEEDED);
        }
        if (status >= 500) {
            return new RestApiException(WeatherErrorStatus.WEATHER_PROVIDER_UNAVAILABLE);
        }
        return new RestApiException(WeatherErrorStatus.INVALID_WEATHER_RESPONSE);
    }
}
