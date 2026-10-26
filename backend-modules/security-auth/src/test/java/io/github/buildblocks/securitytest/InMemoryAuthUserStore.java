package io.github.buildblocks.securitytest;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import io.github.buildblocks.securityauth.user.AuthUser;
import io.github.buildblocks.securityauth.user.AuthUserStore;

@Component
public class InMemoryAuthUserStore implements AuthUserStore {

    private final Map<String, AuthUser> users = new ConcurrentHashMap<>();

    @Override
    public Optional<AuthUser> findByUsername(String username) {
        return Optional.ofNullable(users.get(username));
    }

    @Override
    public boolean existsByUsername(String username) {
        return users.containsKey(username);
    }

    @Override
    public void save(String username, String passwordHash, Set<String> roles) {
        users.put(username, new AuthUser(username, passwordHash, roles));
    }
}