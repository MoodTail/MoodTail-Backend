package com.example.moodtail.domain.auth.service;

import com.example.moodtail.domain.auth.repository.AccountWithdrawalRepository;
import com.example.moodtail.domain.auth.repository.AccountWithdrawalRepository.PendingAssetDeletion;
import com.example.moodtail.global.infra.s3.S3StorageService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountWithdrawalAssetCleanupService {

    private final AccountWithdrawalRepository accountWithdrawalRepository;
    private final S3StorageService s3StorageService;

    @Value("${auth.withdrawal.asset-cleanup-batch-size:100}")
    private int batchSize;

    @Value("${auth.withdrawal.asset-cleanup-retry-delay-millis:300000}")
    private long retryDelayMillis;

    @PostConstruct
    void validateConfiguration() {
        if (batchSize < 1 || retryDelayMillis < 1) {
            throw new IllegalArgumentException("Account-withdrawal cleanup settings must be positive");
        }
    }

    public void cleanup(List<PendingAssetDeletion> pendingAssets) {
        if (pendingAssets == null || pendingAssets.isEmpty()) {
            return;
        }
        pendingAssets.forEach(this::deleteOrDefer);
    }

    @Scheduled(
            initialDelayString = "${auth.withdrawal.asset-cleanup-initial-delay-millis:30000}",
            fixedDelayString = "${auth.withdrawal.asset-cleanup-interval-millis:60000}"
    )
    void retryPendingDeletions() {
        cleanup(accountWithdrawalRepository.findPendingAssetDeletions(batchSize));
    }

    private void deleteOrDefer(PendingAssetDeletion pendingAsset) {
        try {
            s3StorageService.deleteImage(pendingAsset.imageUrl());
            accountWithdrawalRepository.completeAssetDeletion(pendingAsset.imageId());
        } catch (RuntimeException exception) {
            deferSafely(pendingAsset.imageId());
            log.warn(
                    "Deferred account-withdrawal asset deletion: imageId={}, cause={}",
                    pendingAsset.imageId(),
                    exception.getClass().getSimpleName()
            );
            log.debug("Account-withdrawal asset deletion failure", exception);
        }
    }

    private void deferSafely(Long imageId) {
        try {
            accountWithdrawalRepository.deferAssetDeletion(
                    imageId,
                    LocalDateTime.now().plus(Duration.ofMillis(retryDelayMillis))
            );
        } catch (RuntimeException exception) {
            log.error(
                    "Failed to defer account-withdrawal asset deletion: imageId={}, cause={}",
                    imageId,
                    exception.getClass().getSimpleName()
            );
            log.debug("Account-withdrawal asset deferral failure", exception);
        }
    }
}
