package com.example.moodtail.domain.cocktail.service;

import com.example.moodtail.domain.cocktail.entity.Cocktail;
import com.example.moodtail.domain.cocktail.entity.Region;
import com.example.moodtail.domain.cocktail.model.DailyCocktailCacheValue;
import com.example.moodtail.domain.cocktail.repository.CocktailRepository;
import com.example.moodtail.domain.cocktail.repository.redis.DailyCocktailRedisRepository;
import com.example.moodtail.domain.recommendation.calculator.WeatherWeightCalculator;
import com.example.moodtail.domain.recommendation.model.CocktailRecommendationResult;
import com.example.moodtail.domain.recommendation.model.WeatherTasteVector;
import com.example.moodtail.domain.recommendation.service.DailyCocktailRecommender;
import com.example.moodtail.domain.weather.dto.response.WeatherResponse;
import com.example.moodtail.domain.weather.service.WeatherService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DailyCocktailGenerationService {
    private final CocktailRepository cocktailRepository;
    private final WeatherService weatherService;
    private final WeatherWeightCalculator weatherWeightCalculator;
    private final DailyCocktailRecommender dailyCocktailRecommender;
    private final DailyCocktailRedisRepository dailyCocktailRedisRepository;

    public DailyCocktailCacheValue generate(
            LocalDate recommendationDate,
            Region region
    ) {
        //날씨 조회
        WeatherResponse weatherResponse =
                weatherService.getCurrentWeather(
                        region.getRepresentativeLatitude(),
                        region.getRepresentativeLongitude()
                );

        // 추천 계산
        WeatherTasteVector weatherTasteVector =
                weatherWeightCalculator.calculate(
                        weatherResponse.weather(),
                        weatherResponse.temperature(),
                        weatherResponse.humidity(),
                        recommendationDate.getDayOfWeek()
                );

        List<Cocktail> candidates =
                cocktailRepository.findAllByOrderByIdAsc();

        // 칵테일 후보 중 하나 고르기
        CocktailRecommendationResult result =
                dailyCocktailRecommender.recommend(
                        weatherTasteVector,
                        candidates
                );

        DailyCocktailCacheValue cacheValue =
                DailyCocktailCacheValue.of(
                        recommendationDate,
                        result.cocktail().getId(),
                        weatherResponse,
                        result.similarity()
                );

        // redis 저장
        dailyCocktailRedisRepository.save(
                region,
                cacheValue
        );

        return cacheValue;
    }
}
