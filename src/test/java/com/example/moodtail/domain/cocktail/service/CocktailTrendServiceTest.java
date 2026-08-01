package com.example.moodtail.domain.cocktail.service;

import com.example.moodtail.domain.cocktail.dto.response.CocktailTrendResponse;
import com.example.moodtail.domain.cocktail.entity.CocktailTrendSnapshot;
import com.example.moodtail.domain.cocktail.repository.CocktailTrendSnapshotRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CocktailTrendServiceTest {

    @Mock
    private CocktailTrendSnapshotRepository cocktailTrendSnapshotRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private CocktailTrendService cocktailTrendService;

    @BeforeEach
    void setUp() {
        cocktailTrendService = new CocktailTrendService(cocktailTrendSnapshotRepository, objectMapper);
    }

    @Test
    void returnsSnapshotDataFromLatestSnapshotWithoutRecalculating() throws Exception {
        CocktailTrendResponse snapshotData = new CocktailTrendResponse(
                List.of(),
                new CocktailTrendResponse.TasteProfile(
                        BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE
                ),
                new CocktailTrendResponse.DisplayTasteScores(20, 20, 20, 20, 20),
                List.of(),
                List.of()
        );
        CocktailTrendSnapshot snapshot = CocktailTrendSnapshot.create(
                objectMapper.writeValueAsString(snapshotData), LocalDateTime.now()
        );
        when(cocktailTrendSnapshotRepository.findTopByOrderByIdDesc()).thenReturn(Optional.of(snapshot));

        CocktailTrendResponse response = cocktailTrendService.getTrend();

        assertThat(response.averageTasteProfile().alcoholIntensity()).isEqualByComparingTo(BigDecimal.ONE);
        assertThat(response.displayAverageTasteScores().sweetness()).isEqualTo(20);
    }

    @Test
    void throwsWhenSnapshotDoesNotExistYet() {
        when(cocktailTrendSnapshotRepository.findTopByOrderByIdDesc()).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cocktailTrendService.getTrend())
                .isInstanceOf(IllegalStateException.class);
    }
}
