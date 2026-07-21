package com.example.moodtail.domain.report.controller;

import com.example.moodtail.domain.report.dto.response.MonthlyReportResponse;
import com.example.moodtail.domain.report.dto.response.MonthlyReportShareImageResponse;
import com.example.moodtail.domain.report.service.MonthlyReportService;
import com.example.moodtail.domain.report.service.MonthlyReportShareImageService;
import com.example.moodtail.domain.user.entity.UserRole;
import com.example.moodtail.global.common.exception.ExceptionAdvice;
import com.example.moodtail.global.config.security.auth.PrincipalDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class MonthlyReportControllerTest {

    private static final Long USER_ID = 1L;

    @Mock
    private MonthlyReportService monthlyReportService;
    @Mock
    private MonthlyReportShareImageService shareImageService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        PrincipalDetails principal = new PrincipalDetails(USER_ID, UserRole.USER);
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.getObjectMapper().findAndRegisterModules();
        mockMvc = MockMvcBuilders.standaloneSetup(
                        new MonthlyReportController(monthlyReportService, shareImageService)
                )
                .setControllerAdvice(new ExceptionAdvice())
                .setCustomArgumentResolvers(new FixedPrincipalResolver(principal))
                .setMessageConverters(new StringHttpMessageConverter(StandardCharsets.UTF_8), converter)
                .build();
    }

    @Test
    void routesMonthlyReportQueryParameters() throws Exception {
        when(monthlyReportService.getMonthlyReport(USER_ID, 2026, 7)).thenReturn(report());

        mockMvc.perform(get("/api/v1/reports/monthly").param("year", "2026").param("month", "7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.year").value(2026))
                .andExpect(jsonPath("$.result.activity.testCount").value(5));

        verify(monthlyReportService).getMonthlyReport(USER_ID, 2026, 7);
    }

    @Test
    void routesMonthlyReportShareImageUsingTheSpecifiedContract() throws Exception {
        MockMultipartFile image = new MockMultipartFile(
                "image",
                "monthly-report.png",
                "image/png",
                new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47}
        );
        when(shareImageService.uploadShareImage(eq(USER_ID), eq(2026), eq(7), any())).thenReturn(
                new MonthlyReportShareImageResponse("https://cdn.example/report.png")
        );

        mockMvc.perform(multipart("/api/v1/reports/monthly/share-image")
                        .file(image)
                        .param("year", "2026")
                        .param("month", "7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.shareImageUrl").value("https://cdn.example/report.png"));

        verify(shareImageService).uploadShareImage(eq(USER_ID), eq(2026), eq(7), any());
    }

    @Test
    void rejectsMonthlyReportShareRequestWithoutImage() throws Exception {
        mockMvc.perform(multipart("/api/v1/reports/monthly/share-image")
                        .param("year", "2026")
                        .param("month", "7"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsMissingRequiredMonthParameter() throws Exception {
        mockMvc.perform(get("/api/v1/reports/monthly").param("year", "2026"))
                .andExpect(status().isBadRequest());
    }

    private MonthlyReportResponse report() {
        return new MonthlyReportResponse(
                2026,
                7,
                new MonthlyReportResponse.MoodType(3L, "FRESH", "상큼", "설명", null),
                List.of(),
                null,
                null,
                null,
                null,
                List.of(),
                new MonthlyReportResponse.Activity(5, 2)
        );
    }

    private static class FixedPrincipalResolver implements HandlerMethodArgumentResolver {

        private final PrincipalDetails principal;

        private FixedPrincipalResolver(PrincipalDetails principal) {
            this.principal = principal;
        }

        @Override
        public boolean supportsParameter(MethodParameter parameter) {
            return parameter.getParameterType() == PrincipalDetails.class;
        }

        @Override
        public Object resolveArgument(
                MethodParameter parameter,
                ModelAndViewContainer mavContainer,
                NativeWebRequest webRequest,
                org.springframework.web.bind.support.WebDataBinderFactory binderFactory
        ) {
            return principal;
        }
    }
}
