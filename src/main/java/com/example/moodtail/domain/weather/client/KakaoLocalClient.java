package com.example.moodtail.domain.weather.client;

import com.example.moodtail.domain.weather.client.dto.KakaoRegionResponse;
import com.example.moodtail.domain.weather.config.KakaoLocalProperties;
import com.example.moodtail.global.common.exception.RestApiException;
import com.example.moodtail.global.common.exception.code.status.RegionErrorStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.time.Duration;

@Component
public class KakaoLocalClient {
    private final RestClient restClient;

    public KakaoLocalClient(
            RestClient.Builder restClientBuilder,
            KakaoLocalProperties properties
    ) {
        validateProperties(properties);

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

    private RestApiException mapResponseException(
            RestClientResponseException exception
    ) {
        int status = exception.getStatusCode().value();

        if (status == 401 || status == 403) {
            return new RestApiException(
                    RegionErrorStatus.REGION_CONFIGURATION_ERROR
            );
        }

        if (status == 429) {
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
}
