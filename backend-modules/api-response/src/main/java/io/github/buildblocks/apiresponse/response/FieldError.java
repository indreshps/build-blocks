package io.github.buildblocks.apiresponse.response;

public record FieldError(
        String field,
        String message
) {
}