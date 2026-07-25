package com.example.moodtail.domain.term.controller;

import com.example.moodtail.domain.term.dto.response.TermsResponse;
import com.example.moodtail.domain.term.entity.TermType;
import com.example.moodtail.domain.term.service.TermService;
import com.example.moodtail.global.common.exception.ExceptionAdvice;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TermControllerTest {

    @Mock
    private TermService termService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.getObjectMapper().findAndRegisterModules();
        mockMvc = MockMvcBuilders.standaloneSetup(new TermController(termService))
                .setControllerAdvice(new ExceptionAdvice())
                .setMessageConverters(converter)
                .build();
    }

    @Test
    void getTermsPassesOptionalTermType() throws Exception {
        when(termService.getTerms("SERVICE")).thenReturn(new TermsResponse(List.of(
                new TermsResponse.TermResponse(
                        1L,
                        TermType.SERVICE,
                        "서비스 이용약관",
                        "1.0",
                        true,
                        "약관 내용"
                )
        )));

        mockMvc.perform(get("/api/v1/terms").param("termType", "SERVICE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.terms[0].termId").value(1))
                .andExpect(jsonPath("$.result.terms[0].termType").value("SERVICE"))
                .andExpect(jsonPath("$.result.terms[0].required").value(true));

        verify(termService).getTerms("SERVICE");
    }

    @Test
    void getTermsWithoutTypePassesNull() throws Exception {
        when(termService.getTerms(null)).thenReturn(new TermsResponse(List.of()));

        mockMvc.perform(get("/api/v1/terms"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.terms").isArray());

        verify(termService).getTerms(null);
    }
}
