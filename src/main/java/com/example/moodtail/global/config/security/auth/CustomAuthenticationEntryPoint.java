package com.example.moodtail.global.config.security.auth;

import com.example.moodtail.global.common.exception.code.BaseCodeDto;
import com.example.moodtail.global.common.exception.code.BaseCodeInterface;
import com.example.moodtail.global.common.exception.code.status.AuthErrorStatus;
import com.example.moodtail.global.common.exception.code.status.GlobalErrorStatus;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

	private final RequestMappingHandlerMapping handlerMapping;

	public CustomAuthenticationEntryPoint(
		@Qualifier("requestMappingHandlerMapping") RequestMappingHandlerMapping handlerMapping
	) {
		this.handlerMapping = handlerMapping;
	}

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
	                    AuthenticationException authException) throws IOException {
		BaseCodeInterface errorStatus = resolveErrorStatus(request);
		BaseCodeDto code = errorStatus.getCode();

		response.setStatus(code.getHttpStatus().value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding(StandardCharsets.UTF_8.name());
		response.getWriter().write(String.format(
			"{\"timestamp\": \"%s\", \"code\": \"%s\", \"message\": \"%s\"}",
			LocalDateTime.now(),
			code.getCode(),
			code.getMessage()
		));
	}

	private BaseCodeInterface resolveErrorStatus(HttpServletRequest request) {
		HandlerMatchStatus matchStatus = detectHandler(request);
		if (matchStatus == HandlerMatchStatus.NOT_FOUND) {
			return GlobalErrorStatus._NOT_FOUND;
		}
		if (matchStatus == HandlerMatchStatus.METHOD_NOT_SUPPORTED) {
			return GlobalErrorStatus._BAD_REQUEST;
		}
		String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
		if (!StringUtils.hasText(authorization)) {
			return GlobalErrorStatus._UNAUTHORIZED;
		}
		return AuthErrorStatus.INVALID_ACCESS_TOKEN;
	}

	private HandlerMatchStatus detectHandler(HttpServletRequest request) {
		try {
			HandlerExecutionChain handler = handlerMapping.getHandler(request);
			return handler != null ? HandlerMatchStatus.MATCHED : HandlerMatchStatus.NOT_FOUND;
		} catch (HttpRequestMethodNotSupportedException ex) {
			return HandlerMatchStatus.METHOD_NOT_SUPPORTED;
		} catch (Exception ex) {
			return HandlerMatchStatus.MATCHED;
		}
	}

	private enum HandlerMatchStatus {
		MATCHED,
		NOT_FOUND,
		METHOD_NOT_SUPPORTED
	}
}
