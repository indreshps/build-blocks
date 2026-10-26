package io.github.buildblocks.securityauth.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import io.github.buildblocks.securityauth.exception.InvalidCredentialsException;
import io.github.buildblocks.securityauth.exception.UsernameAlreadyExistsException;
import io.github.buildblocks.securityauth.jwt.JwtService;
import io.github.buildblocks.securityauth.user.AuthUser;
import io.github.buildblocks.securityauth.user.AuthUserStore;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private static final String SECRET = "this-is-a-test-secret-with-at-least-32-chars";

    @Mock
    private AuthUserStore userStore;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private JwtService jwtService;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET, 60);
        authService = new AuthService(userStore, passwordEncoder, jwtService);
    }

    @Test
    void registerSavesHashedPasswordWithUserRoleOnly() {
        when(userStore.existsByUsername("indresh")).thenReturn(false);

        authService.register("indresh", "secret123");

        ArgumentCaptor<String> hashCaptor = ArgumentCaptor.forClass(String.class);
        verify(userStore).save(eq("indresh"), hashCaptor.capture(), eq(Set.of("USER")));

        String savedHash = hashCaptor.getValue();
        assertNotEquals("secret123", savedHash);
        assertTrue(passwordEncoder.matches("secret123", savedHash));
    }

    @Test
    void registerRejectsExistingUsername() {
        when(userStore.existsByUsername("indresh")).thenReturn(true);

        assertThrows(UsernameAlreadyExistsException.class,
                () -> authService.register("indresh", "secret123"));

        verify(userStore, never()).save(any(), any(), any());
    }

    @Test
    void loginReturnsValidTokenForCorrectPassword() {
        String hash = passwordEncoder.encode("secret123");
        when(userStore.findByUsername("indresh"))
                .thenReturn(Optional.of(new AuthUser("indresh", hash, Set.of("USER"))));

        String token = authService.login("indresh", "secret123");

        JwtService.TokenData data = jwtService.parseToken(token).orElseThrow();
        assertEquals("indresh", data.username());
        assertEquals(Set.of("USER"), data.roles());
    }

    @Test
    void loginRejectsWrongPassword() {
        String hash = passwordEncoder.encode("secret123");
        when(userStore.findByUsername("indresh"))
                .thenReturn(Optional.of(new AuthUser("indresh", hash, Set.of("USER"))));

        assertThrows(InvalidCredentialsException.class,
                () -> authService.login("indresh", "wrong-password"));
    }

    @Test
    void loginRejectsUnknownUser() {
        when(userStore.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class,
                () -> authService.login("ghost", "secret123"));
    }


    @Test
    void registerUsesConfiguredDefaultRole() {
        AuthService customRoleService = new AuthService(userStore, passwordEncoder, jwtService, "MEMBER");
        when(userStore.existsByUsername("indresh")).thenReturn(false);

        customRoleService.register("indresh", "secret123");

        verify(userStore).save(eq("indresh"), any(), eq(Set.of("MEMBER")));
    }

    @Test
    void blankDefaultRoleIsRefused() {
        assertThrows(IllegalArgumentException.class,
                () -> new AuthService(userStore, passwordEncoder, jwtService, "  "));
    }
    
}