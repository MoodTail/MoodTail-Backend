package com.example.moodtail.domain.auth.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class JdbcAccountWithdrawalRepository implements AccountWithdrawalRepository {

    private static final String FIND_HISTORY_IMAGES_SQL = """
            select distinct image.id, image.image_url
              from images image
              join history_photos photo on photo.image_id = image.id
              join drinking_records dr on dr.id = photo.drinking_record_id
             where dr.user_id = ?
            """;

    private static final String DELETE_RECOMMENDATION_ITEMS_SQL = """
            delete from recommendation_items
             where recommendation_session_id in (
                   select rs.id
                     from recommendation_sessions rs
                    where rs.user_id = ?
             )
            """;

    private static final String DELETE_RECOMMENDATION_SESSIONS_SQL = """
            delete from recommendation_sessions
             where user_id = ?
            """;

    private static final String DELETE_HISTORY_PHOTOS_SQL = """
            delete from history_photos
             where drinking_record_id in (
                   select dr.id from drinking_records dr where dr.user_id = ?
             )
            """;

    private static final String DELETE_UNREFERENCED_IMAGE_SQL = """
            delete from images
             where id = ?
               and not exists (select 1 from history_photos where image_id = ?)
               and not exists (select 1 from cocktails where image_id = ?)
               and not exists (select 1 from mood_types where character_image_id = ?)
            """;

    private final JdbcTemplate jdbcTemplate;

    @Override
    public WithdrawalResult deleteAccountData(Long userId) {
        List<StoredImage> historyImages = List.of();
        boolean historySchemaAvailable = tableExists("drinking_records") && tableExists("history_photos");
        if (historySchemaAvailable) {
            historyImages = jdbcTemplate.query(
                    FIND_HISTORY_IMAGES_SQL,
                    (resultSet, rowNumber) -> new StoredImage(
                            resultSet.getLong("id"),
                            resultSet.getString("image_url")
                    ),
                    userId
            );
        }

        jdbcTemplate.update(DELETE_RECOMMENDATION_ITEMS_SQL, userId);
        jdbcTemplate.update(DELETE_RECOMMENDATION_SESSIONS_SQL, userId);
        if (historySchemaAvailable) {
            jdbcTemplate.update(DELETE_HISTORY_PHOTOS_SQL, userId);
            jdbcTemplate.update("delete from drinking_records where user_id = ?", userId);
        }

        List<PendingAssetDeletion> pendingAssets = enqueueUnreferencedImages(historyImages);
        jdbcTemplate.update("delete from cocktail_favorites where user_id = ?", userId);
        jdbcTemplate.update("delete from user_unlocked_mood_types where user_id = ?", userId);
        if (tableExists("user_unlocked_cocktails")) {
            jdbcTemplate.update("delete from user_unlocked_cocktails where user_id = ?", userId);
        }
        jdbcTemplate.update(
                "update inquiries set user_id = null, contact_email = null where user_id = ?",
                userId
        );
        jdbcTemplate.update("delete from user_term_agreements where user_id = ?", userId);
        jdbcTemplate.update("delete from social_accounts where user_id = ?", userId);
        jdbcTemplate.update("delete from local_accounts where user_id = ?", userId);
        int anonymizedTestResults = jdbcTemplate.update(
                "update mood_test_results set user_id = null, share_token = null where user_id = ?",
                userId
        );
        int deletedUsers = jdbcTemplate.update("delete from users where id = ?", userId);

        return new WithdrawalResult(deletedUsers, anonymizedTestResults, List.copyOf(pendingAssets));
    }

    @Override
    public List<PendingAssetDeletion> findPendingAssetDeletions(int limit) {
        return jdbcTemplate.query(
                "select image_id, image_url from account_withdrawal_asset_cleanup "
                        + "where next_attempt_at <= now() order by next_attempt_at, image_id limit ?",
                (resultSet, rowNumber) -> new PendingAssetDeletion(
                        resultSet.getLong("image_id"),
                        resultSet.getString("image_url")
                ),
                limit
        );
    }

    @Override
    public void completeAssetDeletion(Long imageId) {
        jdbcTemplate.update("delete from account_withdrawal_asset_cleanup where image_id = ?", imageId);
    }

    @Override
    public void deferAssetDeletion(Long imageId, LocalDateTime nextAttemptAt) {
        jdbcTemplate.update(
                "update account_withdrawal_asset_cleanup "
                        + "set attempt_count = attempt_count + 1, next_attempt_at = ? where image_id = ?",
                nextAttemptAt,
                imageId
        );
    }

    private List<PendingAssetDeletion> enqueueUnreferencedImages(List<StoredImage> images) {
        List<PendingAssetDeletion> pendingAssets = new ArrayList<>();
        for (StoredImage image : images) {
            int deleted = jdbcTemplate.update(
                    DELETE_UNREFERENCED_IMAGE_SQL,
                    image.id(),
                    image.id(),
                    image.id(),
                    image.id()
            );
            if (deleted == 1) {
                jdbcTemplate.update(
                        "insert into account_withdrawal_asset_cleanup "
                                + "(image_id, image_url, attempt_count, next_attempt_at, created_at) "
                                + "values (?, ?, 0, now(), now())",
                        image.id(),
                        image.url()
                );
                pendingAssets.add(new PendingAssetDeletion(image.id(), image.url()));
            }
        }
        return pendingAssets;
    }

    private boolean tableExists(String tableName) {
        Integer count = jdbcTemplate.queryForObject(
                "select count(*) from information_schema.tables "
                        + "where table_schema = database() and table_name = ?",
                Integer.class,
                tableName
        );
        return count != null && count > 0;
    }

    private record StoredImage(Long id, String url) {
    }
}
