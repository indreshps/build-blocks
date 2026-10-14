# BuildBlocks API Response & Error Handler

A reusable Spring Boot module that helps applications return consistent API error responses with meaningful error codes, timestamps, request IDs, and field-level validation details.

## Features

- **Consistent Error Responses** — Provides a predictable JSON structure for API errors.
- **Standard Error Codes** — Supports error codes such as `RESOURCE_NOT_FOUND`, `VALIDATION_FAILED`, and `INTERNAL_SERVER_ERROR`.
- **HTTP Status Mapping** — Maps supported application errors to appropriate HTTP status codes.
- **Request ID Support** — Generates or accepts a validated `X-Request-ID` for request correlation.
- **Validation Error Details** — Returns field names and validation messages for supported request validation failures.
- **Safe Unexpected-Error Responses** — Avoids returning internal exception details to API clients.
- **Spring Boot Auto-Configuration** — Automatically configures the error handler and request ID filter when enabled.
- **Customizable Integration** — Allows applications to provide their own beans when appropriate.
- **Automated Tests** — Verifies error response structure, exception handling, and request ID behavior.

## Project Status

- **Version:** `0.1.0-SNAPSHOT`
- **Status:** Under Development

This module is under development. Its configuration, API, and behavior may change before the first stable release.

## Error Response Format

The module returns errors in a consistent JSON format.

### Example

```json
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
```

### Field Descriptions

| Field | Description |
|---|---|
| `success` | Always `false` for an error response. |
| `error.code` | Stable application error code. |
| `error.message` | Human-readable error message. |
| `error.timestamp` | Time the error response was created, in UTC using ISO 8601 format. |
| `error.requestId` | Request correlation ID, when available. |
| `path` | Request URI that produced the error. |
| `fieldErrors` | List of field-level validation errors; an empty list when none apply. |

### Example Validation Errors

For invalid request data, the response can include field-specific errors:

```json
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
```

The HTTP status code is sent separately in the HTTP response. For example, a validation failure uses `400 Bad Request`, while a missing resource uses `404 Not Found`.

## Supported Error Codes

| Error Code | HTTP Status | Meaning |
|---|---:|---|
| `VALIDATION_FAILED` | 400 | Request data failed validation. |
| `INVALID_REQUEST` | 400 | The request contains invalid input. |
| `UNAUTHORIZED` | 401 | Authentication is required or invalid. |
| `FORBIDDEN` | 403 | The caller is not permitted to perform the operation. |
| `RESOURCE_NOT_FOUND` | 404 | The requested resource does not exist. |
| `METHOD_NOT_ALLOWED` | 405 | The HTTP method is not supported for the requested endpoint. |
| `CONFLICT` | 409 | The request conflicts with the current resource state. |
| `DATABASE_ERROR` | 500 | A database-related application error occurred. |
| `INTERNAL_SERVER_ERROR` | 500 | An unexpected or otherwise unmapped server error occurred. |

## Using Custom Exceptions

The module provides specialized exceptions for common application errors:

- `ResourceNotFoundException`
- `InvalidRequestException`
- `UnauthorizedException`
- `ForbiddenException`
- `ConflictException`
- `DatabaseException`

These exceptions can be thrown from application code and handled by the module's exception handler.

### Example

```java
throw new ResourceNotFoundException("Expense not found");
```

**Security Note:** Only pass messages that are safe to show to API clients. Never include passwords, database credentials, SQL statements, stack traces, or other internal implementation details in exception messages.

## Configuration

The module uses the following configuration property:

```properties
buildblocks.api-response.enabled=true
```

### Disable the Module

To disable its auto-configuration in an application, set:

```properties
buildblocks.api-response.enabled=false
```

When the property is missing, auto-configuration is enabled by default.

## Integration Notes

- The module is intended for Spring Boot servlet-based web applications.
- It provides a default exception handler and request ID filter.
- Applications can provide their own beans to customize or replace default behavior, subject to Spring Boot bean registration and configuration ordering.
- Applications using Spring Security may need additional security-specific exception handling to ensure authentication and authorization errors follow the same response format.
- Only exceptions explicitly supported by the handler are mapped to their documented status codes. Other unexpected exceptions receive a generic `500 Internal Server Error` response.
- Database exceptions should be translated deliberately; not every database failure is necessarily a server error, and transient availability failures may require different handling.

## Project Structure

```text
api-response/
├── pom.xml
├── README.md
└── src/
    ├── main/
    │   ├── java/
    │   │   └── io/github/buildblocks/apiresponse/
    │   │       ├── autoconfigure/
    │   │       ├── exception/
    │   │       ├── filter/
    │   │       ├── handler/
    │   │       └── response/
    │   └── resources/
    │       └── META-INF/
    │           └── spring/
    │               └── org.springframework.boot.autoconfigure.AutoConfiguration.imports
    └── test/
        └── java/
            └── io/github/buildblocks/apiresponse/
```

## Build and Test

### Requirements

- Java 21 or later
- Maven 3.9 or later

Run the following commands from the `backend-modules/api-response` directory.

**Run the tests:**

```bash
mvn test
```

**Perform a clean verification build:**

```bash
mvn clean verify
```

**Build the JAR:**

```bash
mvn package
```

The generated JAR is placed in the `target/` directory.

## Using as a Maven Dependency

This module is under development and has not been published to Maven Central yet. To use it today, install it into your local Maven repository.

### Step 1: Clone the Repository

```bash
git clone https://github.com/indreshps5/build-blocks.git
```

### Step 2: Install the Module

```bash
cd build-blocks/backend-modules/api-response
mvn clean install
```

### Step 3: Add the Dependency to Your Spring Boot Application

Add the following dependency to your application's `pom.xml`:

```xml
<dependency>
    <groupId>io.github.buildblocks</groupId>
    <artifactId>buildblocks-api-response-spring-boot-starter</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</dependency>
```

No extra configuration is needed when the module's auto-configuration is correctly registered and enabled. The error handler and request ID filter are automatically configured in a supported Spring Boot servlet application.

## License

The license for this module will be specified when the repository's licensing terms are finalized.
