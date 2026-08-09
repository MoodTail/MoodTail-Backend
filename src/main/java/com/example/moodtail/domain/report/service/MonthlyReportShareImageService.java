package com.example.moodtail.domain.report.service;

import com.example.moodtail.domain.report.dto.response.MonthlyReportShareImageResponse;
import com.example.moodtail.domain.report.dto.response.MonthlyReportSharePageResponse;
import com.example.moodtail.domain.report.dto.response.MonthlyReportSharedImageFile;
import com.example.moodtail.domain.report.dto.response.MonthlyReportSharedImageResponse;
import com.example.moodtail.domain.report.entity.MonthlyReportShare;
import com.example.moodtail.domain.report.repository.MonthlyReportShareRepository;
import com.example.moodtail.domain.user.entity.User;
import com.example.moodtail.domain.user.repository.UserRepository;
import com.example.moodtail.global.common.exception.RestApiException;
import com.example.moodtail.global.infra.s3.S3StorageException;
import com.example.moodtail.global.infra.s3.S3StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Base64;

import static com.example.moodtail.global.common.exception.code.status.AuthErrorStatus.USER_NOT_FOUND;
import static com.example.moodtail.global.common.exception.code.status.ReportErrorStatus.SHARE_NOT_FOUND;
import static com.example.moodtail.global.common.exception.code.status.ReportErrorStatus.SHARE_IMAGE_UNAVAILABLE;

@Service
@Slf4j
@RequiredArgsConstructor
public class MonthlyReportShareImageService {

    private static final String DIRECTORY = "public/reports/monthly";
    private static final String SHARE_PATH = "/share/reports/monthly/";
    private static final String SHARED_IMAGE_PATH = "/api/v1/reports/monthly/shares/";
    private static final String FRONTEND_SHARE_PATH = "/reports/monthly/share/";
    private static final String IMAGE_PATH_SUFFIX = "/image";
    private static final int SHARE_RETENTION_DAYS = 30;
    private static final int TOKEN_BYTE_LENGTH = 18;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final MonthlyReportService monthlyReportService;
    private final S3StorageService storageService;
    private final UserRepository userRepository;
    private final MonthlyReportShareRepository monthlyReportShareRepository;
    private final Clock clock;

    @Value("${app.share.base-url}")
    private String shareBaseUrl;

    @Value("${app.share.frontend-base-url}")
    private String shareFrontendBaseUrl;

    @Transactional
    public MonthlyReportShareImageResponse uploadShareImage(
            Long userId,
            int year,
            int month,
            MultipartFile image
    ) {
        monthlyReportService.getMonthlyReport(userId, year, month);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RestApiException(USER_NOT_FOUND));

        String imageUrl = uploadImage(image);
        String shareToken = generateShareToken();
        MonthlyReportShare share = MonthlyReportShare.create(
                user,
                shareToken,
                year,
                month,
                imageUrl
        );

        try {
            monthlyReportShareRepository.saveAndFlush(share);
        } catch (RuntimeException exception) {
            deleteUploadedImage(imageUrl);
            throw exception;
        }
        registerRollbackCleanup(imageUrl);

        return new MonthlyReportShareImageResponse(
                shareToken,
                normalizeBaseUrl(shareBaseUrl) + SHARE_PATH + shareToken
        );
    }

    @Transactional(readOnly = true)
    public MonthlyReportSharedImageResponse getSharedImage(String shareToken) {
        MonthlyReportShare share = findShare(shareToken);
        return new MonthlyReportSharedImageResponse(
                share.getReportYear(),
                share.getReportMonth(),
                createSharedImageUrl(shareToken)
        );
    }

    @Transactional(readOnly = true)
    public MonthlyReportSharePageResponse getSharePage(String shareToken) {
        findShare(shareToken);
        return new MonthlyReportSharePageResponse(
                normalizeBaseUrl(shareBaseUrl) + SHARE_PATH + shareToken,
                normalizeBaseUrl(shareFrontendBaseUrl) + FRONTEND_SHARE_PATH + shareToken,
                createSharedImageUrl(shareToken)
        );
    }

    @Transactional(readOnly = true)
    public MonthlyReportSharedImageFile getSharedImageFile(String shareToken) {
        MonthlyReportShare share = findShare(shareToken);
        try {
            S3StorageService.StoredImage image = storageService.getImage(share.getShareImageUrl());
            return new MonthlyReportSharedImageFile(image.content(), image.contentType());
        } catch (S3StorageException exception) {
            log.error("Failed to load monthly report share image", exception);
            throw new RestApiException(SHARE_IMAGE_UNAVAILABLE);
        }
    }

    private String uploadImage(MultipartFile image) {
        try {
            return storageService.uploadImage(image, DIRECTORY);
        } catch (S3StorageException exception) {
            log.error("Failed to upload monthly report share image", exception);
            throw new RestApiException(SHARE_IMAGE_UNAVAILABLE);
        }
    }

    private MonthlyReportShare findShare(String shareToken) {
        LocalDateTime createdAfter = LocalDateTime.now(clock).minusDays(SHARE_RETENTION_DAYS);
        return monthlyReportShareRepository
                .findByShareTokenAndCreatedAtAfter(shareToken, createdAfter)
                .orElseThrow(() -> new RestApiException(SHARE_NOT_FOUND));
    }

    private String generateShareToken() {
        byte[] randomBytes = new byte[TOKEN_BYTE_LENGTH];
        SECURE_RANDOM.nextBytes(randomBytes);
        return "mr_" + Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(randomBytes);
    }

    private void deleteUploadedImage(String imageUrl) {
        try {
            storageService.deleteImage(imageUrl);
        } catch (S3StorageException exception) {
            log.error(
                    "Failed to clean up monthly report share image: imageUrl={}",
                    imageUrl,
                    exception
            );
        }
    }

    private void registerRollbackCleanup(String imageUrl) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status == STATUS_ROLLED_BACK) {
                    deleteUploadedImage(imageUrl);
                }
            }
        });
    }

    private String normalizeBaseUrl(String baseUrl) {
        return baseUrl.endsWith("/")
                ? baseUrl.substring(0, baseUrl.length() - 1)
                : baseUrl;
    }

    private String createSharedImageUrl(String shareToken) {
        return normalizeBaseUrl(shareBaseUrl)
                + SHARED_IMAGE_PATH
                + shareToken
                + IMAGE_PATH_SUFFIX;
    }
}
