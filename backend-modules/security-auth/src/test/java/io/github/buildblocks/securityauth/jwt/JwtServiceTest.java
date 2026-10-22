package io.github.buildblocks.securityauth.jwt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;

class JwtServiceTest {

    private static final String SECRET = "this-is-a-test-secret-with-at-least-32-chars";

    @Test
    void generatedTokenCanBeParsedBack() {
        JwtService service = new JwtService(SECRET, 60);

        String token = service.generateToken("indresh", Set.of("USER", "ADMIN"));
        Optional<JwtService.TokenData> result = service.parseToken(token);

        assertTrue(result.isPresent());
        assertEquals("indresh", result.get().username());
        assertEquals(Set.of("USER", "ADMIN"), result.get().roles());
    }

    @Test
    void garbageTokenIsRejected() {
        JwtService service = new JwtService(SECRET, 60);

        assertTrue(service.parseToken("not-a-real-token").isEmpty());
    }

    @Test
    void tokenSignedWithDifferentSecretIsRejected() {
        JwtService service = new JwtService(SECRET, 60);
        JwtService other = new JwtService("a-completely-different-secret-of-32-chars-or-more", 60);

        String token = other.generateToken("indresh", Set.of("USER"));

        assertTrue(service.parseToken(token).isEmpty());
    }

    @Test
    void expiredTokenIsRejected() {
        JwtService service = new JwtService(SECRET, -1);

        String token = service.generateToken("indresh", Set.of("USER"));

        assertTrue(service.parseToken(token).isEmpty());
    }

    @Test
    void shortSecretIsRefused() {
        assertThrows(IllegalStateException.class, () -> new JwtService("too-short", 60));
    }

    @Test
    void missingSecretIsRefused() {
        assertThrows(IllegalStateException.class, () -> new JwtService(null, 60));
    }
}