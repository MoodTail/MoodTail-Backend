package com.example.moodtail.domain.moodtest.controller;

import com.example.moodtail.domain.moodtest.dto.response.MoodTestResultSharePageResponse;
import com.example.moodtail.domain.moodtest.service.MoodTestResultShareService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MoodTestResultSharePageControllerTest {

    @Mock
    MoodTestResultShareService moodTestResultShareService;

    @Test
    void returnsOgMetadataAndFrontendRedirect() {
        MoodTestResultSharePageController controller =
                new MoodTestResultSharePageController(moodTestResultShareService);
        when(moodTestResultShareService.getSharePage("r_test"))
                .thenReturn(new MoodTestResultSharePageResponse(
                        "https://api.moodtail.com/share/results/r_test",
                        "https://moodtail.com/share/results/r_test",
                        "https://cdn.moodtail.com/share/result.png"
                ));

        ResponseEntity<String> response = controller.getSharePage("r_test");

        assertThat(response.getHeaders().getContentType().toString())
                .isEqualTo("text/html;charset=UTF-8");
        assertThat(response.getBody())
                .contains("<meta property=\"og:image\" content=\"https://cdn.moodtail.com/share/result.png\">")
                .contains("<meta property=\"og:url\" content=\"https://api.moodtail.com/share/results/r_test\">")
                .contains("<meta http-equiv=\"refresh\" content=\"0;url=https://moodtail.com/share/results/r_test\">");
    }
}
