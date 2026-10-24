package io.github.buildblocks.securityauth.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import io.github.buildblocks.apiresponse.filter.RequestIdFilter;
import io.github.buildblocks.apiresponse.response.ApiErrorResponse;
import io.github.buildblocks.securityauth.exception.InvalidCredentialsException;
import io.github.buildblocks.securityauth.exception.UsernameAlreadyExistsException;

class AuthExceptionHandlerTest {

    private final AuthExceptionHandler handler = new AuthExceptionHandler();

    @Test
    void invalidCredentialsReturns401() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/auth/login");
        request.setAttribute(RequestIdFilter.REQUEST_ID_ATTRIBUTE, "req-test-1");

        ResponseEntity<ApiErrorResponse> response =
                handler.handleInvalidCredentials(new InvalidCredentialsException(), request);

        assertEquals(401, response.getStatusCode().value());
        ApiErrorResponse body = response.getBody();
        assertFalse(body.success());
        assertEquals("UNAUTHORIZED", body.error().code());
        assertEquals("Invalid username or password", body.error().message());
        assertEquals("req-test-1", body.error().requestId());
        assertEquals("/api/auth/login", body.path());
    }

    @Test
    void usernameAlreadyExistsReturns409() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/auth/register");

        ResponseEntity<ApiErrorResponse> response =
                handler.handleUsernameExists(new UsernameAlreadyExistsException("indresh"), request);

        assertEquals(409, response.getStatusCode().value());
        ApiErrorResponse body = response.getBody();
        assertEquals("CONFLICT", body.error().code());
        assertEquals("/api/auth/register", body.path());
    }
}