package com.example.moodtail.domain.user.repository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

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

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object[]> argumentsCaptor = ArgumentCaptor.forClass(Object[].class);
        verify(jdbcTemplate, times(8)).update(sqlCaptor.capture(), argumentsCaptor.capture());

        List<String> sql = sqlCaptor.getAllValues();
        assertThat(sql.get(0)).contains("update mood_test_results", "target_result.user_id = ?");
        assertThat(sql.get(1)).contains("update recommendation_sessions", "set rs.user_id = ?");
        assertThat(sql.get(2)).contains("update drinking_records");
        assertThat(sql.get(3)).contains("update inquiries");
        assertThat(sql.get(4)).contains("insert ignore into cocktail_favorites");
        assertThat(sql.get(5)).contains("delete from cocktail_favorites");
        assertThat(sql.get(6)).contains("insert into user_unlocked_mood_types", "on duplicate key update");
        assertThat(sql.get(7)).contains("delete from user_unlocked_mood_types");

        List<Object[]> arguments = argumentsCaptor.getAllValues();
        assertThat(arguments.get(0)).containsExactly(11L, 11L, 7L);
        assertThat(arguments.get(1)).containsExactly(11L, 7L, 11L, 11L);
        assertThat(arguments.get(2)).containsExactly(11L, 7L);
        assertThat(arguments.get(3)).containsExactly(11L, 7L);
        assertThat(arguments.get(4)).containsExactly(11L, 11L, 7L);
        assertThat(arguments.get(5)).containsExactly(7L);
        assertThat(arguments.get(6)).containsExactly(11L, 7L);
        assertThat(arguments.get(7)).containsExactly(7L);
    }
}
