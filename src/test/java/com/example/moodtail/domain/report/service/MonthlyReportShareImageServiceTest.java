package com.example.moodtail.domain.report.service;

import com.example.moodtail.domain.report.dto.response.MonthlyReportResponse;
import com.example.moodtail.global.common.exception.RestApiException;
import com.example.moodtail.global.infra.s3.config.S3Properties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Utilities;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.math.BigDecimal;
import java.util.Base64;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MonthlyReportShareImageServiceTest {

    private static final Long USER_ID = 1L;

    @Mock
    private MonthlyReportService monthlyReportService;
    @Mock
    private S3Client s3Client;
    private MonthlyReportShareImageService shareImageService;
    private S3Utilities s3Utilities;

    @BeforeEach
    void setUp() {
        s3Utilities = S3Utilities.builder()
                .region(Region.AP_NORTHEAST_2)
                .build();
        shareImageService = new MonthlyReportShareImageService(
                monthlyReportService,
                s3Client,
                new S3Properties("moodtail", "ap-northeast-2", "", "")
        );
        lenient().when(s3Client.utilities()).thenReturn(s3Utilities);
        lenient().when(s3Client.getObjectAsBytes(any(GetObjectRequest.class))).thenReturn(
                ResponseBytes.fromByteArray(
                        GetObjectResponse.builder().build(),
                        Base64.getDecoder().decode(
                                "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNk"
                                        + "YAAAAAYAAjCB0C8AAAAASUVORK5CYII="
                        )
                )
        );
    }

    @Test
    void rendersAndUploadsPngUnderAContentAddressedObjectKey() throws Exception {
        when(monthlyReportService.getMonthlyReport(USER_ID, 2026, 7)).thenReturn(report());
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenReturn(PutObjectResponse.builder().build());

        var response = shareImageService.createShareImage(USER_ID, 2026, 7);

        assertThat(response.shareImageUrl())
                .matches("https://moodtail\\.s3\\.ap-northeast-2\\.amazonaws\\.com/"
                        + "reports/monthly/2026/07/[0-9a-f]{64}\\.png");

        ArgumentCaptor<PutObjectRequest> request = ArgumentCaptor.forClass(PutObjectRequest.class);
        ArgumentCaptor<RequestBody> body = ArgumentCaptor.forClass(RequestBody.class);
        verify(s3Client).putObject(request.capture(), body.capture());
        assertThat(request.getValue().key())
                .matches("reports/monthly/2026/07/[0-9a-f]{64}\\.png")
                .doesNotContain("/1/");
        assertThat(request.getValue().contentType()).isEqualTo("image/png");
        assertThat(body.getValue().contentStreamProvider().newStream().readNBytes(8))
                .containsExactly(0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A);
    }

    @Test
    void rendersAtMostThreeCocktails() {
        List<MonthlyReportResponse.FrequentCocktail> topThree = List.of(
                cocktail(1L, "첫 번째", 1),
                cocktail(2L, "두 번째", 2),
                cocktail(3L, "세 번째", 3)
        );
        List<MonthlyReportResponse.FrequentCocktail> withFourth = List.of(
                topThree.get(0),
                topThree.get(1),
                topThree.get(2),
                cocktail(4L, "렌더링되면 안 되는 네 번째", 4)
        );
        when(monthlyReportService.getMonthlyReport(USER_ID, 2026, 7))
                .thenReturn(report(topThree))
                .thenReturn(report(withFourth));
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenReturn(PutObjectResponse.builder().build());

        var topThreeImage = shareImageService.createShareImage(USER_ID, 2026, 7);
        var withFourthImage = shareImageService.createShareImage(USER_ID, 2026, 7);

        assertThat(withFourthImage.shareImageUrl()).isEqualTo(topThreeImage.shareImageUrl());
    }

    @Test
    void failsWithoutUploadingWhenTheSharedS3BucketIsNotConfigured() {
        shareImageService = new MonthlyReportShareImageService(
                monthlyReportService,
                s3Client,
                new S3Properties("", "ap-northeast-2", "", "")
        );
        when(monthlyReportService.getMonthlyReport(USER_ID, 2026, 7)).thenReturn(report());

        assertThatThrownBy(() -> shareImageService.createShareImage(USER_ID, 2026, 7))
                .isInstanceOfSatisfying(RestApiException.class, exception ->
                        assertThat(exception.getErrorCode().getCode()).isEqualTo("REPORT_IMAGE_503"));

        verify(s3Client, never()).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    void mapsS3UploadFailureToTheReportErrorContract() {
        when(monthlyReportService.getMonthlyReport(USER_ID, 2026, 7)).thenReturn(report());
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenThrow(S3Exception.builder().message("storage unavailable").build());

        assertThatThrownBy(() -> shareImageService.createShareImage(USER_ID, 2026, 7))
                .isInstanceOfSatisfying(RestApiException.class, exception ->
                        assertThat(exception.getErrorCode().getCode()).isEqualTo("REPORT_IMAGE_503"));
    }

    private MonthlyReportResponse report() {
        return report(List.of(cocktail(7L, "모히토", 1)));
    }

    private MonthlyReportResponse report(
            List<MonthlyReportResponse.FrequentCocktail> frequentCocktails
    ) {
        MonthlyReportResponse.TasteProfile profile = new MonthlyReportResponse.TasteProfile(
                new BigDecimal("3.4"),
                new BigDecimal("2.8"),
                new BigDecimal("4.1"),
                new BigDecimal("4.5"),
                new BigDecimal("1.9")
        );
        MonthlyReportResponse.DisplayTasteScores scores = new MonthlyReportResponse.DisplayTasteScores(
                60,
                45,
                78,
                88,
                23
        );
        return new MonthlyReportResponse(
                2026,
                7,
                new MonthlyReportResponse.MoodType(
                        3L,
                        "FRESH_SPARK",
                        "상큼주의자",
                        "톡 쏘는 산미와 청량함을 좋아하는 타입",
                        "https://moodtail.s3.ap-northeast-2.amazonaws.com/mood-types/fresh.png"
                ),
                List.of(),
                profile,
                scores,
                null,
                null,
                frequentCocktails,
                new MonthlyReportResponse.Activity(8, 5)
        );
    }

    private MonthlyReportResponse.FrequentCocktail cocktail(Long id, String name, int ranking) {
        return new MonthlyReportResponse.FrequentCocktail(
                id,
                name,
                name,
                "칵테일 설명",
                null,
                3,
                ranking
        );
    }
}
