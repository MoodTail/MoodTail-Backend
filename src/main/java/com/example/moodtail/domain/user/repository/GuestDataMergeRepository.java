package com.example.moodtail.domain.user.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class GuestDataMergeRepository {

    private static final String TRANSFER_DRINKING_RECORDS_SQL = """
            update drinking_records
               set user_id = ?
             where user_id = ?
            """;

    private static final String COPY_NON_CONFLICTING_MOOD_TEST_RESULTS_SQL = """
            insert into mood_test_results (
                user_id,
                mood_type_id,
                result_date,
                alcohol_intensity,
                sweetness,
                sourness,
                refreshing,
                bitterness,
                share_token,
                created_at
            )
            select ?,
                   guest_result.mood_type_id,
                   guest_result.result_date,
                   guest_result.alcohol_intensity,
                   guest_result.sweetness,
                   guest_result.sourness,
                   guest_result.refreshing,
                   guest_result.bitterness,
                   null,
                   guest_result.created_at
              from mood_test_results guest_result
              left join mood_test_results existing_result
                on existing_result.user_id = ?
               and existing_result.result_date = guest_result.result_date
             where guest_result.user_id = ?
               and existing_result.id is null
            """;

    private static final String UPDATE_DUPLICATE_MOOD_TYPE_UNLOCKS_SQL = """
            update user_unlocked_mood_types existing_unlock
              join user_unlocked_mood_types guest_unlock
                on guest_unlock.user_id = ?
               and guest_unlock.mood_type_id = existing_unlock.mood_type_id
               set existing_unlock.unlocked_at = least(
                   existing_unlock.unlocked_at,
                   guest_unlock.unlocked_at
               )
             where existing_unlock.user_id = ?
            """;

    private static final String INSERT_NEW_MOOD_TYPE_UNLOCKS_SQL = """
            insert into user_unlocked_mood_types (user_id, mood_type_id, unlocked_at)
            select ?, guest_unlock.mood_type_id, guest_unlock.unlocked_at
              from user_unlocked_mood_types guest_unlock
              left join user_unlocked_mood_types existing_unlock
                on existing_unlock.user_id = ?
               and existing_unlock.mood_type_id = guest_unlock.mood_type_id
             where guest_unlock.user_id = ?
               and existing_unlock.id is null
            """;

    private static final String UPDATE_DUPLICATE_COCKTAIL_UNLOCKS_SQL = """
            update user_unlocked_cocktails existing_unlock
              join user_unlocked_cocktails guest_unlock
                on guest_unlock.user_id = ?
               and guest_unlock.cocktail_id = existing_unlock.cocktail_id
               set existing_unlock.unlocked_at = least(
                   existing_unlock.unlocked_at,
                   guest_unlock.unlocked_at
               )
             where existing_unlock.user_id = ?
            """;

    private static final String INSERT_NEW_COCKTAIL_UNLOCKS_SQL = """
            insert into user_unlocked_cocktails (user_id, cocktail_id, unlocked_at)
            select ?, guest_unlock.cocktail_id, guest_unlock.unlocked_at
              from user_unlocked_cocktails guest_unlock
              left join user_unlocked_cocktails existing_unlock
                on existing_unlock.user_id = ?
               and existing_unlock.cocktail_id = guest_unlock.cocktail_id
             where guest_unlock.user_id = ?
               and existing_unlock.id is null
            """;

    private final JdbcTemplate jdbcTemplate;

    public MergeResult merge(Long guestUserId, Long targetUserId) {
        int transferredDrinkingRecords = jdbcTemplate.update(
                TRANSFER_DRINKING_RECORDS_SQL,
                targetUserId,
                guestUserId
        );
        int copiedMoodTestResults = jdbcTemplate.update(
                COPY_NON_CONFLICTING_MOOD_TEST_RESULTS_SQL,
                targetUserId,
                targetUserId,
                guestUserId
        );
        int updatedMoodTypes = jdbcTemplate.update(
                UPDATE_DUPLICATE_MOOD_TYPE_UNLOCKS_SQL,
                guestUserId,
                targetUserId
        );
        int insertedMoodTypes = jdbcTemplate.update(
                INSERT_NEW_MOOD_TYPE_UNLOCKS_SQL,
                targetUserId,
                targetUserId,
                guestUserId
        );
        int updatedCocktails = jdbcTemplate.update(
                UPDATE_DUPLICATE_COCKTAIL_UNLOCKS_SQL,
                guestUserId,
                targetUserId
        );
        int insertedCocktails = jdbcTemplate.update(
                INSERT_NEW_COCKTAIL_UNLOCKS_SQL,
                targetUserId,
                targetUserId,
                guestUserId
        );

        return new MergeResult(
                transferredDrinkingRecords,
                copiedMoodTestResults,
                updatedMoodTypes + insertedMoodTypes,
                updatedCocktails + insertedCocktails
        );
    }

    public record MergeResult(
            int transferredDrinkingRecords,
            int copiedMoodTestResults,
            int mergedMoodTypes,
            int mergedCocktails
    ) {
    }
}
