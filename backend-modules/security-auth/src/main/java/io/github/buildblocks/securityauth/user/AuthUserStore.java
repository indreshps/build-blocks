package io.github.buildblocks.securityauth.user;

import java.util.Optional;
import java.util.Set;

public interface AuthUserStore {

    Optional<AuthUser> findByUsername(String username);

    boolean existsByUsername(String username);

    void save(String username, String passwordHash, Set<String> roles);
}