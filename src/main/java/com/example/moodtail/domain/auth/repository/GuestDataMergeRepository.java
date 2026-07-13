package com.example.moodtail.domain.auth.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class GuestDataMergeRepository {

    private static final String TRANSFER_NON_CONFLICTING_MOOD_TEST_RESULTS_SQL = """
            update mood_test_results guest_result
              left join mood_test_results target_result
                on target_result.user_id = ?
               and target_result.result_date = guest_result.result_date
               set guest_result.user_id = ?
             where guest_result.user_id = ?
               and target_result.id is null
            """;

    private static final String TRANSFER_RECOMMENDATION_SESSIONS_SQL = """
            update recommendation_sessions rs
              left join mood_test_results mtr
                on mtr.id = rs.mood_test_result_id
              left join mood_test_results partner_mtr
                on partner_mtr.id = rs.partner_mood_test_result_id
               set rs.user_id = ?
             where rs.user_id = ?
               and (rs.mood_test_result_id is null or mtr.user_id = ?)
               and (rs.partner_mood_test_result_id is null or partner_mtr.user_id = ?)
            """;

    private static final String TRANSFER_DRINKING_RECORDS_SQL = """
            update drinking_records
               set user_id = ?
             where user_id = ?
            """;

    private static final String TRANSFER_INQUIRIES_SQL = """
            update inquiries
               set user_id = ?
             where user_id = ?
            """;

    private static final String INSERT_NEW_COCKTAIL_FAVORITES_SQL = """
            insert ignore into cocktail_favorites (user_id, cocktail_id)
            select ?, guest_favorite.cocktail_id
              from cocktail_favorites guest_favorite
              left join cocktail_favorites target_favorite
                on target_favorite.user_id = ?
               and target_favorite.cocktail_id = guest_favorite.cocktail_id
             where guest_favorite.user_id = ?
               and target_favorite.id is null
            """;

    private static final String DELETE_GUEST_COCKTAIL_FAVORITES_SQL = """
            delete from cocktail_favorites
             where user_id = ?
            """;

    private static final String MERGE_MOOD_TYPE_UNLOCKS_SQL = """
            insert into user_unlocked_mood_types (user_id, mood_type_id, unlocked_at)
            select ?, guest_unlock.mood_type_id, guest_unlock.unlocked_at
              from user_unlocked_mood_types guest_unlock
             where guest_unlock.user_id = ?
            on duplicate key update
                unlocked_at = least(user_unlocked_mood_types.unlocked_at, values(unlocked_at))
            """;

    private static final String DELETE_GUEST_MOOD_TYPE_UNLOCKS_SQL = """
            delete from user_unlocked_mood_types
             where user_id = ?
            """;

    private final JdbcTemplate jdbcTemplate;

    public MergeResult merge(Long guestUserId, Long targetUserId) {
        int transferredMoodTestResults = jdbcTemplate.update(
                TRANSFER_NON_CONFLICTING_MOOD_TEST_RESULTS_SQL,
                targetUserId,
                targetUserId,
                guestUserId
        );
        int transferredRecommendationSessions = jdbcTemplate.update(
                TRANSFER_RECOMMENDATION_SESSIONS_SQL,
                targetUserId,
                guestUserId,
                targetUserId,
                targetUserId
        );
        int transferredDrinkingRecords = jdbcTemplate.update(
                TRANSFER_DRINKING_RECORDS_SQL,
                targetUserId,
                guestUserId
        );
        int transferredInquiries = jdbcTemplate.update(
                TRANSFER_INQUIRIES_SQL,
                targetUserId,
                guestUserId
        );
        int mergedCocktailFavorites = jdbcTemplate.update(
                INSERT_NEW_COCKTAIL_FAVORITES_SQL,
                targetUserId,
                targetUserId,
                guestUserId
        );
        jdbcTemplate.update(DELETE_GUEST_COCKTAIL_FAVORITES_SQL, guestUserId);

        int mergedMoodTypes = jdbcTemplate.update(
                MERGE_MOOD_TYPE_UNLOCKS_SQL,
                targetUserId,
                guestUserId
        );
        jdbcTemplate.update(DELETE_GUEST_MOOD_TYPE_UNLOCKS_SQL, guestUserId);

        return new MergeResult(
                transferredMoodTestResults,
                transferredRecommendationSessions,
                transferredDrinkingRecords,
                transferredInquiries,
                mergedCocktailFavorites,
                mergedMoodTypes
        );
    }

    public record MergeResult(
            int transferredMoodTestResults,
            int transferredRecommendationSessions,
            int transferredDrinkingRecords,
            int transferredInquiries,
            int mergedCocktailFavorites,
            int mergedMoodTypes
    ) {
    }
}
