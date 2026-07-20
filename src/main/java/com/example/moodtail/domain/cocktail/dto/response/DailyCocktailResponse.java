package com.example.moodtail.domain.cocktail.dto.response;

import com.example.moodtail.domain.cocktail.entity.Cocktail;
import com.example.moodtail.domain.cocktail.entity.DailyCocktailRecommendation;
import com.example.moodtail.domain.weather.entity.WeatherCondition;
import lombok.Builder;

import java.time.DayOfWeek;

@Builder
public record DailyCocktailResponse(
        boolean recommendationSaved,
        ContextDto context,
        CocktailDto cocktail
) {

    public static DailyCocktailResponse from(
            DailyCocktailRecommendation recommendation,
            boolean recommendationSaved
    ) {
        return DailyCocktailResponse.builder()
                .recommendationSaved(recommendationSaved)
                .context(ContextDto.from(recommendation))
                .cocktail(CocktailDto.from(recommendation))
                .build();
    }

    @Builder
    public record ContextDto(
            double temperature,
            int humidity,
            WeatherCondition weather,
            String day
    ) {

        public static ContextDto from(
                DailyCocktailRecommendation recommendation
        ) {
            return ContextDto.builder()
                    .temperature(recommendation.getTemperature())
                    .humidity(recommendation.getHumidity())
                    .weather(recommendation.getWeather())
                    .day(toKoreanDay(
                            recommendation
                                    .getRecommendationDate()
                                    .getDayOfWeek()
                    ))
                    .build();
        }

        private static String toKoreanDay(DayOfWeek dayOfWeek) {
            return switch (dayOfWeek) {
                case MONDAY -> "월요일";
                case TUESDAY -> "화요일";
                case WEDNESDAY -> "수요일";
                case THURSDAY -> "목요일";
                case FRIDAY -> "금요일";
                case SATURDAY -> "토요일";
                case SUNDAY -> "일요일";
            };
        }
    }

    @Builder
    public record CocktailDto(
            Long cocktailId,
            String nameKo,
            String nameEn,
            String shortDescription,
            String imageUrl,
            int matchScore
    ) {

        public static CocktailDto from(
                DailyCocktailRecommendation recommendation
        ) {
            Cocktail cocktail = recommendation.getCocktail();

            return CocktailDto.builder()
                    .cocktailId(cocktail.getId())
                    .nameKo(cocktail.getNameKo())
                    .nameEn(cocktail.getNameEn())
                    .shortDescription(
                            cocktail.getShortDescription()
                    )
                    .imageUrl(
                            cocktail.getImage() == null
                                    ? null
                                    : cocktail.getImage().getImageUrl()
                    )
                    .matchScore(toMatchScore(
                            recommendation.getCosineSimilarity()
                    ))
                    .build();
        }

        private static int toMatchScore(double cosineSimilarity) {
            int score = (int) Math.round(
                    cosineSimilarity * 100.0
            );

            return Math.max(0, Math.min(100, score));
        }
    }
}

