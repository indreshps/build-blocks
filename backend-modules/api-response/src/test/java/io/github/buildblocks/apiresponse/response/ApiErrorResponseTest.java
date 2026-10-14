package io.github.buildblocks.apiresponse.response;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ApiErrorResponseTest {

    @Test
    void shouldUseEmptyListWhenFieldErrorsAreNull() {

        ApiErrorResponse response = new ApiErrorResponse(
                false,
                null,
                null,
                null
        );

        assertNotNull(response.fieldErrors());
        assertEquals(List.of(), response.fieldErrors());
    }

    @Test
    void shouldPreserveProvidedFieldErrors() {

        FieldError fieldError = new FieldError(
                "amount",
                "Amount must be greater than zero"
        );

        ApiErrorResponse response = new ApiErrorResponse(
                false,
                null,
                null,
                List.of(fieldError)
        );

        assertEquals(1, response.fieldErrors().size());
        assertEquals("amount", response.fieldErrors().get(0).field());
        assertEquals(
                "Amount must be greater than zero",
                response.fieldErrors().get(0).message()
        );
    }


    @Test
void shouldPreventModificationOfFieldErrors() {

    FieldError fieldError = new FieldError(
            "amount",
            "Amount must be greater than zero"
    );

    ApiErrorResponse response = new ApiErrorResponse(
            false,
            null,
            null,
            List.of(fieldError)
    );

    assertThrows(
            UnsupportedOperationException.class,
            () -> response.fieldErrors().add(
                    new FieldError("date", "Date is required")
            )
    );
}


}