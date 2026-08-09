package com.example.moodtail.global.config.security;

import com.example.moodtail.domain.user.repository.UserRepository;
import com.example.moodtail.global.config.security.auth.CustomAccessDeniedHandler;
import com.example.moodtail.global.config.security.auth.CustomAuthenticationEntryPoint;
import com.example.moodtail.global.config.security.jwt.JwtProvider;
import com.example.moodtail.global.token.repository.redis.RedisRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HistoryReportSecurityConfigTest.ProbeController.class)
@Import({
        SecurityConfig.class,
        CustomAccessDeniedHandler.class,
        CustomAuthenticationEntryPoint.class,
        HistoryReportSecurityConfigTest.ProbeController.class
})
@TestPropertySource(properties = {
        "cors.allowed-origins=https://app.moodtail.example",
        "cors.max-age=3600"
})
class HistoryReportSecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtProvider jwtProvider;

    @MockitoBean
    private RedisRepository redisRepository;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    @WithMockUser(roles = "GUEST")
    void guestCannotUseHistoryApi() throws Exception {
        mockMvc.perform(get("/api/v1/history/probe"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "GUEST")
    void guestCannotUseReportApi() throws Exception {
        mockMvc.perform(get("/api/v1/reports/probe"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void memberCanUseHistoryAndReportApis() throws Exception {
        mockMvc.perform(get("/api/v1/history/probe"))
                .andExpect(status().isOk())
                .andExpect(content().string("history"));

        mockMvc.perform(get("/api/v1/reports/probe"))
                .andExpect(status().isOk())
                .andExpect(content().string("report"));
    }

    @Test
    void monthlyReportShareReadsArePublic() throws Exception {
        mockMvc.perform(get("/api/v1/reports/monthly/shares/mr_test"))
                .andExpect(status().isOk())
                .andExpect(content().string("shared-report"));

        mockMvc.perform(get("/api/v1/reports/monthly/shares/mr_test/image"))
                .andExpect(status().isOk())
                .andExpect(content().string("shared-report-image"));

        mockMvc.perform(get("/share/reports/monthly/mr_test"))
                .andExpect(status().isOk())
                .andExpect(content().string("share-page"));
    }

    @Test
    @WithMockUser(roles = "GUEST")
    void publicShareReadsDoNotOpenMonthlyReportWrites() throws Exception {
        mockMvc.perform(multipart("/api/v1/reports/monthly/share-image"))
                .andExpect(status().isForbidden());
    }

    @RestController
    static class ProbeController {

        @GetMapping("/api/v1/history/probe")
        String history() {
            return "history";
        }

        @GetMapping("/api/v1/reports/probe")
        String report() {
            return "report";
        }

        @GetMapping("/api/v1/reports/monthly/shares/{shareToken}")
        String sharedReport() {
            return "shared-report";
        }

        @GetMapping("/api/v1/reports/monthly/shares/{shareToken}/image")
        String sharedReportImage() {
            return "shared-report-image";
        }

        @GetMapping("/share/reports/monthly/{shareToken}")
        String sharePage() {
            return "share-page";
        }
    }
}
