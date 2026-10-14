package io.github.buildblocks.apiresponse.response;

import java.time.Instant;

public record ApiError(
        String code,
        String message,
        Instant timestamp,
        String requestId
) {
}