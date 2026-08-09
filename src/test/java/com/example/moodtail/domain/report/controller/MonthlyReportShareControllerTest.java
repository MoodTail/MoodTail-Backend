package com.example.moodtail.domain.report.controller;

import com.example.moodtail.domain.report.dto.response.MonthlyReportSharedImageFile;
import com.example.moodtail.domain.report.dto.response.MonthlyReportSharedImageResponse;
import com.example.moodtail.domain.report.service.MonthlyReportShareImageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class MonthlyReportShareControllerTest {

    @Mock
    private MonthlyReportShareImageService shareImageService;

    @Test
    void returnsSharedImageByToken() throws Exception {
        when(shareImageService.getSharedImage("mr_test"))
                .thenReturn(new MonthlyReportSharedImageResponse(
                        2026,
                        7,
                        "https://mood-tail.site/api/v1/reports/monthly/shares/mr_test/image"
                ));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(
                new MonthlyReportShareController(shareImageService)
        ).build();

        mockMvc.perform(get("/api/v1/reports/monthly/shares/mr_test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.year").value(2026))
                .andExpect(jsonPath("$.result.month").value(7))
                .andExpect(jsonPath("$.result.shareImageUrl")
                        .value("https://mood-tail.site/api/v1/reports/monthly/shares/mr_test/image"));

        verify(shareImageService).getSharedImage("mr_test");
    }

    @Test
    void returnsSharedImageFileByTokenWithoutExposingStorageUrl() throws Exception {
        when(shareImageService.getSharedImageFile("mr_test"))
                .thenReturn(new MonthlyReportSharedImageFile(
                        new byte[]{1, 2, 3},
                        MediaType.IMAGE_PNG_VALUE
                ));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(
                new MonthlyReportShareController(shareImageService)
        ).build();

        mockMvc.perform(get("/api/v1/reports/monthly/shares/mr_test/image"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_PNG))
                .andExpect(content().bytes(new byte[]{1, 2, 3}))
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"));

        verify(shareImageService).getSharedImageFile("mr_test");
    }
}
