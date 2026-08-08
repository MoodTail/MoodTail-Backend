package com.example.moodtail.domain.cocktail.repository.redis.impl;

import com.example.moodtail.domain.cocktail.entity.Region;
import com.example.moodtail.domain.cocktail.model.DailyCocktailCacheValue;
import com.example.moodtail.domain.weather.entity.WeatherCondition;
import com.example.moodtail.global.common.exception.RestApiException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DailyCocktailRedisRepositoryImplTest {

    private static final LocalDate RECOMMENDATION_DATE =
            LocalDate.of(2026, 7, 28);

    private static final String SEOUL_KEY =
            "moodtail:local:daily-cocktail:"
                    + "2026-07-28:seoul";

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private ObjectMapper objectMapper;
    private DailyCocktailRedisRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(
                        SerializationFeature
                                .WRITE_DATES_AS_TIMESTAMPS
                );

        repository =
                new DailyCocktailRedisRepositoryImpl(
                        redisTemplate,
                        objectMapper
                );

        lenient()
                .when(redisTemplate.opsForValue())
                .thenReturn(valueOperations);
    }

    @Test
    void savesDailyCocktailWithRegionKeyAndTtl()
            throws Exception {
        DailyCocktailCacheValue cacheValue =
                createCacheValue();

        String expectedJson =
                objectMapper.writeValueAsString(cacheValue);

        repository.save(
                Region.SEOUL,
                cacheValue
        );

        verify(valueOperations).set(
                SEOUL_KEY,
                expectedJson,
                Duration.ofDays(2)
        );
    }

    @Test
    void findsAndDeserializesDailyCocktail()
            throws Exception {
        DailyCocktailCacheValue expected =
                createCacheValue();

        String json =
                objectMapper.writeValueAsString(expected);

        when(valueOperations.get(SEOUL_KEY))
                .thenReturn(json);

        Optional<DailyCocktailCacheValue> result =
                repository.find(
                        RECOMMENDATION_DATE,
                        Region.SEOUL
                );

        assertThat(result).contains(expected);

        verify(valueOperations).get(SEOUL_KEY);
    }

    @Test
    void returnsEmptyWhenDailyCocktailDoesNotExist() {
        when(valueOperations.get(SEOUL_KEY))
                .thenReturn(null);

        Optional<DailyCocktailCacheValue> result =
                repository.find(
                        RECOMMENDATION_DATE,
                        Region.SEOUL
                );

        assertThat(result).isEmpty();
    }

    @Test
    void deletesDailyCocktailUsingDateAndRegion() {
        repository.delete(
                RECOMMENDATION_DATE,
                Region.SEOUL
        );

        verify(redisTemplate).delete(SEOUL_KEY);
    }

    @Test
    void mapsRedisReadFailureToServiceUnavailable() {
        when(valueOperations.get(SEOUL_KEY))
                .thenThrow(
                        new DataAccessResourceFailureException(
                                "Redis unavailable"
                        )
                );

        assertThatThrownBy(() ->
                repository.find(
                        RECOMMENDATION_DATE,
                        Region.SEOUL
                )
        ).isInstanceOfSatisfying(
                RestApiException.class,
                exception -> assertThat(
                        exception.getErrorCode().getCode()
                ).isEqualTo("DAILY_COCKTAIL503")
        );
    }

    @Test
    void mapsRedisSaveFailureToServiceUnavailable() {
        DailyCocktailCacheValue cacheValue =
                createCacheValue();

        doThrow(
                new DataAccessResourceFailureException(
                        "Redis unavailable"
                )
        ).when(valueOperations).set(
                anyString(),
                anyString(),
                any(Duration.class)
        );

        assertThatThrownBy(() ->
                repository.save(
                        Region.SEOUL,
                        cacheValue
                )
        ).isInstanceOfSatisfying(
                RestApiException.class,
                exception -> assertThat(
                        exception.getErrorCode().getCode()
                ).isEqualTo("DAILY_COCKTAIL503")
        );
    }

    @Test
    void mapsRedisDeleteFailureToServiceUnavailable() {
        when(redisTemplate.delete(SEOUL_KEY))
                .thenThrow(
                        new DataAccessResourceFailureException(
                                "Redis unavailable"
                        )
                );

        assertThatThrownBy(() ->
                repository.delete(
                        RECOMMENDATION_DATE,
                        Region.SEOUL
                )
        ).isInstanceOfSatisfying(
                RestApiException.class,
                exception -> assertThat(
                        exception.getErrorCode().getCode()
                ).isEqualTo("DAILY_COCKTAIL503")
        );
    }

    @Test
    void mapsSerializationFailureToInternalServerError()
            throws Exception {
        ObjectMapper failingObjectMapper =
                mock(ObjectMapper.class);

        when(failingObjectMapper.writeValueAsString(any()))
                .thenThrow(
                        new JsonProcessingException(
                                "Serialization failed"
                        ) {
                        }
                );

        DailyCocktailRedisRepositoryImpl failingRepository =
                new DailyCocktailRedisRepositoryImpl(
                        redisTemplate,
                        failingObjectMapper
                );

        assertThatThrownBy(() ->
                failingRepository.save(
                        Region.SEOUL,
                        createCacheValue()
                )
        ).isInstanceOfSatisfying(
                RestApiException.class,
                exception -> assertThat(
                        exception.getErrorCode().getCode()
                ).isEqualTo("DAILY_COCKTAIL500")
        );
    }

    @Test
    void mapsDeserializationFailureToInternalServerError()
            throws Exception {
        ObjectMapper failingObjectMapper =
                mock(ObjectMapper.class);

        when(failingObjectMapper.readValue(
                anyString(),
                eq(DailyCocktailCacheValue.class)
        )).thenThrow(
                new JsonProcessingException(
                        "Deserialization failed"
                ) {
                }
        );

        when(valueOperations.get(SEOUL_KEY))
                .thenReturn("invalid-json");

        DailyCocktailRedisRepositoryImpl failingRepository =
                new DailyCocktailRedisRepositoryImpl(
                        redisTemplate,
                        failingObjectMapper
                );

        assertThatThrownBy(() ->
                failingRepository.find(
                        RECOMMENDATION_DATE,
                        Region.SEOUL
                )
        ).isInstanceOfSatisfying(
                RestApiException.class,
                exception -> assertThat(
                        exception.getErrorCode().getCode()
                ).isEqualTo("DAILY_COCKTAIL500_1")
        );
    }

    private DailyCocktailCacheValue createCacheValue() {
        return new DailyCocktailCacheValue(
                RECOMMENDATION_DATE,
                10L,
                28.5,
                70,
                WeatherCondition.CLEAR,
                0.92
        );
    }
}