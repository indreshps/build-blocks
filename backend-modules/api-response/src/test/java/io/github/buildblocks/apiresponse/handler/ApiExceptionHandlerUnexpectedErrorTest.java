package io.github.buildblocks.apiresponse.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import io.github.buildblocks.apiresponse.filter.RequestIdFilter;
import io.github.buildblocks.apiresponse.response.ApiErrorResponse;

class ApiExceptionHandlerUnexpectedErrorTest {

    private final ApiExceptionHandler handler = new ApiExceptionHandler();

    @Test
    void unexpectedErrorReturnsGenericMessageWithoutLeakingDetails() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/boom");
        request.setAttribute(RequestIdFilter.REQUEST_ID_ATTRIBUTE, "req-test-500");

        ResponseEntity<ApiErrorResponse> response = handler.handleUnexpectedException(
                new RuntimeException("db password is hunter2"), request);

        assertEquals(500, response.getStatusCode().value());
        ApiErrorResponse body = response.getBody();
        assertEquals("INTERNAL_SERVER_ERROR", body.error().code());
        assertEquals("An unexpected error occurred", body.error().message());
        assertEquals("req-test-500", body.error().requestId());
        assertEquals("/api/boom", body.path());
        assertFalse(body.error().message().contains("hunter2"));
    }
}