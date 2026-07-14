package com.example.moodtail.global.config.security.jwt;

import com.example.moodtail.domain.user.entity.User;
import com.example.moodtail.domain.user.entity.UserRole;
import com.example.moodtail.domain.user.repository.UserRepository;
import com.example.moodtail.global.common.exception.RestApiException;
import com.example.moodtail.global.config.security.auth.PrincipalDetails;
import com.example.moodtail.global.token.repository.redis.RedisRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private RedisRepository redisRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private FilterChain filterChain;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void authenticatesStoredGuestWithGuestAuthority() throws Exception {
        User guest = guestWithId(7L);
        Claims claims = accessClaims("7", UserRole.GUEST);
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtProvider, redisRepository, userRepository);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/auth/oauth-states/kakao");
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(jwtProvider.resolveToken(request)).thenReturn("guest-access-token");
        when(jwtProvider.validateAccessToken("guest-access-token")).thenReturn(true);
        when(jwtProvider.getAccessTokenClaims("guest-access-token")).thenReturn(claims);
        when(userRepository.findById(7L)).thenReturn(Optional.of(guest));

        filter.doFilterInternal(request, response, filterChain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNotNull();
        assertThat(((PrincipalDetails) authentication.getPrincipal()).getUserId()).isEqualTo(7L);
        assertThat(authentication.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_GUEST");
        verify(redisRepository).extendUserTimer(7L);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void rejectsOldGuestTokenAfterGuestWasUpgradedToUser() {
        User upgradedUser = guestWithId(7L);
        upgradedUser.upgradeToUser("회원", LocalDateTime.now());
        Claims claims = accessClaims("7", UserRole.GUEST);
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtProvider, redisRepository, userRepository);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/history");
        when(jwtProvider.resolveToken(request)).thenReturn("old-guest-token");
        when(jwtProvider.validateAccessToken("old-guest-token")).thenReturn(true);
        when(jwtProvider.getAccessTokenClaims("old-guest-token")).thenReturn(claims);
        when(userRepository.findById(7L)).thenReturn(Optional.of(upgradedUser));

        assertThatThrownBy(() -> filter.doFilterInternal(
                request,
                new MockHttpServletResponse(),
                filterChain
        )).isInstanceOfSatisfying(RestApiException.class, exception ->
                assertThat(exception.getErrorCode().getCode()).isEqualTo("AUTH009")
                );
    }

    @Test
    void logoutSkipsAuthenticationSoExpiredSessionsCanClearCookie() throws Exception {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtProvider, redisRepository, userRepository);
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/auth/logout");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtProvider, redisRepository, userRepository);
    }

    @Test
    void activityTimerRedisFailureDoesNotBlockAuthenticatedRequest() throws Exception {
        User guest = guestWithId(7L);
        Claims claims = accessClaims("7", UserRole.GUEST);
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtProvider, redisRepository, userRepository);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/history");
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(jwtProvider.resolveToken(request)).thenReturn("guest-access-token");
        when(jwtProvider.validateAccessToken("guest-access-token")).thenReturn(true);
        when(jwtProvider.getAccessTokenClaims("guest-access-token")).thenReturn(claims);
        when(userRepository.findById(7L)).thenReturn(Optional.of(guest));
        org.mockito.Mockito.doThrow(new RedisConnectionFailureException("redis unavailable"))
                .when(redisRepository).extendUserTimer(7L);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
    }

    private User guestWithId(Long id) {
        User user = User.createGuest(
                "b8e2b515-76f0-4a6b-a94f-8a85f6b5bc7d",
                "게스트",
                LocalDateTime.now()
        );
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private Claims accessClaims(String subject, UserRole role) {
        Claims claims = Jwts.claims().setSubject(subject).setId("access-jti");
        claims.put("role", role.name());
        claims.put("tokenType", "ACCESS");
        return claims;
    }
}
