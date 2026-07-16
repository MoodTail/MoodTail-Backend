package com.example.moodtail.domain.auth.repository;

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
import static org.mockito.ArgumentMatchers.eq;
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
                .thenReturn(2, 3, 4, 5, 6, 7, 8, 9, 10, 1);
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), any(Object.class)))
                .thenReturn(1);
        GuestDataMergeRepository repository = new GuestDataMergeRepository(jdbcTemplate);

        GuestDataMergeRepository.MergeResult result = repository.merge(7L, 11L);

        assertThat(result.transferredMoodTestResults()).isEqualTo(2);
        assertThat(result.transferredRecommendationSessions()).isEqualTo(3);
        assertThat(result.transferredDrinkingRecords()).isEqualTo(4);
        assertThat(result.transferredInquiries()).isEqualTo(5);
        assertThat(result.mergedCocktailFavorites()).isEqualTo(6);
        assertThat(result.mergedMoodTypes()).isEqualTo(8);
        assertThat(result.mergedCocktails()).isEqualTo(10);
        assertThat(result.retiredGuests()).isEqualTo(1);

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object[]> argumentsCaptor = ArgumentCaptor.forClass(Object[].class);
        verify(jdbcTemplate, times(11)).update(sqlCaptor.capture(), argumentsCaptor.capture());

        List<String> sql = sqlCaptor.getAllValues();
        assertThat(sql.get(0)).contains("update mood_test_results", "target_result.user_id = ?");
        assertThat(sql.get(1)).contains("update recommendation_sessions", "set rs.user_id = ?");
        assertThat(sql.get(2)).contains("update drinking_records");
        assertThat(sql.get(3)).contains("update inquiries");
        assertThat(sql.get(4)).contains("insert ignore into cocktail_favorites");
        assertThat(sql.get(5)).contains("delete from cocktail_favorites");
        assertThat(sql.get(6)).contains("insert into user_unlocked_mood_types", "on duplicate key update");
        assertThat(sql.get(7)).contains("delete from user_unlocked_mood_types");
        assertThat(sql.get(8)).contains("insert into user_unlocked_cocktails", "on duplicate key update");
        assertThat(sql.get(9)).contains("delete from user_unlocked_cocktails");
        assertThat(sql.get(10)).contains("update users", "set guest_uuid = ?", "deleted_at = now()");

        List<Object[]> arguments = argumentsCaptor.getAllValues();
        assertThat(arguments.get(0)).containsExactly(11L, 11L, 7L);
        assertThat(arguments.get(1)).containsExactly(11L, 7L, 11L, 11L);
        assertThat(arguments.get(2)).containsExactly(11L, 7L);
        assertThat(arguments.get(3)).containsExactly(11L, 7L);
        assertThat(arguments.get(4)).containsExactly(11L, 11L, 7L);
        assertThat(arguments.get(5)).containsExactly(7L);
        assertThat(arguments.get(6)).containsExactly(11L, 7L);
        assertThat(arguments.get(7)).containsExactly(7L);
        assertThat(arguments.get(8)).containsExactly(11L, 7L);
        assertThat(arguments.get(9)).containsExactly(7L);
        assertThat(arguments.get(10)).hasSize(2);
        assertThat(arguments.get(10)[1]).isEqualTo(7L);
    }

    @Test
    void skipsHistoryTransferWhenHistorySchemaIsNotInstalledYet() {
        when(jdbcTemplate.update(anyString(), any(Object[].class))).thenReturn(1);
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), any(Object.class)))
                .thenAnswer(invocation -> "drinking_records".equals(invocation.getArgument(2)) ? 0 : 1);
        GuestDataMergeRepository repository = new GuestDataMergeRepository(jdbcTemplate);

        GuestDataMergeRepository.MergeResult result = repository.merge(7L, 11L);

        assertThat(result.transferredDrinkingRecords()).isZero();
        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(jdbcTemplate, times(10)).update(sqlCaptor.capture(), any(Object[].class));
        assertThat(sqlCaptor.getAllValues()).noneMatch(sql -> sql.contains("update drinking_records"));
    }
}
