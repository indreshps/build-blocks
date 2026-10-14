package io.github.buildblocks.apiresponse.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertNull;

class DatabaseExceptionTest {

    @Test
    void shouldUseDatabaseErrorCode() {
        DatabaseException exception =
                new DatabaseException("Unable to access the database");

        assertEquals(
                ErrorCode.DATABASE_ERROR,
                exception.getErrorCode()
        );
    }

    @Test
    void shouldPreserveExceptionMessage() {
        DatabaseException exception =
                new DatabaseException("Unable to access the database");

        assertEquals(
                "Unable to access the database",
                exception.getMessage()
        );
    }

    @Test
    void shouldPreserveOriginalCause() {
        Throwable originalCause =
                new RuntimeException("Connection failed");

        DatabaseException exception =
                new DatabaseException(
                        "Unable to access the database",
                        originalCause
                );

        assertSame(originalCause, exception.getCause());
    }

    @Test
    void shouldAllowMissingCause() {
        DatabaseException exception =
                new DatabaseException("Unable to access the database");

        assertNull(exception.getCause());
    }
}