package com.example.moodtail.global.config.security.auth;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

import static com.example.moodtail.global.common.exception.code.status.AuthErrorStatus.INVALID_ROLE;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {
	//AccessDeniedHandler 인터페이스를 구현하여 사용자가 권한이 없는 자원에 접근할 경우 요청을 /access/denied 경로로 포워딩
	// accessDeniedException이 발생했을 때만 요청을 포워드하도록 조건을 추가
	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response,
	                   AccessDeniedException accessDeniedException) throws IOException, ServletException {
		response.setStatus(HttpStatus.FORBIDDEN.value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE); // 응답의 Content-Type을 application/json으로 설정
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write(String.format(
			"{\"timestamp\": \"%s\", \"code\": \"%s\", \"message\": \"%s\"}",
			LocalDateTime.now(),
			INVALID_ROLE.getCode().getCode(),
			INVALID_ROLE.getMessage()
		));
	}
}
