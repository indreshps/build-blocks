BuildBlocks API Response & Error Handler

A reusable Spring Boot module that helps applications return consistent API error responses with meaningful error codes, timestamps, request IDs, and field-level validation details.

Features

- Consistent error responses — a predictable JSON structure for API errors.
- Standard error codes — codes such as "RESOURCE_NOT_FOUND", "VALIDATION_FAILED", and "INTERNAL_SERVER_ERROR".
- HTTP status mapping — maps supported application errors to appropriate HTTP status codes.
- Request ID support — generates or accepts a validated "X-Request-ID" for request correlation.
- Validation error details — returns field names and validation messages for supported request validation failures.
- Safe unexpected-error responses — avoids returning internal exception details to API clients.
- Spring Boot auto-configuration — automatically configures the handler and request ID filter when enabled.
- Customizable integration — allows applications to provide their own beans when appropriate.
- Automated tests — verifies error response structure, exception handling, and request ID behavior.

Project Status

Version: "0.1.0-SNAPSHOT"

This module is under development. Its configuration, API, and behavior may change before the first stable release.

Error Response Format

The module returns errors in a consistent JSON format.

Example

{
  "success": false,
  "error": {
    "code": "RESOURCE_NOT_FOUND",
    "message": "Expense not found",
    "timestamp": "2026-10-13T10:30:00Z",
    "requestId": "req-a83f21d7"
  },
  "path": "/api/expenses/999",
  "fieldErrors": []
}

Field descriptions

Field| Description
"success"| Always "false" for an error response.
"error.code"| Stable application error code.
"error.message"| Human-readable error message.
"error.timestamp"| Time the error response was created, in UTC using ISO 8601 format.
"error.requestId"| Request correlation ID, when available.
"path"| Request URI that produced the error.
"fieldErrors"| List of field-level validation errors; an empty list when none apply.

Example validation errors

For invalid request data, the response can include field-specific errors:

{
  "success": false,
  "error": {
    "code": "VALIDATION_FAILED",
    "message": "Request validation failed",
    "timestamp": "2026-10-13T10:30:00Z",
    "requestId": "req-a83f21d7"
  },
  "path": "/api/expenses",
  "fieldErrors": [
    {
      "field": "amount",
      "message": "Amount must be greater than zero"
    }
  ]
}

The HTTP status code is sent separately in the HTTP response. For example, a validation failure uses "400 Bad Request", while a missing resource uses "404 Not Found".

Supported Error Codes

Error code| HTTP status| Meaning
"VALIDATION_FAILED"| 400| Request data failed validation.
"INVALID_REQUEST"| 400| The request contains invalid input.
"UNAUTHORIZED"| 401| Authentication is required or invalid.
"FORBIDDEN"| 403| The caller is not permitted to perform the operation.
"RESOURCE_NOT_FOUND"| 404| The requested resource does not exist.
"METHOD_NOT_ALLOWED"| 405| The HTTP method is not supported for the requested endpoint.
"CONFLICT"| 409| The request conflicts with the current resource state.
"DATABASE_ERROR"| 500| A database-related application error occurred.
"INTERNAL_SERVER_ERROR"| 500| An unexpected or otherwise unmapped server error occurred.

Using custom exceptions

The module provides specialized exceptions for common application errors:

- "ResourceNotFoundException"
- "InvalidRequestException"
- "UnauthorizedException"
- "ForbiddenException"
- "ConflictException"
- "DatabaseException"

These exceptions can be thrown from application code and handled by the module's exception handler.

For example:

throw new ResourceNotFoundException("Expense not found");

Security note: Only pass messages that are safe to show to API clients. Never include passwords, database credentials, SQL statements, stack traces, or other internal implementation details in exception messages.

Configuration

The module uses the following configuration property:

buildblocks.api-response.enabled=true

Disable the module

To disable its auto-configuration in an application, set:

buildblocks.api-response.enabled=false

When the property is missing, auto-configuration is enabled by default.

Integration notes

- The module is intended for Spring Boot servlet-based web applications.
- It provides a default exception handler and request ID filter.
- Applications can provide their own beans to customize or replace default behavior, subject to Spring Boot bean registration and configuration ordering.
- Applications using Spring Security may need additional security-specific exception handling to ensure authentication and authorization errors follow the same response format.
- Only exceptions explicitly supported by the handler are mapped to their documented status codes. Other unexpected exceptions receive a generic "500 Internal Server Error" response.
- Database exceptions should be translated deliberately; not every database failure is necessarily a server error, and transient availability failures may require different handling.

Project Structure

api-response/
├── pom.xml
├── README.md
└── src/
    ├── main/
    │   ├── java/io/github/buildblocks/apiresponse/
    │   │   ├── autoconfigure/
    │   │   ├── exception/
    │   │   ├── filter/
    │   │   ├── handler/
    │   │   └── response/
    │   └── resources/
    │       └── META-INF/spring/
    │           └── AutoConfiguration.imports
    └── test/
        └── java/io/github/buildblocks/apiresponse/

Build and Test

Requirements:

- Java 21 or later
- Maven 3.9 or later

Run these commands from the "backend-modules/api-response" directory.

Run the tests:

mvn test

Perform a clean verification build:

mvn clean verify

Build the JAR:

mvn package

The generated JAR is placed in the "target/" directory.

License

The license for this module will be specified when the repository's licensing terms are finalized.