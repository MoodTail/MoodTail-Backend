package com.example.moodtail.domain.report.service;

import com.example.moodtail.domain.report.dto.response.MonthlyReportShareImageResponse;
import com.example.moodtail.global.common.exception.RestApiException;
import com.example.moodtail.global.infra.s3.S3StorageException;
import com.example.moodtail.global.infra.s3.S3StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import static com.example.moodtail.global.common.exception.code.status.ReportErrorStatus.SHARE_IMAGE_UNAVAILABLE;

@Service
@Slf4j
@RequiredArgsConstructor
public class MonthlyReportShareImageService {

    private static final String DIRECTORY = "reports/monthly";

    private final MonthlyReportService monthlyReportService;
    private final S3StorageService storageService;

    public MonthlyReportShareImageResponse uploadShareImage(
            Long userId,
            int year,
            int month,
            MultipartFile image
    ) {
        monthlyReportService.getMonthlyReport(userId, year, month);

        try {
            String imageUrl = storageService.uploadImage(image, DIRECTORY);
            return new MonthlyReportShareImageResponse(imageUrl);
        } catch (S3StorageException exception) {
            log.error("Failed to upload monthly report share image", exception);
            throw new RestApiException(SHARE_IMAGE_UNAVAILABLE);
        }
    }
}
