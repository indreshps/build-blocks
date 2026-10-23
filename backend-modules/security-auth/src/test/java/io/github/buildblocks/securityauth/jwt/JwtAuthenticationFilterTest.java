package io.github.buildblocks.securityauth.jwt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

class JwtAuthenticationFilterTest {

    private static final String SECRET = "this-is-a-test-secret-with-at-least-32-chars";

    private JwtService jwtService;
    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        jwtService = new JwtService(SECRET, 60);
        filter = new JwtAuthenticationFilter(jwtService);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private MockFilterChain run(String authorizationHeader) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        if (authorizationHeader != null) {
            request.addHeader("Authorization", authorizationHeader);
        }
        MockFilterChain chain = new MockFilterChain();
        filter.doFilter(request, new MockHttpServletResponse(), chain);
        return chain;
    }

    @Test
    void validTokenAuthenticatesUserWithRolePrefix() throws Exception {
        String token = jwtService.generateToken("indresh", Set.of("USER", "ADMIN"));

        run("Bearer " + token);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertEquals("indresh", auth.getPrincipal());

        Set<String> authorities = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
        assertEquals(Set.of("ROLE_USER", "ROLE_ADMIN"), authorities);
    }

    @Test
    void missingHeaderLeavesRequestUnauthenticated() throws Exception {
        run(null);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void garbageTokenLeavesRequestUnauthenticated() throws Exception {
        run("Bearer not-a-real-token");

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void headerWithoutBearerPrefixIsIgnored() throws Exception {
        String token = jwtService.generateToken("indresh", Set.of("USER"));

        run(token);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void requestAlwaysContinuesDownTheChain() throws Exception {
        MockFilterChain chain = run("Bearer not-a-real-token");

        assertNotNull(chain.getRequest());
    }
}