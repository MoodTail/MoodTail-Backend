package com.example.moodtail.domain.moodtest.controller;

import com.example.moodtail.domain.moodtest.dto.request.MoodTestResultShareCreateRequest;
import com.example.moodtail.domain.moodtest.dto.response.MoodTestResultResponse;
import com.example.moodtail.domain.moodtest.dto.response.MoodTestResultShareCreateResponse;
import com.example.moodtail.domain.moodtest.service.MoodTestResultShareService;
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
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class MoodTestResultShareControllerTest {

    private static final Long USER_ID = 1L;

    @Mock
    private MoodTestResultShareService moodTestResultShareService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.getObjectMapper().findAndRegisterModules();
        mockMvc = MockMvcBuilders.standaloneSetup(
                        new MoodTestResultShareController(moodTestResultShareService)
                )
                .setControllerAdvice(new ExceptionAdvice())
                .setCustomArgumentResolvers(new FixedPrincipalResolver(
                        new PrincipalDetails(USER_ID, UserRole.USER)
                ))
                .setMessageConverters(converter)
                .build();
    }

    @Test
    void createShareAcceptsJsonRequestPartAndThumbnail() throws Exception {
        when(moodTestResultShareService.createShare(eq(USER_ID), any(), any()))
                .thenReturn(new MoodTestResultShareCreateResponse(
                        "r_test",
                        "https://api.moodtail.com/share/results/r_test"
                ));
        MockMultipartFile request = new MockMultipartFile(
                "request",
                "",
                "application/json",
                validRequest().getBytes(StandardCharsets.UTF_8)
        );
        MockMultipartFile thumbnail = new MockMultipartFile(
                "thumbnail",
                "result.png",
                "image/png",
                new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47}
        );

        mockMvc.perform(multipart("/api/v1/tests/results/share")
                        .file(request)
                        .file(thumbnail))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.shareToken").value("r_test"))
                .andExpect(jsonPath("$.result.shareUrl")
                        .value("https://api.moodtail.com/share/results/r_test"));

        ArgumentCaptor<MoodTestResultShareCreateRequest> requestCaptor =
                ArgumentCaptor.forClass(MoodTestResultShareCreateRequest.class);
        ArgumentCaptor<MultipartFile> fileCaptor = ArgumentCaptor.forClass(MultipartFile.class);
        verify(moodTestResultShareService).createShare(
                eq(USER_ID),
                requestCaptor.capture(),
                fileCaptor.capture()
        );
        assertThat(requestCaptor.getValue().tasteProfile().sweetness()).isEqualByComparingTo("4.0");
        assertThat(fileCaptor.getValue().getOriginalFilename()).isEqualTo("result.png");
    }

    @Test
    void createShareRejectsTasteScoreOutsideRange() throws Exception {
        MockMultipartFile request = new MockMultipartFile(
                "request",
                "",
                "application/json",
                validRequest().replace("\"sweetness\":4.0", "\"sweetness\":6.0")
                        .getBytes(StandardCharsets.UTF_8)
        );
        MockMultipartFile thumbnail = new MockMultipartFile(
                "thumbnail", "result.png", "image/png", new byte[]{1}
        );

        mockMvc.perform(multipart("/api/v1/tests/results/share")
                        .file(request)
                        .file(thumbnail))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON402"));

        verify(moodTestResultShareService, never()).createShare(any(), any(), any());
    }

    @Test
    void getSharedResultUsesShareToken() throws Exception {
        when(moodTestResultShareService.getSharedResult("r_test"))
                .thenReturn(MoodTestResultResponse.builder()
                        .saved(false)
                        .recommendations(List.of())
                        .build());

        mockMvc.perform(get("/api/v1/tests/results/share/r_test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("COMMON200"))
                .andExpect(jsonPath("$.result.saved").value(false));

        verify(moodTestResultShareService).getSharedResult("r_test");
    }

    private String validRequest() {
        return """
                {"tasteProfile":{"alcoholIntensity":2.0,"sweetness":4.0,"sourness":3.0,"refreshing":3.0,"bitterness":2.0}}
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
