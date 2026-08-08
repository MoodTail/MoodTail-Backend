package com.example.moodtail.global.config.security;

import com.example.moodtail.domain.user.repository.UserRepository;
import com.example.moodtail.global.config.security.auth.CustomAccessDeniedHandler;
import com.example.moodtail.global.config.security.auth.CustomAuthenticationEntryPoint;
import com.example.moodtail.global.config.security.jwt.JwtAuthenticationFilter;
import com.example.moodtail.global.config.security.jwt.JwtExceptionFilter;
import com.example.moodtail.global.config.security.jwt.JwtProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.DispatcherType;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private final JwtProvider jwtProvider;
	private final CustomAccessDeniedHandler customAccessDeniedHandler;
	private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
	private final UserRepository userRepository;
	private final ObjectMapper objectMapper;

	@Value("${cors.allowed-origins}")
	private List<String> allowedOrigins;

	@Value("${cors.max-age}")
	private long corsMaxAge;

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		return http
				.csrf(AbstractHttpConfigurer::disable)
				.cors(Customizer.withDefaults())
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.formLogin(AbstractHttpConfigurer::disable)
				.httpBasic(AbstractHttpConfigurer::disable)
				.authorizeHttpRequests(auth -> auth
						.dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
						.requestMatchers("/error").permitAll()
						.requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
						.requestMatchers(HttpMethod.DELETE, "/api/v1/auth").hasRole("USER")
						.requestMatchers(HttpMethod.POST,
								"/api/v1/auth/guest",
								"/api/v1/auth/oauth-states/*",
								"/api/v1/auth/kakao",
								"/api/v1/auth/google",
								"/api/v1/auth/signup/social",
								"/api/v1/auth/login/local",
								"/api/v1/auth/signup/local",
								"/api/v1/auth/password-reset/codes",
								"/api/v1/auth/password-reset/codes/verify",
								"/api/v1/auth/reissue",
								"/api/v1/auth/logout",
								"/api/v1/weather/current"
						).permitAll()
						.requestMatchers(HttpMethod.GET, "/api/v1/auth/signup/local/email-availability").permitAll()
						.requestMatchers(HttpMethod.PATCH, "/api/v1/auth/password").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/v1/terms").permitAll()
						.requestMatchers(HttpMethod.POST, "/api/v1/inquiries").permitAll()
						.requestMatchers("/api/v1/tests/questions").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/v1/tests/results/share/*").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/v1/share/pair-recommendations/*").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/v1/collections/share/*").permitAll()
						.requestMatchers(HttpMethod.GET, "/share/results/*").permitAll()
						.requestMatchers(HttpMethod.GET, "/share/pair/*").permitAll()
						.requestMatchers(HttpMethod.GET, "/share/collections/*").permitAll()
						.requestMatchers(HttpMethod.POST, "/api/v1/tests/results/share")
								.hasAnyRole("GUEST", "USER")
						.requestMatchers(HttpMethod.POST, "/api/v1/tests/collections/share")
								.hasAnyRole("GUEST", "USER")
						.requestMatchers("/api/v1/tests/results").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/v1/cocktails/favorites")
								.hasAnyRole("USER", "ADMIN")
						.requestMatchers(
								HttpMethod.GET,
								"/api/v1/cocktails",
								"/api/v1/cocktails/*"
						).permitAll()
						.requestMatchers("/api/v1/history/**", "/api/v1/reports/**").hasRole("USER")
						.requestMatchers(
								"/actuator/health",
								"/actuator/health/**",
								"/actuator/prometheus"
						).permitAll()
						.anyRequest().hasAnyRole("USER", "ADMIN")
				)
				.exceptionHandling(exceptionHandling -> exceptionHandling
						.accessDeniedHandler(customAccessDeniedHandler)
						.authenticationEntryPoint(customAuthenticationEntryPoint)
				)
				.addFilterBefore(new JwtExceptionFilter(objectMapper), LogoutFilter.class)
				.addFilterBefore(new JwtAuthenticationFilter(jwtProvider, userRepository),
						UsernamePasswordAuthenticationFilter.class)
				.build();
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(allowedOrigins);
		configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
		configuration.setAllowedHeaders(List.of("*"));
		configuration.setAllowCredentials(true);
		configuration.setMaxAge(corsMaxAge);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}
}
