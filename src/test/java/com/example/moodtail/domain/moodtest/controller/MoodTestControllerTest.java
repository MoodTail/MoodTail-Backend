package com.example.moodtail.domain.moodtest.controller;

import com.example.moodtail.domain.moodtest.dto.request.MoodTestResultRequest;
import com.example.moodtail.domain.moodtest.dto.response.MoodTestQuestionResponse;
import com.example.moodtail.domain.moodtest.dto.response.MoodTestResultResponse;
import com.example.moodtail.domain.moodtest.service.MoodTestQuestionService;
import com.example.moodtail.domain.moodtest.service.MoodTestResultService;
import com.example.moodtail.global.common.exception.ExceptionAdvice;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class MoodTestControllerTest {

    @Mock
    private MoodTestQuestionService moodTestQuestionService;
    @Mock
    private MoodTestResultService moodTestResultService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.getObjectMapper().findAndRegisterModules();
        mockMvc = MockMvcBuilders.standaloneSetup(
                        new MoodTestController(moodTestQuestionService, moodTestResultService)
                )
                .setControllerAdvice(new ExceptionAdvice())
                .setMessageConverters(converter)
                .build();
    }

    @Test
    void getQuestionsReturnsQuestionCount() throws Exception {
        when(moodTestQuestionService.getQuestions()).thenReturn(
                MoodTestQuestionResponse.builder()
                        .totalCount(7)
                        .questions(List.of())
                        .build()
        );

        mockMvc.perform(get("/api/v1/tests/questions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("COMMON200"))
                .andExpect(jsonPath("$.result.totalCount").value(7));

        verify(moodTestQuestionService).getQuestions();
    }

    @Test
    void calculateResultPassesSevenAnswersToService() throws Exception {
        when(moodTestResultService.calculateResult(org.mockito.ArgumentMatchers.any(MoodTestResultRequest.class)))
                .thenReturn(MoodTestResultResponse.builder()
                        .saved(false)
                        .recommendations(List.of())
                        .build());

        mockMvc.perform(post("/api/v1/tests/results")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"answers":[
                                  {"questionId":1,"optionId":1},
                                  {"questionId":2,"optionId":3},
                                  {"questionId":3,"optionId":5},
                                  {"questionId":4,"optionId":7},
                                  {"questionId":5,"optionId":9},
                                  {"questionId":11,"optionId":21},
                                  {"questionId":14,"optionId":27}
                                ]}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.saved").value(false))
                .andExpect(jsonPath("$.result.recommendations").isArray());

        ArgumentCaptor<MoodTestResultRequest> captor = ArgumentCaptor.forClass(MoodTestResultRequest.class);
        verify(moodTestResultService).calculateResult(captor.capture());
        assertThat(captor.getValue().answers()).hasSize(7);
        assertThat(captor.getValue().answers().get(0).questionId()).isEqualTo(1L);
    }

    @Test
    void calculateResultRejectsEmptyAnswersBeforeCallingService() throws Exception {
        mockMvc.perform(post("/api/v1/tests/results")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"answers\":[]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON402"));

        verify(moodTestResultService, never()).calculateResult(
                org.mockito.ArgumentMatchers.any(MoodTestResultRequest.class)
        );
    }
}
