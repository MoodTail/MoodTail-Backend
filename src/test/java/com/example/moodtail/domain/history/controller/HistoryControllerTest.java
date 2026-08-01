package com.example.moodtail.domain.history.controller;

import com.example.moodtail.domain.history.dto.request.HistoryCreateRequest;
import com.example.moodtail.domain.history.dto.request.HistoryUpdateRequest;
import com.example.moodtail.domain.history.dto.response.HistoryCalendarResponse;
import com.example.moodtail.domain.history.dto.response.HistoryCreateResponse;
import com.example.moodtail.domain.history.dto.response.HistoryDateResponse;
import com.example.moodtail.domain.history.dto.response.HistoryDetailResponse;
import com.example.moodtail.domain.history.dto.response.HistoryPhotoResponse;
import com.example.moodtail.domain.history.dto.response.HistoryTestResultDetailResponse;
import com.example.moodtail.domain.history.dto.response.HistoryUpdateResponse;
import com.example.moodtail.domain.history.service.HistoryPhotoService;
import com.example.moodtail.domain.history.service.HistoryService;
import com.example.moodtail.domain.user.entity.UserRole;
import com.example.moodtail.global.common.exception.ExceptionAdvice;
import com.example.moodtail.global.common.exception.RestApiException;
import com.example.moodtail.global.config.security.auth.PrincipalDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockPart;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

import static com.example.moodtail.global.common.exception.code.status.HistoryErrorStatus.PHOTO_LIMIT_EXCEEDED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class HistoryControllerTest {

    private static final Long USER_ID = 1L;

    @Mock
    private HistoryService historyService;
    @Mock
    private HistoryPhotoService historyPhotoService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        PrincipalDetails principal = new PrincipalDetails(USER_ID, UserRole.USER);
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.getObjectMapper().findAndRegisterModules();
        converter.getObjectMapper().disable(
                com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS
        );
        mockMvc = MockMvcBuilders.standaloneSetup(new HistoryController(historyService, historyPhotoService))
                .setControllerAdvice(new ExceptionAdvice())
                .setCustomArgumentResolvers(new FixedPrincipalResolver(principal))
                .setMessageConverters(new StringHttpMessageConverter(StandardCharsets.UTF_8), converter)
                .build();
    }

    @Test
    void routesHistoryQueriesUsingSpecificationPaths() throws Exception {
        when(historyService.getCalendar(USER_ID, 2026, 7)).thenReturn(new HistoryCalendarResponse(
                2026,
                7,
                0,
                0,
                5,
                false,
                List.of(),
                List.of(new HistoryCalendarResponse.Day(
                        LocalDate.of(2026, 7, 8),
                        false,
                        false,
                        2L,
                        null
                ))
        ));
        when(historyService.getByDate(USER_ID, "2026-07-05")).thenReturn(new HistoryDateResponse(
                LocalDate.of(2026, 7, 5),
                null,
                List.of(
                        new HistoryDateResponse.DrinkingRecordItem(
                                31L,
                                10L,
                                "모히토",
                                "민트와 라임의 청량한 만남",
                                new BigDecimal("20.0"),
                                "https://cdn.example/mojito.jpg"
                        ),
                        new HistoryDateResponse.DrinkingRecordItem(
                                32L,
                                11L,
                                "네그로니",
                                null,
                                null,
                                "https://cdn.example/negroni.jpg"
                        )
                ),
                List.of()
        ));
        when(historyService.getDetail(USER_ID, 31L)).thenReturn(new HistoryDetailResponse(
                31L, 10L, "모히토", null, LocalDate.of(2026, 7, 5)
        ));
        when(historyService.getTestResultDetail(USER_ID, 10L)).thenReturn(
                new HistoryTestResultDetailResponse(
                        10L,
                        LocalDate.of(2026, 7, 5),
                        null,
                        null,
                        List.of()
                )
        );

        mockMvc.perform(get("/api/v1/history/calendar").param("year", "2026").param("month", "7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.reportRequiredTestCount").value(5))
                .andExpect(jsonPath("$.result.days[0].date").value("2026-07-08"))
                .andExpect(jsonPath("$.result.days[0].hasTestResult").value(false))
                .andExpect(jsonPath("$.result.days[0].hasDrinkingRecord").value(false))
                .andExpect(jsonPath("$.result.days[0].photoCount").value(2));
        mockMvc.perform(get("/api/v1/history/dates/2026-07-05"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.date").value("2026-07-05"))
                .andExpect(jsonPath("$.result.drinkingRecords.length()").value(2))
                .andExpect(jsonPath("$.result.drinkingRecords[0].recordId").value(31))
                .andExpect(jsonPath("$.result.drinkingRecords[0].shortDescription")
                        .value("민트와 라임의 청량한 만남"))
                .andExpect(jsonPath("$.result.drinkingRecords[0].alcoholDegree").value(20.0))
                .andExpect(jsonPath("$.result.drinkingRecords[1].recordId").value(32))
                .andExpect(jsonPath("$.result.drinkingRecords[1].shortDescription").doesNotExist())
                .andExpect(jsonPath("$.result.drinkingRecords[1].alcoholDegree").doesNotExist())
                .andExpect(jsonPath("$.result.drinkingRecord").doesNotExist());
        mockMvc.perform(get("/api/v1/history/drinking-records/31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.recordId").value(31));
        mockMvc.perform(get("/api/v1/history/test-results/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.resultId").value(10));
    }

    @Test
    void routesCreateUpdateAndDeleteUsingSpecificationPaths() throws Exception {
        when(historyService.create(eq(USER_ID), any())).thenReturn(
                List.of(
                        new HistoryCreateResponse(31L, 10L, LocalDate.of(2026, 7, 5)),
                        new HistoryCreateResponse(32L, 11L, LocalDate.of(2026, 7, 5))
                )
        );
        when(historyService.update(eq(USER_ID), eq(31L), any())).thenReturn(
                new HistoryUpdateResponse(31L)
        );

        mockMvc.perform(post("/api/v1/history/drinking-records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cocktailIds": [10, 11],
                                  "recordDate": "2026-07-05"
                                }
                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.length()").value(2))
                .andExpect(jsonPath("$.result[0].recordId").value(31))
                .andExpect(jsonPath("$.result[0].cocktailId").value(10))
                .andExpect(jsonPath("$.result[1].recordId").value(32))
                .andExpect(jsonPath("$.result[1].cocktailId").value(11));
        mockMvc.perform(patch("/api/v1/history/drinking-records/31")
                        .contentType(MediaType.APPLICATION_JSON)
                .content("{\"cocktailId\":11}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.recordId").value(31));
        mockMvc.perform(delete("/api/v1/history/drinking-records/31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("COMMON200"));

        ArgumentCaptor<HistoryCreateRequest> createCaptor =
                ArgumentCaptor.forClass(HistoryCreateRequest.class);
        verify(historyService).create(eq(USER_ID), createCaptor.capture());
        assertThat(createCaptor.getValue().cocktailIds()).containsExactly(10L, 11L);
        assertThat(createCaptor.getValue().recordDate()).isEqualTo(LocalDate.of(2026, 7, 5));

        ArgumentCaptor<HistoryUpdateRequest> updateCaptor =
                ArgumentCaptor.forClass(HistoryUpdateRequest.class);
        verify(historyService).update(eq(USER_ID), eq(31L), updateCaptor.capture());
        assertThat(updateCaptor.getValue().cocktailId()).isEqualTo(11L);
        assertThat(updateCaptor.getValue().recordDate()).isNull();
    }

    @Test
    void rejectsEmptyCocktailIdsBeforeCallingService() throws Exception {
        mockMvc.perform(post("/api/v1/history/drinking-records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cocktailIds": [],
                                  "recordDate": "2026-07-05"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON402"));

        verify(historyService, never()).create(any(), any());
    }

    @Test
    void routesPhotoAddAndDeleteUsingSpecificationPaths() throws Exception {
        MockMultipartFile image = new MockMultipartFile(
                "image",
                "history.jpg",
                "image/jpeg",
                new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF}
        );
        when(historyPhotoService.add(eq(USER_ID), eq("2026-07-05"), any())).thenReturn(
                new HistoryPhotoResponse(
                        3L,
                        LocalDate.of(2026, 7, 5),
                        "https://cdn.example/photo.jpg"
                )
        );
        mockMvc.perform(multipart("/api/v1/history/photos")
                        .file(image)
                        .part(textPart("date", "2026-07-05")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.recordDate").value("2026-07-05"))
                .andExpect(jsonPath("$.result.sourceType").doesNotExist());
        mockMvc.perform(delete("/api/v1/history/dates/2026-07-05/photos/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("COMMON200"));
    }

    @Test
    void returnsConflictWhenDailyPhotoLimitIsExceeded() throws Exception {
        MockMultipartFile image = new MockMultipartFile(
                "image",
                "history.jpg",
                "image/jpeg",
                new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF}
        );
        doThrow(new RestApiException(PHOTO_LIMIT_EXCEEDED))
                .when(historyPhotoService)
                .add(eq(USER_ID), eq("2026-07-05"), any());

        mockMvc.perform(multipart("/api/v1/history/photos")
                        .file(image)
                        .part(textPart("date", "2026-07-05")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("HISTORY_PHOTO409"));
    }

    @Test
    void returnsCommonValidationErrorWhenHistoryPhotoMultipartPartIsMissing() throws Exception {
        MockMultipartFile image = new MockMultipartFile(
                "image",
                "history.jpg",
                "image/jpeg",
                new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF}
        );

        mockMvc.perform(multipart("/api/v1/history/photos")
                        .part(textPart("date", "2026-07-05")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON402"))
                .andExpect(jsonPath("$.message").value("입력값 검증에 실패했습니다."))
                .andExpect(jsonPath("$.result").doesNotExist());

        mockMvc.perform(multipart("/api/v1/history/photos")
                        .file(image))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON402"))
                .andExpect(jsonPath("$.message").value("입력값 검증에 실패했습니다."))
                .andExpect(jsonPath("$.result").doesNotExist());

        verify(historyPhotoService, never()).add(any(), any(), any());
    }

    @Test
    void rejectsInvalidCreateRequestBeforeCallingService() throws Exception {
        mockMvc.perform(post("/api/v1/history/drinking-records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cocktailId\":0}"))
                .andExpect(status().isBadRequest());
    }

    private MockPart textPart(String name, String value) {
        return new MockPart(name, value.getBytes(StandardCharsets.UTF_8));
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
