package io.github.buildblocks.securityauth.autoconfigure;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "buildblocks.security-auth")
public class SecurityAuthProperties {

    private boolean enabled = true;

    private final Jwt jwt = new Jwt();

    private List<String> publicPaths = new ArrayList<>(List.of("/api/auth/**"));

    private String defaultRole = "USER";

public String getDefaultRole() { return defaultRole; }
public void setDefaultRole(String defaultRole) { this.defaultRole = defaultRole; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public Jwt getJwt() { return jwt; }

    public List<String> getPublicPaths() { return publicPaths; }
    public void setPublicPaths(List<String> publicPaths) { this.publicPaths = publicPaths; }

    public static class Jwt {

        private String secret;
        private long expirationMinutes = 60;

        public String getSecret() { return secret; }
        public void setSecret(String secret) { this.secret = secret; }

        public long getExpirationMinutes() { return expirationMinutes; }
        public void setExpirationMinutes(long expirationMinutes) { this.expirationMinutes = expirationMinutes; }
    }
}