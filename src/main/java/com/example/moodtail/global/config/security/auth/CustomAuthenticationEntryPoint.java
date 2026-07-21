package com.example.moodtail.global.config.security.auth;

import com.example.moodtail.global.common.base.BaseResponse;
import com.example.moodtail.global.common.exception.code.BaseCodeDto;
import com.example.moodtail.global.common.exception.code.status.AuthErrorStatus;
import com.example.moodtail.global.common.exception.code.status.GlobalErrorStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

	private final ObjectMapper objectMapper;

	public CustomAuthenticationEntryPoint(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
	                    AuthenticationException authException) throws IOException {
		BaseCodeDto code = resolveErrorCode(request);

		response.setStatus(code.getHttpStatus().value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding(StandardCharsets.UTF_8.name());
		objectMapper.writeValue(
				response.getWriter(),
				BaseResponse.onFailure(code.getCode(), code.getMessage(), null)
		);
	}

	private BaseCodeDto resolveErrorCode(HttpServletRequest request) {
		String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
		if (!StringUtils.hasText(authorization)) {
			return GlobalErrorStatus._UNAUTHORIZED.getCode();
		}
		return AuthErrorStatus.INVALID_ACCESS_TOKEN.getCode();
	}
}
