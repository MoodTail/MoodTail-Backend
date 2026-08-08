package com.example.moodtail.domain.weather.client;

import com.example.moodtail.domain.weather.client.dto.KakaoRegionResponse;
import com.example.moodtail.domain.weather.config.KakaoLocalProperties;
import com.example.moodtail.global.common.exception.RestApiException;
import com.example.moodtail.global.common.exception.code.status.RegionErrorStatus;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.io.IOException;
import java.time.Duration;

@Slf4j
@Component
public class KakaoLocalClient {
    private static final int KAKAO_RATE_LIMIT_ERROR_CODE = -10;

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public KakaoLocalClient(
            RestClient.Builder restClientBuilder,
            KakaoLocalProperties properties,
            ObjectMapper objectMapper
    ) {
        validateProperties(properties);
        this.objectMapper = objectMapper;

        SimpleClientHttpRequestFactory requestFactory =
                new SimpleClientHttpRequestFactory();

        requestFactory.setConnectTimeout(
                Duration.ofMillis(
                        properties.connectTimeoutMillis()
                )
        );

        requestFactory.setReadTimeout(
                Duration.ofMillis(
                        properties.readTimeoutMillis()
                )
        );

        this.restClient = restClientBuilder
                .baseUrl(properties.baseUrl())
                .requestFactory(requestFactory)
                .defaultHeader(
                        HttpHeaders.AUTHORIZATION,
                        "KakaoAK " + properties.restApiKey()
                )
                .defaultHeader(
                        HttpHeaders.ACCEPT,
                        MediaType.APPLICATION_JSON_VALUE
                )
                .build();
    }

    public KakaoRegionResponse getRegion(
            double latitude,
            double longitude
    ) {
        try {
            KakaoRegionResponse response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(
                                    "/v2/local/geo/"
                                            + "coord2regioncode.json"
                            )
                            .queryParam("x", longitude)
                            .queryParam("y", latitude)
                            .queryParam("input_coord", "WGS84")
                            .build()
                    )
                    .retrieve()
                    .body(KakaoRegionResponse.class);

            if (response == null) {
                throw new RestApiException(
                        RegionErrorStatus.INVALID_REGION_RESPONSE
                );
            }

            return response;
        } catch (RestApiException exception) {
            throw exception;
        } catch (RestClientResponseException exception) {
            throw mapResponseException(exception);
        } catch (ResourceAccessException exception) {
            throw new RestApiException(
                    RegionErrorStatus.REGION_PROVIDER_UNAVAILABLE
            );
        } catch (IllegalArgumentException exception) {
            throw new RestApiException(
                    RegionErrorStatus.REGION_CONFIGURATION_ERROR
            );
        } catch (RestClientException exception) {
            throw new RestApiException(
                    RegionErrorStatus.INVALID_REGION_RESPONSE
            );
        }
    }

    private void validateProperties(
            KakaoLocalProperties properties
    ) {
        if (properties == null
                || !StringUtils.hasText(properties.baseUrl())
                || !StringUtils.hasText(properties.restApiKey())
                || properties.connectTimeoutMillis() <= 0
                || properties.readTimeoutMillis() <= 0) {
            throw new RestApiException(
                    RegionErrorStatus.REGION_CONFIGURATION_ERROR
            );
        }
    }

    RestApiException mapResponseException(
            RestClientResponseException exception
    ) {
        log.error(
                "Kakao Local API failed: status={}, body={}",
                exception.getStatusCode(),
                exception.getResponseBodyAsString()
        );

        int status = exception.getStatusCode().value();

        if (status == 401 || status == 403) {
            return new RestApiException(
                    RegionErrorStatus.REGION_CONFIGURATION_ERROR
            );
        }

        if (status == 429 || isKakaoRateLimitError(exception, status)) {
            return new RestApiException(
                    RegionErrorStatus.REGION_RATE_LIMIT_EXCEEDED
            );
        }

        if (status >= 500) {
            return new RestApiException(
                    RegionErrorStatus.REGION_PROVIDER_UNAVAILABLE
            );
        }

        return new RestApiException(
                RegionErrorStatus.INVALID_REGION_RESPONSE
        );
    }

    private boolean isKakaoRateLimitError(
            RestClientResponseException exception,
            int status
    ) {
        if (status != 400) {
            return false;
        }

        try {
            JsonNode errorResponse = objectMapper.readTree(
                    exception.getResponseBodyAsByteArray()
            );
            JsonNode errorCode = errorResponse == null
                    ? null
                    : errorResponse.get("code");
            return errorCode != null
                    && errorCode.canConvertToInt()
                    && errorCode.intValue() == KAKAO_RATE_LIMIT_ERROR_CODE;
        } catch (IOException parsingException) {
            log.warn(
                    "Failed to parse Kakao Local API error response",
                    parsingException
            );
            return false;
        }
    }

}
