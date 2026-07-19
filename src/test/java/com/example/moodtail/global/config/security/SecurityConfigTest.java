package com.example.moodtail.global.config.security;

import com.example.moodtail.domain.user.entity.User;
import com.example.moodtail.domain.user.entity.UserRole;
import com.example.moodtail.domain.user.repository.UserRepository;
import com.example.moodtail.global.config.security.auth.CustomAccessDeniedHandler;
import com.example.moodtail.global.config.security.auth.CustomAuthenticationEntryPoint;
import com.example.moodtail.global.config.security.jwt.JwtProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SecurityConfigTest.ProbeController.class)
@Import({
        SecurityConfig.class,
        CustomAccessDeniedHandler.class,
        CustomAuthenticationEntryPoint.class,
        SecurityConfigTest.ProbeController.class
})
@TestPropertySource(properties = {
        "cors.allowed-origins=https://app.moodtail.example",
        "cors.max-age=3600"
})
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtProvider jwtProvider;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @BeforeEach
    void setUpAuthentication() {
        User guest = User.createGuest(
                "b8e2b515-76f0-4a6b-a94f-8a85f6b5bc7d",
                "게스트",
                LocalDateTime.now()
        );
        ReflectionTestUtils.setField(guest, "id", 7L);
        User member = User.createMember("회원", LocalDateTime.now());
        ReflectionTestUtils.setField(member, "id", 8L);

        when(jwtProvider.validateAccessTokenAndGetClaims("guest-token"))
                .thenReturn(Optional.of(accessClaims("7", UserRole.GUEST)));
        when(jwtProvider.validateAccessTokenAndGetClaims("member-token"))
                .thenReturn(Optional.of(accessClaims("8", UserRole.USER)));
        when(userRepository.findAuthUserById(7L)).thenReturn(Optional.of(guest));
        when(userRepository.findAuthUserById(8L)).thenReturn(Optional.of(member));
    }

    @Test
    void onlyMemberCanWithdrawAccount() throws Exception {
        assertGuestBlocked(delete("/api/v1/auth"));

        mockMvc.perform(delete("/api/v1/auth")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer member-token"))
                .andExpect(status().isOk())
                .andExpect(content().string("ok"));
    }

    @Test
    void guestCannotUseUnlistedMemberApi() throws Exception {
        assertGuestBlocked(get("/api/v1/protected"));

        mockMvc.perform(get("/api/v1/protected")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer member-token"))
                .andExpect(status().isOk())
                .andExpect(content().string("ok"));
    }

    @Test
    void guestCannotUseFavoritesDespitePublicCocktailDetailMatcher() throws Exception {
        assertGuestBlocked(get("/api/v1/cocktails/favorites"));
    }

    private void assertGuestBlocked(
            org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder request
    ) throws Exception {
        mockMvc.perform(request
                        .header(HttpHeaders.AUTHORIZATION, "Bearer guest-token")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("AUTH027"))
                .andExpect(jsonPath("$.message").value("기록을 저장하려면 로그인하세요"));
    }

    private Claims accessClaims(String subject, UserRole role) {
        Claims claims = Jwts.claims().setSubject(subject).setId("access-jti");
        claims.put("role", role.name());
        claims.put("tokenType", "ACCESS");
        return claims;
    }

    @RestController
    public static class ProbeController {

        @DeleteMapping("/api/v1/auth")
        String deleteProbe() {
            return "ok";
        }

        @GetMapping("/api/v1/protected")
        String protectedProbe() {
            return "ok";
        }

        @GetMapping("/api/v1/cocktails/favorites")
        String favoritesProbe() {
            return "ok";
        }
    }
}
