package com.example.moodtail.domain.auth.service;

import com.example.moodtail.domain.auth.repository.AccountWithdrawalRepository;
import com.example.moodtail.domain.auth.repository.AccountWithdrawalRepository.PendingAssetDeletion;
import com.example.moodtail.global.infra.s3.S3StorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountWithdrawalAssetCleanupServiceTest {

    @Mock AccountWithdrawalRepository accountWithdrawalRepository;
    @Mock S3StorageService s3StorageService;

    private AccountWithdrawalAssetCleanupService service;

    @BeforeEach
    void setUp() {
        service = new AccountWithdrawalAssetCleanupService(accountWithdrawalRepository, s3StorageService);
        ReflectionTestUtils.setField(service, "batchSize", 20);
        ReflectionTestUtils.setField(service, "retryDelayMillis", 60_000L);
        service.validateConfiguration();
    }

    @Test
    void successfulDeletionCompletesPersistentTask() {
        PendingAssetDeletion task = new PendingAssetDeletion(31L, "https://bucket.s3.ap-northeast-2.amazonaws.com/a.jpg");

        service.cleanup(List.of(task));

        verify(s3StorageService).deleteImage(task.imageUrl());
        verify(accountWithdrawalRepository).completeAssetDeletion(task.imageId());
        verify(accountWithdrawalRepository, never()).deferAssetDeletion(any(), any());
    }

    @Test
    void storageFailureDefersTaskWithoutFailingCompletedWithdrawal() {
        PendingAssetDeletion task = new PendingAssetDeletion(32L, "https://bucket.s3.ap-northeast-2.amazonaws.com/b.jpg");
        doThrow(new IllegalStateException("S3 unavailable"))
                .when(s3StorageService).deleteImage(task.imageUrl());

        service.cleanup(List.of(task));

        verify(accountWithdrawalRepository).deferAssetDeletion(eq(32L), any(LocalDateTime.class));
        verify(accountWithdrawalRepository, never()).completeAssetDeletion(any());
    }

    @Test
    void scheduledRetryUsesConfiguredBatchSize() {
        PendingAssetDeletion task = new PendingAssetDeletion(33L, "https://bucket.s3.ap-northeast-2.amazonaws.com/c.jpg");
        when(accountWithdrawalRepository.findPendingAssetDeletions(20)).thenReturn(List.of(task));

        service.retryPendingDeletions();

        verify(s3StorageService).deleteImage(task.imageUrl());
        verify(accountWithdrawalRepository).completeAssetDeletion(33L);
    }
}
