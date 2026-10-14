package io.github.buildblocks.apiresponse.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class ResourceNotFoundExceptionTest {

    @Test
    void shouldUseResourceNotFoundErrorCode() {

        ResourceNotFoundException exception =
                new ResourceNotFoundException("Expense not found");

        assertEquals(
                ErrorCode.RESOURCE_NOT_FOUND,
                exception.getErrorCode()
        );
    }

    @Test
    void shouldPreserveExceptionMessage() {

        ResourceNotFoundException exception =
                new ResourceNotFoundException("Expense not found");

        assertEquals(
                "Expense not found",
                exception.getMessage()
        );
    }

    @Test
    void shouldExtendApiException() {

        ResourceNotFoundException exception =
                new ResourceNotFoundException("Expense not found");

        assertInstanceOf(ApiException.class, exception);
    }
}