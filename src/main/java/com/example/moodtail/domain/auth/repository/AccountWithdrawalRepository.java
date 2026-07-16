package com.example.moodtail.domain.auth.repository;

import java.time.LocalDateTime;
import java.util.List;

public interface AccountWithdrawalRepository {

    WithdrawalResult deleteAccountData(Long userId);

    List<PendingAssetDeletion> findPendingAssetDeletions(int limit);

    void completeAssetDeletion(Long imageId);

    void deferAssetDeletion(Long imageId, LocalDateTime nextAttemptAt);

    record PendingAssetDeletion(Long imageId, String imageUrl) {
    }

    record WithdrawalResult(
            int deletedUsers,
            int anonymizedTestResults,
            List<PendingAssetDeletion> pendingAssetDeletions
    ) {
    }
}
