package com.example.moodtail.domain.moodtest.controller;

import com.example.moodtail.domain.moodtest.dto.request.MoodTestResultSaveRequest;
import com.example.moodtail.domain.moodtest.service.MoodTestResultSaveService;
import com.example.moodtail.domain.user.entity.UserRole;
import com.example.moodtail.global.common.exception.ExceptionAdvice;
import com.example.moodtail.global.config.security.auth.PrincipalDetails;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class MoodTestResultSaveControllerTest {

    private static final Long USER_ID = 1L;

    @Mock
    private MoodTestResultSaveService moodTestResultSaveService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.getObjectMapper().findAndRegisterModules();
        mockMvc = MockMvcBuilders.standaloneSetup(
                        new MoodTestResultSaveController(moodTestResultSaveService)
                )
                .setControllerAdvice(new ExceptionAdvice())
                .setCustomArgumentResolvers(new FixedPrincipalResolver(
                        new PrincipalDetails(USER_ID, UserRole.USER)
                ))
                .setMessageConverters(converter)
                .build();
    }

    @Test
    void saveResultReturnsSavedResultId() throws Exception {
        when(moodTestResultSaveService.saveResult(
                org.mockito.ArgumentMatchers.eq(USER_ID),
                any(MoodTestResultSaveRequest.class)
        )).thenReturn(10L);

        mockMvc.perform(post("/api/v1/tests/results/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequest()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.code").value("COMMON200"))
                .andExpect(jsonPath("$.result.test_result_id").value(10));

        ArgumentCaptor<MoodTestResultSaveRequest> captor =
                ArgumentCaptor.forClass(MoodTestResultSaveRequest.class);
        verify(moodTestResultSaveService).saveResult(
                org.mockito.ArgumentMatchers.eq(USER_ID),
                captor.capture()
        );
        assertThat(captor.getValue().moodType().moodTypeId()).isEqualTo(2001L);
        assertThat(captor.getValue().recommendedCocktails()).hasSize(4);
    }

    @Test
    void saveResultRejectsRecommendationListWithLessThanFourItems() throws Exception {
        String invalidRequest = """
                {
                  "moodType":{"moodTypeId":2001,"typeCode":"TYPE01"},
                  "tasteProfile":{"alcoholIntensity":2.0,"sweetness":4.0,"sourness":3.0,"refreshing":3.0,"bitterness":2.0},
                  "recommendedCocktails":[
                    {"cocktailId":101,"matchScore":96},
                    {"cocktailId":102,"matchScore":92},
                    {"cocktailId":103,"matchScore":88}
                  ]
                }
                """;

        mockMvc.perform(post("/api/v1/tests/results/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON402"));

        verify(moodTestResultSaveService, never()).saveResult(any(), any());
    }

    private String validRequest() {
        return """
                {
                  "moodType":{"moodTypeId":2001,"typeCode":"TYPE01"},
                  "tasteProfile":{"alcoholIntensity":2.0,"sweetness":4.0,"sourness":3.0,"refreshing":3.0,"bitterness":2.0},
                  "recommendedCocktails":[
                    {"cocktailId":101,"matchScore":96},
                    {"cocktailId":102,"matchScore":92},
                    {"cocktailId":103,"matchScore":88},
                    {"cocktailId":104,"matchScore":84}
                  ]
                }
                """;
    }

    private record FixedPrincipalResolver(PrincipalDetails principal)
            implements HandlerMethodArgumentResolver {

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
