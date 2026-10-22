package io.github.buildblocks.securityauth.user;

import java.util.Set;

public record AuthUser(String username, String passwordHash, Set<String> roles) {
}