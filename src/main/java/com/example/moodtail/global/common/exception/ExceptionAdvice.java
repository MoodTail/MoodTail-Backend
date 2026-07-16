package com.example.moodtail.global.common.exception;

import com.example.moodtail.global.common.base.BaseResponse;
import com.example.moodtail.global.common.exception.code.BaseCodeDto;
import com.example.moodtail.global.common.exception.code.status.GlobalErrorStatus;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import static com.example.moodtail.global.common.exception.code.status.ImageErrorStatus.IMAGE_TOO_LARGE;

@Slf4j
@RestControllerAdvice(annotations = {RestController.class})
public class ExceptionAdvice extends ResponseEntityExceptionHandler {

    @ExceptionHandler(RestApiException.class)
    public ResponseEntity<BaseResponse<String>> handleRestApiException(RestApiException e) {
        return handleExceptionInternal(e.getErrorCode());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse<String>> handleException(Exception e) {
        log.error("Unhandled exception", e);
        return handleExceptionInternal(GlobalErrorStatus._INTERNAL_SERVER_ERROR.getCode());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<BaseResponse<String>> handleConstraintViolationException(ConstraintViolationException e) {
        return handleExceptionInternal(GlobalErrorStatus._VALIDATION_ERROR.getCode());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<BaseResponse<String>> handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException e
    ) {
        return handleExceptionInternal(GlobalErrorStatus._METHOD_ARGUMENT_ERROR.getCode());
    }

    @Override
    protected ResponseEntity<Object> handleMaxUploadSizeExceededException(
            MaxUploadSizeExceededException e,
            HttpHeaders headers,
            HttpStatusCode statusCode,
            WebRequest request
    ) {
        BaseCodeDto errorCode = IMAGE_TOO_LARGE.getCode();
        return ResponseEntity
                .status(errorCode.getHttpStatus().value())
                .body(BaseResponse.onFailure(errorCode.getCode(), errorCode.getMessage(), null));
    }

    @Override
    public ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException e,
            HttpHeaders headers,
            HttpStatusCode statusCode,
            WebRequest request
    ) {
        Map<String, String> errors = new LinkedHashMap<>();

        e.getBindingResult().getFieldErrors().forEach(fieldError -> {
            String fieldName = fieldError.getField();
            String errorMessage = Optional.ofNullable(fieldError.getDefaultMessage()).orElse("");
            errors.merge(fieldName, errorMessage,
                    (existingErrorMessage, newErrorMessage) -> existingErrorMessage + ", " + newErrorMessage);
        });

        log.warn("Validation Error: {}", errors);
        return handleExceptionInternalArgs(GlobalErrorStatus._VALIDATION_ERROR.getCode(), errors);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException e,
            HttpHeaders headers,
            HttpStatusCode statusCode,
            WebRequest request
    ) {
        return ResponseEntity
                .status(GlobalErrorStatus._REQUEST_FORMAT_ERROR.getHttpStatus())
                .body(BaseResponse.onFailure(
                        GlobalErrorStatus._REQUEST_FORMAT_ERROR.getCode().getCode(),
                        GlobalErrorStatus._REQUEST_FORMAT_ERROR.getMessage(),
                        null
                ));
    }

    private ResponseEntity<BaseResponse<String>> handleExceptionInternal(BaseCodeDto errorCode) {
        return ResponseEntity
                .status(errorCode.getHttpStatus().value())
                .body(BaseResponse.onFailure(errorCode.getCode(), errorCode.getMessage(), null));
    }

    private ResponseEntity<Object> handleExceptionInternalArgs(BaseCodeDto errorCode, Map<String, String> errorArgs) {
        return ResponseEntity
                .status(errorCode.getHttpStatus().value())
                .body(BaseResponse.onFailure(errorCode.getCode(), errorCode.getMessage(), errorArgs));
    }

}
