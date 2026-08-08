package com.example.moodtail.domain.cocktail.service;

import com.example.moodtail.domain.cocktail.dto.response.DailyCocktailResponse;
import com.example.moodtail.domain.cocktail.entity.Cocktail;
import com.example.moodtail.domain.cocktail.entity.Region;
import com.example.moodtail.domain.cocktail.model.DailyCocktailCacheValue;
import com.example.moodtail.domain.cocktail.repository.CocktailRepository;
import com.example.moodtail.domain.cocktail.repository.redis.DailyCocktailRedisRepository;
import com.example.moodtail.domain.weather.service.ReverseGeocodingService;
import com.example.moodtail.global.common.exception.RestApiException;
import com.example.moodtail.global.common.exception.code.status.CocktailErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DailyCocktailService {
    private static final ZoneId SEOUL_ZONE_ID =
            ZoneId.of("Asia/Seoul");

    private final ReverseGeocodingService reverseGeocodingService;
    private final DailyCocktailRedisRepository dailyCocktailRedisRepository;
    private final DailyCocktailGenerationService generationService;
    private final CocktailRepository cocktailRepository;

    public DailyCocktailResponse getOrCreateTodayCocktail(
            Double latitude,
            Double longitude
    ) {
        LocalDate today = LocalDate.now(SEOUL_ZONE_ID);

        Region region;
        if (latitude == null || longitude == null) {
            region = Region.SEOUL;
        } else{
            region = reverseGeocodingService.resolve(
                    latitude,
                    longitude
            );
        }

        Optional<DailyCocktailCacheValue> cached =
                dailyCocktailRedisRepository.find(
                        today,
                        region
                );

        if (cached.isPresent()) {
            return createResponse(
                    cached.get(),
                    false
            );
        }

        DailyCocktailCacheValue created =
                generationService.generate(
                        today,
                        region
                );

        return createResponse(
                created,
                true
        );
    }

    private DailyCocktailResponse createResponse(
            DailyCocktailCacheValue cacheValue,
            boolean recommendationSaved
    ) {
        Cocktail cocktail =
                cocktailRepository.findByIdWithImage(
                        cacheValue.cocktailId()
                ).orElseThrow(() ->
                        new RestApiException(CocktailErrorStatus.COCKTAIL_NOT_FOUND)
                );

        return DailyCocktailResponse.from(
                cacheValue,
                cocktail,
                recommendationSaved
        );
    }
}
