package io.github.buildblocks.apiresponse.handler;

import io.github.buildblocks.apiresponse.exception.ApiException;
import io.github.buildblocks.apiresponse.exception.ErrorCode;
import io.github.buildblocks.apiresponse.filter.RequestIdFilter;
import io.github.buildblocks.apiresponse.response.ApiError;
import io.github.buildblocks.apiresponse.response.ApiErrorResponse;
import io.github.buildblocks.apiresponse.response.FieldError;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

        private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiErrorResponse> handleApiException(
            ApiException exception,
            HttpServletRequest request) {

        HttpStatus status = getHttpStatus(exception);

        ApiError error = new ApiError(
                exception.getErrorCode().name(),
                exception.getMessage(),
                Instant.now(),
                getRequestId(request)
        );

        ApiErrorResponse response = new ApiErrorResponse(
                false,
                error,
                request.getRequestURI(),
                null
        );

        return ResponseEntity.status(status).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationException(
            MethodArgumentNotValidException exception,
            HttpServletRequest request) {

        List<FieldError> fieldErrors = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> new FieldError(
                        error.getField(),
                        error.getDefaultMessage() == null
                                ? "Invalid value"
                                : error.getDefaultMessage()
                ))
                .toList();

        ApiError error = new ApiError(
                ErrorCode.VALIDATION_FAILED.name(),
                "Request validation failed",
                Instant.now(),
                getRequestId(request)
        );

        ApiErrorResponse response = new ApiErrorResponse(
                false,
                error,
                request.getRequestURI(),
                fieldErrors
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpectedException(
            Exception exception,
            HttpServletRequest request) {

                log.error("Unexpected error on {} (requestId={})",
                request.getRequestURI(), getRequestId(request), exception);

        ApiError error = new ApiError(
                ErrorCode.INTERNAL_SERVER_ERROR.name(),
                "An unexpected error occurred",
                Instant.now(),
                getRequestId(request)
        );

        ApiErrorResponse response = new ApiErrorResponse(
                false,
                error,
                request.getRequestURI(),
                null
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }

    private String getRequestId(HttpServletRequest request) {
        Object requestId = request.getAttribute(
                RequestIdFilter.REQUEST_ID_ATTRIBUTE
        );

        return requestId instanceof String ? (String) requestId : null;
    }

    private HttpStatus getHttpStatus(ApiException exception) {
        Map<ErrorCode, HttpStatus> statusMap = Map.of(
                ErrorCode.RESOURCE_NOT_FOUND, HttpStatus.NOT_FOUND,
                ErrorCode.INVALID_REQUEST, HttpStatus.BAD_REQUEST,
                ErrorCode.VALIDATION_FAILED, HttpStatus.BAD_REQUEST,
                ErrorCode.UNAUTHORIZED, HttpStatus.UNAUTHORIZED,
                ErrorCode.FORBIDDEN, HttpStatus.FORBIDDEN,
                ErrorCode.METHOD_NOT_ALLOWED, HttpStatus.METHOD_NOT_ALLOWED,
                ErrorCode.CONFLICT, HttpStatus.CONFLICT,
                ErrorCode.DATABASE_ERROR, HttpStatus.INTERNAL_SERVER_ERROR
        );

        return statusMap.getOrDefault(
                exception.getErrorCode(),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }
}