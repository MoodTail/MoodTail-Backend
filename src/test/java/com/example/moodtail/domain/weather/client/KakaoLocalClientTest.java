package com.example.moodtail.domain.weather.client;

import com.example.moodtail.domain.weather.config.KakaoLocalProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class KakaoLocalClientTest {
    private KakaoLocalClient kakaoLocalClient;

    @BeforeEach
    void setUp() {
        kakaoLocalClient = new KakaoLocalClient(
                RestClient.builder(),
                new KakaoLocalProperties(
                        "https://dapi.kakao.com",
                        "test-api-key",
                        1_000,
                        1_000
                ),
                new ObjectMapper()
        );
    }

    @Test
    void mapsKakaoRateLimitCodeInBadRequestResponse() {
        HttpClientErrorException exception = badRequest(
                "{\"code\":-10,\"msg\":\"rate limit exceeded\"}"
        );

        assertThat(kakaoLocalClient.mapResponseException(exception)
                .getErrorCode().getCode())
                .isEqualTo("REGION503_1");
    }

    @Test
    void keepsOtherBadRequestResponsesAsInvalidRegionResponse() {
        HttpClientErrorException exception = badRequest(
                "{\"code\":-2,\"msg\":\"invalid request\"}"
        );

        assertThat(kakaoLocalClient.mapResponseException(exception)
                .getErrorCode().getCode())
                .isEqualTo("REGION502");
    }

    private HttpClientErrorException badRequest(String responseBody) {
        return HttpClientErrorException.create(
                HttpStatus.BAD_REQUEST,
                "Bad Request",
                HttpHeaders.EMPTY,
                responseBody.getBytes(StandardCharsets.UTF_8),
                StandardCharsets.UTF_8
        );
    }
}
