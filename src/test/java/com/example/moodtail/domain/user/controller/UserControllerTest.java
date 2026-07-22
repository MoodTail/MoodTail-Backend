package com.example.moodtail.domain.user.controller;

import com.example.moodtail.domain.collection.dto.response.MoodTypesResponse;
import com.example.moodtail.domain.collection.service.MoodTypeCollectionService;
import com.example.moodtail.domain.user.dto.request.UserProfileUpdateRequest;
import com.example.moodtail.domain.user.dto.response.MyPageResponse;
import com.example.moodtail.domain.user.dto.response.UserProfileUpdateResponse;
import com.example.moodtail.domain.user.entity.UserRole;
import com.example.moodtail.domain.user.service.InviteCodeService;
import com.example.moodtail.domain.user.service.MyPageService;
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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    private static final Long USER_ID = 1L;
    private static final String ROLE = UserRole.USER.name();

    @Mock
    private MyPageService myPageService;
    @Mock
    private MoodTypeCollectionService moodTypeCollectionService;
    @Mock
    private InviteCodeService inviteCodeService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.getObjectMapper().findAndRegisterModules();
        mockMvc = MockMvcBuilders.standaloneSetup(
                        new UserController(myPageService, moodTypeCollectionService, inviteCodeService)
                )
                .setControllerAdvice(new ExceptionAdvice())
                .setCustomArgumentResolvers(new FixedPrincipalResolver(
                        new PrincipalDetails(USER_ID, UserRole.USER)
                ))
                .setMessageConverters(converter)
                .build();
    }

    @Test
    void getMyPageReturnsProfileAndActivityCounts() throws Exception {
        when(myPageService.getMyPage(USER_ID, ROLE)).thenReturn(new MyPageResponse(
                USER_ID,
                "푸미",
                new MyPageResponse.RepresentativeMoodTypeResponse(
                        2001L, "TYPE01", "몽글몽글 낭만파", "https://cdn.example/type01.png"
                ),
                12L,
                4L,
                3L
        ));

        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.userId").value(USER_ID))
                .andExpect(jsonPath("$.result.nickname").value("푸미"))
                .andExpect(jsonPath("$.result.totalTestCount").value(12))
                .andExpect(jsonPath("$.result.representativeMoodType.moodTypeId").value(2001));

        verify(myPageService).getMyPage(USER_ID, ROLE);
    }

    @Test
    void getMoodTypesReturnsLockedAndUnlockedTypes() throws Exception {
        when(moodTypeCollectionService.getMoodTypes(USER_ID, ROLE))
                .thenReturn(new MoodTypesResponse(0, List.of()));

        mockMvc.perform(get("/api/v1/users/me/mood-types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.totalCount").value(0))
                .andExpect(jsonPath("$.result.moodTypes").isArray());

        verify(moodTypeCollectionService).getMoodTypes(USER_ID, ROLE);
    }

    @Test
    void updateProfilePassesNicknameAndRepresentativeMoodType() throws Exception {
        when(myPageService.updateProfile(eq(USER_ID), eq(ROLE), any(UserProfileUpdateRequest.class)))
                .thenReturn(new UserProfileUpdateResponse(
                        USER_ID,
                        "새로운푸미",
                        new UserProfileUpdateResponse.RepresentativeMoodTypeResponse(
                                2001L, "TYPE01", "몽글몽글 낭만파", "https://cdn.example/type01.png"
                        )
                ));

        mockMvc.perform(patch("/api/v1/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nickname\":\"새로운푸미\",\"representativeMoodTypeId\":2001}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.nickname").value("새로운푸미"))
                .andExpect(jsonPath("$.result.representativeMoodType.moodTypeId").value(2001));

        ArgumentCaptor<UserProfileUpdateRequest> captor =
                ArgumentCaptor.forClass(UserProfileUpdateRequest.class);
        verify(myPageService).updateProfile(eq(USER_ID), eq(ROLE), captor.capture());
        assertThat(captor.getValue().nickname()).isEqualTo("새로운푸미");
        assertThat(captor.getValue().representativeMoodTypeId()).isEqualTo(2001L);
        verifyNoInteractions(inviteCodeService);
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
