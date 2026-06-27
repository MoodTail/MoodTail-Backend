package com.example.moodtail.global.common.exception;

import com.example.moodtail.global.common.base.BaseResponse;
import com.example.moodtail.global.common.exception.code.status.GlobalErrorStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<BaseResponse<String>> handleNoHandlerFoundException(NoHandlerFoundException ex) {
        return ResponseEntity
                .status(GlobalErrorStatus._NOT_FOUND.getHttpStatus())
                .body(BaseResponse.onFailure(GlobalErrorStatus._NOT_FOUND.getCode().getCode(),
                        GlobalErrorStatus._NOT_FOUND.getMessage(), null)); // HTTP 상태 코드 404 반환
    }
}
