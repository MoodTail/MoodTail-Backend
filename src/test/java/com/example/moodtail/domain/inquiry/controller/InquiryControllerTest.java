package com.example.moodtail.domain.inquiry.controller;

import com.example.moodtail.domain.inquiry.dto.request.InquiryCreateRequest;
import com.example.moodtail.domain.inquiry.dto.response.InquiryCreateResponse;
import com.example.moodtail.domain.inquiry.entity.InquiryStatus;
import com.example.moodtail.domain.inquiry.service.InquiryService;
import com.example.moodtail.global.common.exception.ExceptionAdvice;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class InquiryControllerTest {

    @Mock
    private InquiryService inquiryService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.getObjectMapper().findAndRegisterModules();
        converter.getObjectMapper().disable(
                com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS
        );
        mockMvc = MockMvcBuilders.standaloneSetup(new InquiryController(inquiryService))
                .setControllerAdvice(new ExceptionAdvice())
                .setCustomArgumentResolvers(new NullPrincipalResolver())
                .setMessageConverters(converter)
                .build();
    }

    @Test
    void createGuestInquiryPassesNullPrincipalAndReturnsCreatedAt() throws Exception {
        when(inquiryService.createInquiry(any(InquiryCreateRequest.class), isNull()))
                .thenReturn(new InquiryCreateResponse(
                        15L,
                        InquiryStatus.PENDING,
                        LocalDateTime.of(2026, 7, 21, 14, 30)
                ));

        mockMvc.perform(post("/api/v1/inquiries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"inquiryType":"BUG","content":"결과 페이지의 이미지가 보이지 않습니다.","contactEmail":"guest@example.com"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.inquiryId").value(15))
                .andExpect(jsonPath("$.result.status").value("PENDING"))
                .andExpect(jsonPath("$.result.createdAt").value("2026-07-21T14:30:00"));

        ArgumentCaptor<InquiryCreateRequest> captor = ArgumentCaptor.forClass(InquiryCreateRequest.class);
        verify(inquiryService).createInquiry(captor.capture(), isNull());
        assertThat(captor.getValue().contactEmail()).isEqualTo("guest@example.com");
    }

    private static class NullPrincipalResolver implements HandlerMethodArgumentResolver {

        @Override
        public boolean supportsParameter(MethodParameter parameter) {
            return parameter.getParameterType()
                    == com.example.moodtail.global.config.security.auth.PrincipalDetails.class;
        }

        @Override
        public Object resolveArgument(
                MethodParameter parameter,
                ModelAndViewContainer mavContainer,
                NativeWebRequest webRequest,
                org.springframework.web.bind.support.WebDataBinderFactory binderFactory
        ) {
            return null;
        }
    }
}
