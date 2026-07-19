package com.example.moodtail.global.config.security.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CustomAccessDeniedHandlerTest {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
    private final CustomAccessDeniedHandler handler = new CustomAccessDeniedHandler(objectMapper);

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void guestReceivesTheFeatureSpecificationLoginPrompt() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "guest",
                        "",
                        List.of(new SimpleGrantedAuthority("ROLE_GUEST"))
                )
        );
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.handle(
                new MockHttpServletRequest(),
                response,
                new org.springframework.security.access.AccessDeniedException("denied")
        );

        JsonNode body = objectMapper.readTree(response.getContentAsString());
        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(body.path("code").asText()).isEqualTo("AUTH027");
        assertThat(body.path("message").asText()).isEqualTo("기록을 저장하려면 로그인하세요");
    }

    @Test
    void authenticatedMemberWithoutPermissionReceivesGenericRoleError() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "member",
                        "",
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))
                )
        );
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.handle(
                new MockHttpServletRequest(),
                response,
                new org.springframework.security.access.AccessDeniedException("denied")
        );

        JsonNode body = objectMapper.readTree(response.getContentAsString());
        assertThat(body.path("code").asText()).isEqualTo("AUTH009");
    }
}
