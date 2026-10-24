package io.github.buildblocks.securityauth.security;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import io.github.buildblocks.apiresponse.exception.ErrorCode;
import io.github.buildblocks.apiresponse.filter.RequestIdFilter;
import io.github.buildblocks.apiresponse.response.ApiError;
import io.github.buildblocks.apiresponse.response.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.ObjectMapper;

public class ApiAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public ApiAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        Object attribute = request.getAttribute(RequestIdFilter.REQUEST_ID_ATTRIBUTE);
        String requestId = attribute instanceof String s ? s : null;

        ApiError error = new ApiError(
                ErrorCode.UNAUTHORIZED.name(),
                "Authentication is required or invalid",
                Instant.now(),
                requestId);

        ApiErrorResponse body = new ApiErrorResponse(false, error, request.getRequestURI(), List.of());

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(), body);
    }
}