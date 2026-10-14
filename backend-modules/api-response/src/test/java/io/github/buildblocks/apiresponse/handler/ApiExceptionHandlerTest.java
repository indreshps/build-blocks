package io.github.buildblocks.apiresponse.handler;

import io.github.buildblocks.apiresponse.exception.ErrorCode;
import io.github.buildblocks.apiresponse.exception.ResourceNotFoundException;
import io.github.buildblocks.apiresponse.filter.RequestIdFilter;
import io.github.buildblocks.apiresponse.response.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApiExceptionHandlerTest {

    private final ApiExceptionHandler handler =
            new ApiExceptionHandler();

    @Test
    void shouldReturnNotFoundForResourceNotFoundException() {
        MockHttpServletRequest request = createRequest();

        ResponseEntity<ApiErrorResponse> response =
                handler.handleApiException(
                        new ResourceNotFoundException("Expense not found"),
                        request
                );

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(
                "RESOURCE_NOT_FOUND",
                response.getBody().error().code()
        );
        assertEquals(
                "/api/expenses/999",
                response.getBody().path()
        );
        assertEquals(
                "req-test-123",
                response.getBody().error().requestId()
        );
    }

    @Test
    void shouldReturnBadRequestForValidationErrors() {
        MockHttpServletRequest request = createRequest();

        Object target = new Object();
        BeanPropertyBindingResult bindingResult =
                new BeanPropertyBindingResult(target, "expense");

        bindingResult.addError(
                new FieldError(
                        "expense",
                        "amount",
                        -10,
                        false,
                        null,
                        null,
                        "Amount must be greater than zero"
                )
        );

        MethodArgumentNotValidException exception =
                new MethodArgumentNotValidException(null, bindingResult);

        ResponseEntity<ApiErrorResponse> response =
                handler.handleValidationException(exception, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(
                ErrorCode.VALIDATION_FAILED.name(),
                response.getBody().error().code()
        );
        assertEquals(
                "Request validation failed",
                response.getBody().error().message()
        );
        assertEquals(1, response.getBody().fieldErrors().size());
        assertEquals(
                "amount",
                response.getBody().fieldErrors().get(0).field()
        );
        assertEquals(
                "Amount must be greater than zero",
                response.getBody().fieldErrors().get(0).message()
        );
    }

    @Test
    void shouldReturnEmptyFieldErrorsForApiException() {
        MockHttpServletRequest request = createRequest();

        ResponseEntity<ApiErrorResponse> response =
                handler.handleApiException(
                        new ResourceNotFoundException("Expense not found"),
                        request
                );

        assertNotNull(response.getBody());
        assertNotNull(response.getBody().fieldErrors());
        assertEquals(0, response.getBody().fieldErrors().size());
    }
    private MockHttpServletRequest createRequest() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/expenses/999");
        request.setAttribute(
                RequestIdFilter.REQUEST_ID_ATTRIBUTE,
                "req-test-123"
        );
        return request;
    }


    @Test
void shouldReturnInternalServerErrorForUnexpectedException() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRequestURI("/api/expenses");

    request.setAttribute(
            RequestIdFilter.REQUEST_ID_ATTRIBUTE,
            "req-test123"
    );

    ResponseEntity<ApiErrorResponse> response =
            handler.handleUnexpectedException(
                    new RuntimeException("Sensitive internal details"),
                    request
            );

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(
            "INTERNAL_SERVER_ERROR",
            response.getBody().error().code()
    );
    assertEquals(
            "An unexpected error occurred",
            response.getBody().error().message()
    );
    assertEquals(
            "req-test123",
            response.getBody().error().requestId()
    );
    assertEquals(
            "/api/expenses",
            response.getBody().path()
    );
    assertTrue(response.getBody().fieldErrors().isEmpty());
    assertFalse(
            response.getBody().error().message()
                    .contains("Sensitive internal details")
    );
}

}