package io.github.buildblocks.apiresponse.response;

import java.util.List;

public record ApiErrorResponse(
        boolean success,
        ApiError error,
        String path,
        List<FieldError> fieldErrors
) {

    public ApiErrorResponse {
        fieldErrors = fieldErrors == null
                ? List.of()
                : List.copyOf(fieldErrors);
    }
}