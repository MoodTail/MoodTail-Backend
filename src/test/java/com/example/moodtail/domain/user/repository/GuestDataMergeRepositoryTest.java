package com.example.moodtail.domain.user.repository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GuestDataMergeRepositoryTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Test
    void executesEverySelectiveMergeStepAndReturnsAffectedCounts() {
        when(jdbcTemplate.update(anyString(), any(Object[].class)))
                .thenReturn(2, 3, 4, 5, 6, 7, 8);
        GuestDataMergeRepository repository = new GuestDataMergeRepository(jdbcTemplate);

        GuestDataMergeRepository.MergeResult result = repository.merge(7L, 11L);

        assertThat(result.transferredMoodTestResults()).isEqualTo(2);
        assertThat(result.transferredRecommendationSessions()).isEqualTo(3);
        assertThat(result.transferredDrinkingRecords()).isEqualTo(4);
        assertThat(result.transferredInquiries()).isEqualTo(5);
        assertThat(result.mergedCocktailFavorites()).isEqualTo(6);
        assertThat(result.mergedMoodTypes()).isEqualTo(8);
        verify(jdbcTemplate, times(8)).update(anyString(), any(Object[].class));
    }
}
