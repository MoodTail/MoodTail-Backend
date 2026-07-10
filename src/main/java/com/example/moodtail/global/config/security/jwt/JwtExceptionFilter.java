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
public class JwtExceptionFilter extends OncePerRequestFilter {

	//JwtExceptionFilter는 발생하는
	//RestApiException을 처리하여 JSON 형식으로 에러 응답을 반환

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
		response.setStatus(exception.getErrorCode().getHttpStatus().value());

		ObjectMapper mapper = new ObjectMapper();
		mapper.findAndRegisterModules();
		mapper.writeValue(
				response.getOutputStream(),
				BaseResponse.onFailure(
						exception.getErrorCode().getCode(),
						exception.getErrorCode().getMessage(),
						null
				)
		);
	}
}
