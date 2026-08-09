package com.example.moodtail.domain.report.controller;

import com.example.moodtail.domain.report.dto.response.MonthlyReportSharePageResponse;
import com.example.moodtail.domain.report.service.MonthlyReportShareImageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MonthlyReportSharePageControllerTest {

    @Mock
    private MonthlyReportShareImageService shareImageService;

    @Test
    void returnsOgMetadataAndFrontendRedirect() {
        MonthlyReportSharePageController controller =
                new MonthlyReportSharePageController(shareImageService);
        when(shareImageService.getSharePage("mr_test"))
                .thenReturn(new MonthlyReportSharePageResponse(
                        "https://mood-tail.site/share/reports/monthly/mr_test",
                        "https://mood-tail.site/reports/monthly/share/mr_test",
                        "https://cdn.example/monthly-report.png"
                ));

        ResponseEntity<String> response = controller.getSharePage("mr_test");

        assertThat(response.getHeaders().getContentType().toString())
                .isEqualTo("text/html;charset=UTF-8");
        assertThat(response.getBody())
                .contains("<meta property=\"og:image\" content=\"https://cdn.example/monthly-report.png\">")
                .contains("<meta property=\"og:url\" content=\"https://mood-tail.site/share/reports/monthly/mr_test\">")
                .contains("<meta http-equiv=\"refresh\" content=\"0;url=https://mood-tail.site/reports/monthly/share/mr_test\">");
    }
}
