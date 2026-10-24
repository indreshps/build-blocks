package io.github.buildblocks.securityauth.handler;

import java.time.Instant;
import java.util.List;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import io.github.buildblocks.apiresponse.exception.ErrorCode;
import io.github.buildblocks.apiresponse.filter.RequestIdFilter;
import io.github.buildblocks.apiresponse.response.ApiError;
import io.github.buildblocks.apiresponse.response.ApiErrorResponse;
import io.github.buildblocks.securityauth.exception.InvalidCredentialsException;
import io.github.buildblocks.securityauth.exception.UsernameAlreadyExistsException;
import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class AuthExceptionHandler {

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidCredentials(
            InvalidCredentialsException ex, HttpServletRequest request) {
        return build(HttpStatus.UNAUTHORIZED, ErrorCode.UNAUTHORIZED, ex.getMessage(), request);
    }

    @ExceptionHandler(UsernameAlreadyExistsException.class)
    public ResponseEntity<ApiErrorResponse> handleUsernameExists(
            UsernameAlreadyExistsException ex, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, ErrorCode.CONFLICT, ex.getMessage(), request);
    }

    private ResponseEntity<ApiErrorResponse> build(
            HttpStatus status, ErrorCode code, String message, HttpServletRequest request) {

        Object attribute = request.getAttribute(RequestIdFilter.REQUEST_ID_ATTRIBUTE);
        String requestId = attribute instanceof String s ? s : null;

        ApiError error = new ApiError(code.name(), message, Instant.now(), requestId);
        ApiErrorResponse body = new ApiErrorResponse(false, error, request.getRequestURI(), List.of());

        return ResponseEntity.status(status).body(body);
    }
}