package com.example.moodtail.global.config.security;

import com.example.moodtail.domain.user.repository.UserRepository;
import com.example.moodtail.global.config.security.auth.CustomAccessDeniedHandler;
import com.example.moodtail.global.config.security.auth.CustomAuthenticationEntryPoint;
import com.example.moodtail.global.config.security.jwt.JwtAuthenticationFilter;
import com.example.moodtail.global.config.security.jwt.JwtExceptionFilter;
import com.example.moodtail.global.config.security.jwt.JwtProvider;
import com.example.moodtail.global.token.repository.redis.RedisRepository;
import jakarta.servlet.DispatcherType;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

	private final JwtProvider jwtProvider;
	private final RedisRepository redisRepository;
	private final CustomAccessDeniedHandler customAccessDeniedHandler;
	private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
	private final UserRepository userRepository;

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
						.requestMatchers("/api/v1/auth/oauth-states/**").hasRole("GUEST")
						.requestMatchers(
								"/api/v1/auth/guest",
								"/api/v1/auth/signup/**",
								"/api/v1/auth/login/**",
								"/api/v1/auth/password-reset/**",
								"/api/v1/auth/password",
								"/api/v1/auth/reissue",
								"/api/v1/auth/logout",
								"/api/v1/weather/current"
						).permitAll()
						.requestMatchers(HttpMethod.GET, "/api/v1/terms").permitAll()
						.requestMatchers(HttpMethod.POST, "/api/v1/inquiries").permitAll()
						.requestMatchers("/api/v1/tests/questions").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/v1/tests/results/share/*").permitAll()
						.requestMatchers(HttpMethod.GET, "/share/results/*").permitAll()
						.requestMatchers(HttpMethod.POST, "/api/v1/tests/results/share")
						.hasAnyRole("GUEST", "USER")
						.requestMatchers("/api/v1/tests/results").permitAll()
						.requestMatchers("/api/v1/cocktails/*", "/api/v1/cocktails").permitAll()
						.requestMatchers("/api/v1//recommends/pair").permitAll()
						.requestMatchers("/actuator/health", "/actuator/health/**", "/actuator/info").permitAll()
						.anyRequest().authenticated()
				)
				.exceptionHandling(exceptionHandling -> exceptionHandling
						.accessDeniedHandler(customAccessDeniedHandler)
						.authenticationEntryPoint(customAuthenticationEntryPoint)
				)
				.addFilterBefore(new JwtExceptionFilter(), LogoutFilter.class)
				.addFilterBefore(new JwtAuthenticationFilter(jwtProvider, redisRepository, userRepository),
						UsernamePasswordAuthenticationFilter.class)
				.build();
	}

	@Bean
	public UserDetailsService userDetailsService() {
		return username -> {
			throw new UsernameNotFoundException(username);
		};
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
