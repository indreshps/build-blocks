package io.github.buildblocks.securityauth.security;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import io.github.buildblocks.apiresponse.exception.ErrorCode;
import io.github.buildblocks.apiresponse.filter.RequestIdFilter;
import io.github.buildblocks.apiresponse.response.ApiError;
import io.github.buildblocks.apiresponse.response.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.ObjectMapper;

public class ApiAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    public ApiAccessDeniedHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {

        Object attribute = request.getAttribute(RequestIdFilter.REQUEST_ID_ATTRIBUTE);
        String requestId = attribute instanceof String s ? s : null;

        ApiError error = new ApiError(
                ErrorCode.FORBIDDEN.name(),
                "You do not have permission to perform this action",
                Instant.now(),
                requestId);

        ApiErrorResponse body = new ApiErrorResponse(false, error, request.getRequestURI(), List.of());

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(), body);
    }
}