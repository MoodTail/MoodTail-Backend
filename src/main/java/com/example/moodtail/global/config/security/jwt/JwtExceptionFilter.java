package com.example.moodtail.global.config.security.jwt;

import com.example.moodtail.global.common.exception.RestApiException;
import com.example.moodtail.global.common.base.BaseResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class JwtExceptionFilter extends OncePerRequestFilter {

	private final ObjectMapper objectMapper;

	public JwtExceptionFilter(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
	                                FilterChain filterChain) throws ServletException, IOException {
		try {
			filterChain.doFilter(request, response);
		} catch (RestApiException exception) {
			setErrorResponse(response, exception);
		}
	}

	private void setErrorResponse(HttpServletResponse response, RestApiException exception) throws IOException {
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding(StandardCharsets.UTF_8.name());
		response.setStatus(exception.getErrorCode().getHttpStatus().value());

		objectMapper.writeValue(
				response.getOutputStream(),
				BaseResponse.onFailure(
						exception.getErrorCode().getCode(),
						exception.getErrorCode().getMessage(),
						null
				)
		);
	}
}
