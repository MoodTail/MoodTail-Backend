package com.example.moodtail.domain.report.controller;

import com.example.moodtail.domain.report.dto.response.MonthlyReportSharedImageResponse;
import com.example.moodtail.domain.report.service.MonthlyReportShareImageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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
                        "https://cdn.example/monthly-report.png"
                ));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(
                new MonthlyReportShareController(shareImageService)
        ).build();

        mockMvc.perform(get("/api/v1/reports/monthly/shares/mr_test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.year").value(2026))
                .andExpect(jsonPath("$.result.month").value(7))
                .andExpect(jsonPath("$.result.shareImageUrl")
                        .value("https://cdn.example/monthly-report.png"));

        verify(shareImageService).getSharedImage("mr_test");
    }
}
