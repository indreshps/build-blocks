# BuildBlocks Security Auth

A reusable Spring Boot module that adds JWT authentication, user registration and login, BCrypt password hashing, and role-based access control. It does not own your user table. Your application provides one small class, and the module handles everything else.

## Features

- **Register and Login Endpoints:** `POST /api/auth/register` and `POST /api/auth/login`.
- **JWT Access Tokens:** Signed tokens carrying the username and roles, with a configurable expiry.
- **BCrypt Password Hashing:** Plain passwords are never stored or passed to your application.
- **Role-Based Access:** Roles are stored in the token. Use `@PreAuthorize("hasRole('ADMIN')")` on your own endpoints.
- **Stateless Security:** No sessions. Each request carries its own token.
- **Configurable Public Paths:** Choose which URLs need no token.
- **Standard Error Responses:** 401, 403, 409 and 400 errors use the BuildBlocks API Response format, including the request ID.
- **Bring Your Own User Storage:** Implement one interface, `AuthUserStore`, with your own database and entity.
- **Spring Boot Auto-Configuration:** Configures itself when enabled. Every bean can be replaced by your own.
- **Automated Tests:** Unit tests plus end-to-end tests covering register, login, protected URLs and role access.

## Project Status

- **Version:** `0.1.0-SNAPSHOT`
- **Status:** Under Development

This module is an early version. Its configuration, API, and behavior may change before the first stable release. See [Known Limitations](#known-limitations) before using it.

## Requirements

- Java 21 or later
- Maven 3.9 or later
- A Spring Boot servlet (Spring MVC) application
- The [API Response module](../api-response) (installed automatically as a dependency, see below)

## Using as a Maven Dependency

This module is under development and has not been published to Maven Central yet. To use it today, install it into your local Maven repository. Install API Response first, because this module depends on it.

### Step 1: Clone the Repository

```bash
git clone https://github.com/indreshps/build-blocks.git
```

### Step 2: Install Both Modules

```bash
cd build-blocks/backend-modules/api-response
mvn clean install

cd ../security-auth
mvn clean install
```

### Step 3: Add the Dependency to Your Application

Add this to your application's `pom.xml`:

```xml
<dependency>
    <groupId>io.github.buildblocks</groupId>
    <artifactId>buildblocks-security-auth-spring-boot-starter</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</dependency>
```

You do not need to add the API Response module separately. It comes with this one.

## Quick Start

You need two things: a JWT secret and one class that implements `AuthUserStore`.

### 1. Set the JWT Secret

In `application.properties`:

```properties
buildblocks.security-auth.jwt.secret=${JWT_SECRET}
```

Provide the value through an environment variable. It must be at least 32 characters long. The application refuses to start if the secret is missing or too short. Never commit a real secret to Git.

### 2. Implement AuthUserStore

This tells the module how to find and save users in your own database. The module only ever calls these three methods.

```java
public interface AuthUserStore {

    Optional<AuthUser> findByUsername(String username);

    boolean existsByUsername(String username);

    void save(String username, String passwordHash, Set<String> roles);
}
```

`AuthUser` is a simple record holding `username`, `passwordHash` and `roles`. Roles are plain names such as `USER` or `ADMIN`, without the `ROLE_` prefix. The module adds that prefix itself.

Here is an example using Spring Data JPA. Your entity and repository will differ.

```java
@Component
public class JpaAuthUserStore implements AuthUserStore {

    private final AppUserRepository repository;

    public JpaAuthUserStore(AppUserRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<AuthUser> findByUsername(String username) {
        return repository.findByUsername(username)
                .map(u -> new AuthUser(
                        u.getUsername(),
                        u.getPasswordHash(),
                        u.getRoles()
                ));
    }

    @Override
    public boolean existsByUsername(String username) {
        return repository.existsByUsername(username);
    }

    @Override
    public void save(String username, String passwordHash, Set<String> roles) {
        AppUser user = new AppUser();
        user.setUsername(username);
        user.setPasswordHash(passwordHash);
        user.setRoles(roles);
        repository.save(user);
    }
}
```

The class must be a Spring bean (for example with `@Component`). If no `AuthUserStore` bean exists, the application fails at startup with a message explaining what is missing.

### 3. Run Your Application

That is all the setup needed. The endpoints below are now available, and every URL except the public paths requires a valid token.

## Endpoints

### Register

`POST /api/auth/register`

**Request body:**

```json
{
  "username": "alice",
  "password": "password123"
}
```

Returns `201 Created` with no body. Username must be 3 to 50 characters. Password must be 8 to 72 characters (BCrypt ignores anything beyond 72 bytes).

New users always receive only the default role. A caller can never choose their own role at registration.

### Login

`POST /api/auth/login`

**Request body:**

```json
{
  "username": "alice",
  "password": "password123"
}
```

Returns `200 OK`:

```json
{
  "accessToken": "eyJhbGciOi...",
  "tokenType": "Bearer"
}
```

### Calling a Protected URL

Send the token in the `Authorization` header:

```http
Authorization: Bearer eyJhbGciOi...
```

## Error Responses

All errors use the API Response format, including `requestId`.

| Situation | HTTP Status | Error Code |
|---|---:|---|
| Missing, invalid or expired token | 401 | `UNAUTHORIZED` |
| Wrong username or password | 401 | `UNAUTHORIZED` |
| Logged in but not allowed (wrong role) | 403 | `FORBIDDEN` |
| Username already taken | 409 | `CONFLICT` |
| Invalid register or login fields | 400 | `VALIDATION_FAILED` |

**Example 401 response:**

```json
{
  "success": false,
  "error": {
    "code": "UNAUTHORIZED",
    "message": "Authentication is required or invalid",
    "timestamp": "2026-10-26T10:30:00Z",
    "requestId": "req-a83f21d7"
  },
  "path": "/api/expenses",
  "fieldErrors": []
}
```

For security, the module does not reveal whether a token was missing, expired or forged. A failed login says only "Invalid username or password", so an attacker cannot tell which usernames exist.

## Configuration

| Property | Default | Description |
|---|---|---|
| `buildblocks.security-auth.enabled` | `true` | Set to `false` to disable the module. |
| `buildblocks.security-auth.jwt.secret` | None (required) | Signing secret, at least 32 characters. |
| `buildblocks.security-auth.jwt.expiration-minutes` | `60` | How long a token stays valid. |
| `buildblocks.security-auth.public-paths` | `/api/auth/**` | URLs that need no token. |
| `buildblocks.security-auth.default-role` | `USER` | Role given to every newly registered user. |

If you set `public-paths`, your list replaces the default. Include `/api/auth/**` in it, or nobody will be able to register or log in.

## Protecting Your Own Endpoints

By default, every URL outside the public paths requires a valid token. To restrict an endpoint to a role, use method security, which this module enables for you:

```java
@GetMapping("/api/admin/reports")
@PreAuthorize("hasRole('ADMIN')")
public String adminReports() {
    return "admin only";
}
```

A user without the role receives the standard 403 response. A request with no token receives the standard 401 response.

## Creating the First Admin

The module deliberately has no endpoint for creating admins, because how an application creates its admins depends on the application. A simple approach is to seed one at startup, with the password taken from an environment variable:

```java
@Bean
CommandLineRunner seedAdmin(
        AuthUserStore store,
        PasswordEncoder encoder,
        @Value("${ADMIN_PASSWORD}") String adminPassword) {

    return args -> {
        if (!store.existsByUsername("admin")) {
            store.save("admin", encoder.encode(adminPassword), Set.of("ADMIN"));
        }
    };
}
```

Always store the hash from `encoder.encode(...)`, never the plain password.

## Customization

Every bean the module creates uses `@ConditionalOnMissingBean`. To replace one, define your own bean of the same type. For example, define your own `PasswordEncoder` to use a different hashing setup.

## Integration Notes

- The module is intended for Spring Boot servlet-based web applications.
- It registers its own security filter chain. If your application defines a `SecurityFilterChain` bean, the module's chain is not created and you must set up JWT handling yourself.
- CSRF protection, form login and HTTP Basic are disabled, because the module uses stateless tokens instead of browser sessions.
- It relies on the API Response module's request ID filter running first, so that 401 and 403 responses include a request ID.

## Project Structure

```text
security-auth/
├── pom.xml
├── README.md
└── src/
    ├── main/
    │   ├── java/io/github/buildblocks/securityauth/
    │   │   ├── autoconfigure/
    │   │   ├── controller/
    │   │   ├── dto/
    │   │   ├── exception/
    │   │   ├── handler/
    │   │   ├── jwt/
    │   │   ├── security/
    │   │   ├── service/
    │   │   └── user/
    │   └── resources/
    │       └── META-INF/spring/
    │           └── org.springframework.boot.autoconfigure.AutoConfiguration.imports
    └── test/
        └── java/io/github/buildblocks/
            ├── securityauth/
            └── securitytest/
```

## Build and Test

Run these from the `backend-modules/security-auth` directory.

**Run the tests:**

```bash
mvn test
```

**Install into your local Maven repository:**

```bash
mvn clean install
```

## Known Limitations

This is an early version. Not included yet:

- No refresh tokens. When a token expires, the user logs in again.
- No logout or token revocation. A token stays valid until it expires, so keep the expiry short.
- No rate limiting on login. Add your own if the application is exposed publicly.
- No email verification or password reset.
- No social login (OAuth).
- No CORS support yet. A browser frontend on a different origin needs CORS configured, which the module's security chain does not handle yet.
- No admin-creation endpoint, on purpose. See [Creating the First Admin](#creating-the-first-admin).

## License

This project is licensed under the MIT License. See the [LICENSE](../../LICENSE) file in the repository root for details.