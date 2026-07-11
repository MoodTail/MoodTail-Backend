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
                .thenReturn(2, 3, 4, 5, 6, 7);
        GuestDataMergeRepository repository = new GuestDataMergeRepository(jdbcTemplate);

        GuestDataMergeRepository.MergeResult result = repository.merge(7L, 11L);

        assertThat(result.transferredDrinkingRecords()).isEqualTo(2);
        assertThat(result.copiedMoodTestResults()).isEqualTo(3);
        assertThat(result.mergedMoodTypes()).isEqualTo(9);
        assertThat(result.mergedCocktails()).isEqualTo(13);
        verify(jdbcTemplate, times(6)).update(anyString(), any(Object[].class));
    }
}
