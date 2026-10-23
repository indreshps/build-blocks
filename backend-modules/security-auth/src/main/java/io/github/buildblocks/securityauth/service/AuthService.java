package io.github.buildblocks.securityauth.service;

import java.util.Set;

import org.springframework.security.crypto.password.PasswordEncoder;

import io.github.buildblocks.securityauth.exception.InvalidCredentialsException;
import io.github.buildblocks.securityauth.exception.UsernameAlreadyExistsException;
import io.github.buildblocks.securityauth.jwt.JwtService;
import io.github.buildblocks.securityauth.user.AuthUser;
import io.github.buildblocks.securityauth.user.AuthUserStore;

public class AuthService {

    private final AuthUserStore userStore;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(AuthUserStore userStore, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userStore = userStore;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public void register(String username, String rawPassword) {
        if (userStore.existsByUsername(username)) {
            throw new UsernameAlreadyExistsException(username);
        }
        userStore.save(username, passwordEncoder.encode(rawPassword), Set.of("USER"));
    }

    public String login(String username, String rawPassword) {
        AuthUser user = userStore.findByUsername(username)
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(rawPassword, user.passwordHash())) {
            throw new InvalidCredentialsException();
        }
        return jwtService.generateToken(user.username(), user.roles());
    }
}