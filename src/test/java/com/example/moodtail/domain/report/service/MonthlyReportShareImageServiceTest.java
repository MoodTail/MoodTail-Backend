package com.example.moodtail.domain.report.service;

import com.example.moodtail.global.common.exception.RestApiException;
import com.example.moodtail.global.infra.s3.S3StorageException;
import com.example.moodtail.global.infra.s3.S3StorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import static com.example.moodtail.global.common.exception.code.status.ReportErrorStatus.INSUFFICIENT_DATA;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MonthlyReportShareImageServiceTest {

    private static final Long USER_ID = 1L;

    @Mock
    private MonthlyReportService monthlyReportService;
    @Mock
    private S3StorageService storageService;
    @Mock
    private MultipartFile image;

    private MonthlyReportShareImageService shareImageService;

    @BeforeEach
    void setUp() {
        shareImageService = new MonthlyReportShareImageService(monthlyReportService, storageService);
    }

    @Test
    void uploadsTheClientGeneratedImage() {
        when(storageService.uploadImage(image, "reports/monthly"))
                .thenReturn("https://cdn.example/monthly-report.png");

        var response = shareImageService.uploadShareImage(USER_ID, 2026, 7, image);

        assertThat(response.shareImageUrl()).isEqualTo("https://cdn.example/monthly-report.png");
        verify(monthlyReportService).getMonthlyReport(USER_ID, 2026, 7);
        verify(storageService).uploadImage(image, "reports/monthly");
    }

    @Test
    void rejectsAnUnavailableReportBeforeUploading() {
        RestApiException reportFailure = new RestApiException(INSUFFICIENT_DATA);
        when(monthlyReportService.getMonthlyReport(USER_ID, 2026, 7)).thenThrow(reportFailure);

        assertThatThrownBy(() -> shareImageService.uploadShareImage(USER_ID, 2026, 7, image))
                .isSameAs(reportFailure);

        verify(storageService, never()).uploadImage(any(), anyString());
    }

    @Test
    void mapsS3UploadFailureToTheReportErrorContract() {
        when(storageService.uploadImage(image, "reports/monthly"))
                .thenThrow(new S3StorageException("storage unavailable"));

        assertThatThrownBy(() -> shareImageService.uploadShareImage(USER_ID, 2026, 7, image))
                .isInstanceOfSatisfying(RestApiException.class, exception ->
                        assertThat(exception.getErrorCode().getCode()).isEqualTo("REPORT_IMAGE_503"));
    }
}
