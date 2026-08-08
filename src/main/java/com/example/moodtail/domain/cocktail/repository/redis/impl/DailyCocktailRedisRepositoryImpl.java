package com.example.moodtail.domain.cocktail.repository.redis.impl;

import com.example.moodtail.domain.cocktail.entity.Region;
import com.example.moodtail.domain.cocktail.model.DailyCocktailCacheValue;
import com.example.moodtail.domain.cocktail.repository.redis.DailyCocktailRedisRepository;
import com.example.moodtail.global.common.exception.RestApiException;
import com.example.moodtail.global.common.exception.code.status.DailyCocktailErrorStatus;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class DailyCocktailRedisRepositoryImpl implements DailyCocktailRedisRepository{
    private static final String KEY_PREFIX =
            "moodtail:local:daily-cocktail:";

    private static final Duration TTL =
            Duration.ofDays(2);

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public Optional<DailyCocktailCacheValue> find(
            LocalDate recommendationDate,
            Region region
    ) {
        try {
            String key = createKey(
                    recommendationDate,
                    region
            );

            String value =
                    redisTemplate.opsForValue().get(key);

            return deserialize(value);
        } catch (RestApiException exception) {
            throw exception;
        } catch (DataAccessException exception) {
            throw redisUnavailable();
        }
    }

    @Override
    public void save(
            Region region,
            DailyCocktailCacheValue value
    ) {
        String key = createKey(
                value.recommendationDate(),
                region
        );

        String serializedValue = serialize(value);

        try {
            redisTemplate.opsForValue().set(
                    key,
                    serializedValue,
                    TTL
            );
        } catch (DataAccessException exception) {
            throw redisUnavailable();
        }
    }

    @Override
    public void delete(
            LocalDate recommendationDate,
            Region region
    ) {
        try {
            redisTemplate.delete(
                    createKey(
                            recommendationDate,
                            region
                    )
            );
        } catch (DataAccessException exception) {
            throw redisUnavailable();
        }
    }

    private String createKey(
            LocalDate recommendationDate,
            Region region
    ) {
        return KEY_PREFIX
                + recommendationDate
                + ":"
                + region.getKey();
    }

    private String serialize(
            DailyCocktailCacheValue value
    ) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new RestApiException(
                    DailyCocktailErrorStatus.DAILY_COCKTAIL_CACHE_SERIALIZATION_ERROR
            );
        }
    }

    private Optional<DailyCocktailCacheValue> deserialize(
            String value
    ) {
        if (value == null) {
            return Optional.empty();
        }

        try {
            return Optional.of(
                    objectMapper.readValue(
                            value,
                            DailyCocktailCacheValue.class
                    )
            );
        } catch (JsonProcessingException exception) {
            throw new RestApiException(
                    DailyCocktailErrorStatus
                            .DAILY_COCKTAIL_CACHE_DESERIALIZATION_ERROR
            );
        }
    }

    private RestApiException redisUnavailable() {
        return new RestApiException(
                DailyCocktailErrorStatus
                        .DAILY_COCKTAIL_REDIS_UNAVAILABLE
        );
    }
}
