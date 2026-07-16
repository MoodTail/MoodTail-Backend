package com.example.moodtail.domain.auth.repository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JdbcAccountWithdrawalRepositoryTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Test
    void deletesOnlyRecommendationSessionsOwnedByWithdrawingUser() {
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), any(Object[].class)))
                .thenReturn(0);
        when(jdbcTemplate.update(anyString(), any(Object[].class)))
                .thenReturn(1);
        JdbcAccountWithdrawalRepository repository = new JdbcAccountWithdrawalRepository(jdbcTemplate);

        repository.deleteAccountData(7L);

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object[]> argumentsCaptor = ArgumentCaptor.forClass(Object[].class);
        verify(jdbcTemplate, atLeastOnce()).update(sqlCaptor.capture(), argumentsCaptor.capture());

        List<String> sql = sqlCaptor.getAllValues();
        int itemDeleteIndex = indexOfSql(sql, "delete from recommendation_items");
        int sessionDeleteIndex = indexOfSql(sql, "delete from recommendation_sessions");
        int inquiryAnonymizationIndex = indexOfSql(sql, "update inquiries");
        int testResultAnonymizationIndex = indexOfSql(sql, "update mood_test_results");
        int userDeleteIndex = indexOfSql(sql, "delete from users");

        assertThat(sql.get(itemDeleteIndex))
                .contains("rs.user_id = ?")
                .doesNotContain("mood_test_result_id in", "partner_mood_test_result_id in");
        assertThat(sql.get(sessionDeleteIndex))
                .contains("user_id = ?")
                .doesNotContain("mood_test_result_id in", "partner_mood_test_result_id in");
        assertThat(sql.get(inquiryAnonymizationIndex))
                .contains("user_id = null", "contact_email = null");
        assertThat(sql.get(testResultAnonymizationIndex))
                .contains("user_id = null", "share_token = null");
        assertThat(userDeleteIndex)
                .isGreaterThan(inquiryAnonymizationIndex)
                .isGreaterThan(testResultAnonymizationIndex);
        assertThat(argumentsCaptor.getAllValues().get(itemDeleteIndex)).containsExactly(7L);
        assertThat(argumentsCaptor.getAllValues().get(sessionDeleteIndex)).containsExactly(7L);
        assertThat(argumentsCaptor.getAllValues().get(inquiryAnonymizationIndex)).containsExactly(7L);
        assertThat(argumentsCaptor.getAllValues().get(testResultAnonymizationIndex)).containsExactly(7L);
        assertThat(argumentsCaptor.getAllValues().get(userDeleteIndex)).containsExactly(7L);
    }

    private int indexOfSql(List<String> sql, String fragment) {
        for (int index = 0; index < sql.size(); index++) {
            if (sql.get(index).contains(fragment)) {
                return index;
            }
        }
        throw new AssertionError("SQL not executed: " + fragment);
    }
}
