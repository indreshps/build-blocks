package io.github.buildblocks.apiresponse.exception;

public class InvalidRequestException extends ApiException {

    public InvalidRequestException(String message) {
        super(ErrorCode.INVALID_REQUEST, message);
    }
}