package com.example.moodtail.domain.cocktail.service;

import com.example.moodtail.domain.cocktail.dto.response.DailyCocktailResponse;
import com.example.moodtail.domain.cocktail.entity.Cocktail;
import com.example.moodtail.domain.cocktail.entity.DailyCocktailRecommendation;
import com.example.moodtail.domain.cocktail.repository.CocktailRepository;
import com.example.moodtail.domain.cocktail.repository.DailyCocktailRecommendationRepository;
import com.example.moodtail.domain.recommendation.calculator.WeatherWeightCalculator;
import com.example.moodtail.domain.recommendation.model.CocktailRecommendationResult;
import com.example.moodtail.domain.recommendation.model.WeatherTasteVector;
import com.example.moodtail.domain.recommendation.service.DailyCocktailRecommender;
import com.example.moodtail.domain.weather.dto.response.WeatherResponse;
import com.example.moodtail.domain.weather.service.WeatherService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DailyCocktailService {
    private static final ZoneId SEOUL_ZONE_ID =
            ZoneId.of("Asia/Seoul");

    private static final double SEOUL_LATITUDE = 37.5665;
    private static final double SEOUL_LONGITUDE = 126.9780;

    private final DailyCocktailRecommendationRepository dailyRepository;
    private final DailyCocktailPersistenceService persistenceService;
    private final CocktailRepository cocktailRepository;
    private final WeatherService weatherService;
    private final WeatherWeightCalculator weatherWeightCalculator;
    private final DailyCocktailRecommender dailyCocktailRecommender;

    public DailyCocktailResponse getOrCreateTodayCocktail(
            double latitude,
            double longitude
    ) {
        LocalDate today = LocalDate.now(SEOUL_ZONE_ID);

        return dailyRepository.findByRecommendationDate(today)
                .map(recommendation -> DailyCocktailResponse.from(recommendation, false))
                .orElseGet(() -> createTodayCocktail(today));
    }

    private DailyCocktailResponse createTodayCocktail(LocalDate today) {
        WeatherResponse weatherResponse =
                weatherService.getCurrentWeather(
                        SEOUL_LATITUDE,
                        SEOUL_LONGITUDE
                );

        WeatherTasteVector weatherTasteVector =
                weatherWeightCalculator.calculate(
                        weatherResponse.weather(),
                        weatherResponse.temperature(),
                        weatherResponse.humidity(),
                        today.getDayOfWeek()
                );

        List<Cocktail> candidates =
                cocktailRepository.findAllByOrderByIdAsc();

        CocktailRecommendationResult result =
                dailyCocktailRecommender.recommend(
                        weatherTasteVector,
                        candidates
                );

        DailyCocktailRecommendation recommendation =
                DailyCocktailRecommendation.create(
                        today,
                        result.cocktail(),
                        weatherResponse,
                        result.similarity()
                );

        DailyCocktailRecommendation saved =
                persistenceService.save(recommendation);

        return DailyCocktailResponse.from(saved, true);

    }
}
