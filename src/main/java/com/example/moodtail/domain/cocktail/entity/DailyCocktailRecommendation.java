package com.example.moodtail.domain.cocktail.entity;

import com.example.moodtail.domain.weather.dto.response.WeatherResponse;
import com.example.moodtail.domain.weather.entity.WeatherCondition;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "daily_cocktail_recommendations",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_daily_cocktail_recommendation_date",
                columnNames = "recommendation_date"
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DailyCocktailRecommendation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(
            name = "recommendation_date",
            nullable = false,
            updatable = false
    )
    private LocalDate recommendationDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cocktail_id", nullable = false)
    private Cocktail cocktail;

    @Column(name = "temperature", nullable = false)
    private double temperature;

    @Column(name = "humidity", nullable = false)
    private int humidity;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "weather",
            nullable = false,
            length = 20
    )
    private WeatherCondition weather;

    @Column(name = "cosine_similarity", nullable = false)
    private double cosineSimilarity;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;

    private DailyCocktailRecommendation(
            LocalDate recommendationDate,
            Cocktail cocktail,
            WeatherResponse weatherResponse,
            double cosineSimilarity
    ) {
        this.recommendationDate = recommendationDate;
        this.cocktail = cocktail;
        this.temperature = weatherResponse.temperature();
        this.humidity = weatherResponse.humidity();
        this.weather = weatherResponse.weather();
        this.cosineSimilarity = cosineSimilarity;
    }

    public static DailyCocktailRecommendation create(
            LocalDate recommendationDate,
            Cocktail cocktail,
            WeatherResponse weatherResponse,
            double cosineSimilarity
    ) {
        return new DailyCocktailRecommendation(
                recommendationDate,
                cocktail,
                weatherResponse,
                cosineSimilarity
        );
    }

    @PrePersist
    private void prePersist() {
        createdAt = LocalDateTime.now();
    }
}
