package com.example.moodtail.domain.report.service;

import com.example.moodtail.domain.report.entity.MonthlyReportShare;
import com.example.moodtail.domain.report.repository.MonthlyReportShareRepository;
import com.example.moodtail.domain.user.entity.User;
import com.example.moodtail.domain.user.repository.UserRepository;
import com.example.moodtail.global.common.exception.RestApiException;
import com.example.moodtail.global.infra.s3.S3StorageException;
import com.example.moodtail.global.infra.s3.S3StorageService;
import com.example.moodtail.global.infra.s3.S3StorageService.StoredImage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

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
    private static final Clock CLOCK = Clock.fixed(
            Instant.parse("2026-08-09T05:30:00Z"),
            ZoneId.of("Asia/Seoul")
    );

    @Mock
    private MonthlyReportService monthlyReportService;
    @Mock
    private S3StorageService storageService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private MonthlyReportShareRepository monthlyReportShareRepository;
    @Mock
    private MultipartFile image;

    private MonthlyReportShareImageService shareImageService;
    private User user;

    @BeforeEach
    void setUp() {
        shareImageService = new MonthlyReportShareImageService(
                monthlyReportService,
                storageService,
                userRepository,
                monthlyReportShareRepository,
                CLOCK
        );
        ReflectionTestUtils.setField(
                shareImageService,
                "shareBaseUrl",
                "https://mood-tail.site/"
        );
        ReflectionTestUtils.setField(
                shareImageService,
                "shareFrontendBaseUrl",
                "https://mood-tail.site/"
        );
        user = User.createMember("회원", LocalDateTime.now(CLOCK));
        ReflectionTestUtils.setField(user, "id", USER_ID);
    }

    @Test
    void uploadsImageAndCreatesShareUrl() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(storageService.uploadImage(image, "public/reports/monthly"))
                .thenReturn("https://cdn.example/monthly-report.png");

        var response = shareImageService.uploadShareImage(USER_ID, 2026, 7, image);

        assertThat(response.shareToken()).matches("mr_[A-Za-z0-9_-]{24}");
        assertThat(response.shareUrl())
                .isEqualTo("https://mood-tail.site/share/reports/monthly/" + response.shareToken());
        verify(monthlyReportService).getMonthlyReport(USER_ID, 2026, 7);
        verify(storageService).uploadImage(image, "public/reports/monthly");

        ArgumentCaptor<MonthlyReportShare> shareCaptor =
                ArgumentCaptor.forClass(MonthlyReportShare.class);
        verify(monthlyReportShareRepository).saveAndFlush(shareCaptor.capture());
        MonthlyReportShare savedShare = shareCaptor.getValue();
        assertThat(savedShare.getUser()).isSameAs(user);
        assertThat(savedShare.getShareToken()).isEqualTo(response.shareToken());
        assertThat(savedShare.getReportYear()).isEqualTo(2026);
        assertThat(savedShare.getReportMonth()).isEqualTo(7);
        assertThat(savedShare.getShareImageUrl())
                .isEqualTo("https://cdn.example/monthly-report.png");
    }

    @Test
    void rejectsUnavailableReportBeforeUploading() {
        RestApiException reportFailure = new RestApiException(INSUFFICIENT_DATA);
        when(monthlyReportService.getMonthlyReport(USER_ID, 2026, 7)).thenThrow(reportFailure);

        assertThatThrownBy(() -> shareImageService.uploadShareImage(USER_ID, 2026, 7, image))
                .isSameAs(reportFailure);

        verify(userRepository, never()).findById(any());
        verify(storageService, never()).uploadImage(any(), anyString());
        verify(monthlyReportShareRepository, never()).saveAndFlush(any());
    }

    @Test
    void mapsS3UploadFailureToReportErrorContract() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(storageService.uploadImage(image, "public/reports/monthly"))
                .thenThrow(new S3StorageException("storage unavailable"));

        assertThatThrownBy(() -> shareImageService.uploadShareImage(USER_ID, 2026, 7, image))
                .isInstanceOfSatisfying(RestApiException.class, exception ->
                        assertThat(exception.getErrorCode().getCode()).isEqualTo("REPORT_IMAGE503"));

        verify(monthlyReportShareRepository, never()).saveAndFlush(any());
    }

    @Test
    void deletesUploadedImageWhenShareCannotBeSaved() {
        IllegalStateException databaseFailure = new IllegalStateException("database unavailable");
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(storageService.uploadImage(image, "public/reports/monthly"))
                .thenReturn("https://cdn.example/monthly-report.png");
        when(monthlyReportShareRepository.saveAndFlush(any())).thenThrow(databaseFailure);

        assertThatThrownBy(() -> shareImageService.uploadShareImage(USER_ID, 2026, 7, image))
                .isSameAs(databaseFailure);

        verify(storageService).deleteImage("https://cdn.example/monthly-report.png");
    }

    @Test
    void deletesUploadedImageWhenTransactionRollsBackAfterFlush() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(storageService.uploadImage(image, "public/reports/monthly"))
                .thenReturn("https://cdn.example/monthly-report.png");
        TransactionSynchronizationManager.initSynchronization();

        try {
            shareImageService.uploadShareImage(USER_ID, 2026, 7, image);
            TransactionSynchronizationManager.getSynchronizations()
                    .forEach(synchronization -> synchronization.afterCompletion(
                            TransactionSynchronization.STATUS_ROLLED_BACK
                    ));
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }

        verify(storageService).deleteImage("https://cdn.example/monthly-report.png");
    }

    @Test
    void returnsSharedImageWithinRetentionPeriod() {
        MonthlyReportShare share = share("mr_test");
        when(monthlyReportShareRepository.findByShareTokenAndCreatedAtAfter(
                "mr_test",
                LocalDateTime.of(2026, 7, 10, 14, 30)
        )).thenReturn(Optional.of(share));

        var response = shareImageService.getSharedImage("mr_test");

        assertThat(response.year()).isEqualTo(2026);
        assertThat(response.month()).isEqualTo(7);
        assertThat(response.shareImageUrl())
                .isEqualTo("https://mood-tail.site/api/v1/reports/monthly/shares/mr_test/image");
    }

    @Test
    void rejectsMissingOrExpiredShareToken() {
        when(monthlyReportShareRepository.findByShareTokenAndCreatedAtAfter(
                "mr_expired",
                LocalDateTime.of(2026, 7, 10, 14, 30)
        )).thenReturn(Optional.empty());

        assertThatThrownBy(() -> shareImageService.getSharedImage("mr_expired"))
                .isInstanceOfSatisfying(RestApiException.class, exception ->
                        assertThat(exception.getErrorCode().getCode()).isEqualTo("REPORT404"));
    }

    @Test
    void createsOgAndFrontendUrlsForSharePage() {
        MonthlyReportShare share = share("mr_test");
        when(monthlyReportShareRepository.findByShareTokenAndCreatedAtAfter(
                "mr_test",
                LocalDateTime.of(2026, 7, 10, 14, 30)
        )).thenReturn(Optional.of(share));

        var response = shareImageService.getSharePage("mr_test");

        assertThat(response.shareUrl())
                .isEqualTo("https://mood-tail.site/share/reports/monthly/mr_test");
        assertThat(response.frontendUrl())
                .isEqualTo("https://mood-tail.site/reports/monthly/share/mr_test");
        assertThat(response.shareImageUrl())
                .isEqualTo("https://mood-tail.site/api/v1/reports/monthly/shares/mr_test/image");
    }

    @Test
    void loadsSharedImageFileOnlyAfterValidatingShareToken() {
        MonthlyReportShare share = share("mr_test");
        when(monthlyReportShareRepository.findByShareTokenAndCreatedAtAfter(
                "mr_test",
                LocalDateTime.of(2026, 7, 10, 14, 30)
        )).thenReturn(Optional.of(share));
        when(storageService.getImage("https://cdn.example/monthly-report.png"))
                .thenReturn(new StoredImage(new byte[]{1, 2, 3}, "image/png"));

        var response = shareImageService.getSharedImageFile("mr_test");

        assertThat(response.content()).containsExactly(1, 2, 3);
        assertThat(response.contentType()).isEqualTo("image/png");
        verify(storageService).getImage("https://cdn.example/monthly-report.png");
    }

    @Test
    void mapsSharedImageLoadingFailureToReportErrorContract() {
        MonthlyReportShare share = share("mr_test");
        when(monthlyReportShareRepository.findByShareTokenAndCreatedAtAfter(
                "mr_test",
                LocalDateTime.of(2026, 7, 10, 14, 30)
        )).thenReturn(Optional.of(share));
        when(storageService.getImage("https://cdn.example/monthly-report.png"))
                .thenThrow(new S3StorageException("storage unavailable"));

        assertThatThrownBy(() -> shareImageService.getSharedImageFile("mr_test"))
                .isInstanceOfSatisfying(RestApiException.class, exception ->
                        assertThat(exception.getErrorCode().getCode()).isEqualTo("REPORT_IMAGE503"));
    }

    @Test
    void rejectsExpiredShareBeforeLoadingImageFromStorage() {
        when(monthlyReportShareRepository.findByShareTokenAndCreatedAtAfter(
                "mr_expired",
                LocalDateTime.of(2026, 7, 10, 14, 30)
        )).thenReturn(Optional.empty());

        assertThatThrownBy(() -> shareImageService.getSharedImageFile("mr_expired"))
                .isInstanceOfSatisfying(RestApiException.class, exception ->
                        assertThat(exception.getErrorCode().getCode()).isEqualTo("REPORT404"));

        verify(storageService, never()).getImage(anyString());
    }

    private MonthlyReportShare share(String shareToken) {
        return MonthlyReportShare.create(
                user,
                shareToken,
                2026,
                7,
                "https://cdn.example/monthly-report.png"
        );
    }
}
