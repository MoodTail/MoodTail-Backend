package com.example.moodtail.global.config.security.auth;

import com.example.moodtail.global.common.base.BaseResponse;
import com.example.moodtail.global.common.exception.code.BaseCodeDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static com.example.moodtail.global.common.exception.code.status.AuthErrorStatus.INVALID_ROLE;
import static com.example.moodtail.global.common.exception.code.status.AuthErrorStatus.LOGIN_USER_REQUIRED;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

	private final ObjectMapper objectMapper;

	public CustomAccessDeniedHandler(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response,
	                   AccessDeniedException accessDeniedException) throws IOException {
		BaseCodeDto errorCode = isGuest()
				? LOGIN_USER_REQUIRED.getCode()
				: INVALID_ROLE.getCode();
		response.setStatus(errorCode.getHttpStatus().value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding(StandardCharsets.UTF_8.name());
		objectMapper.writeValue(
				response.getWriter(),
				BaseResponse.onFailure(errorCode.getCode(), errorCode.getMessage(), null)
		);
	}

	private boolean isGuest() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		return authentication != null
				&& authentication.getAuthorities().stream()
				.anyMatch(authority -> "ROLE_GUEST".equals(authority.getAuthority()));
	}
}
