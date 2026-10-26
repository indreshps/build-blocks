package io.github.buildblocks.securitytest;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import io.github.buildblocks.securityauth.user.AuthUserStore;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RoleAccessIntegrationTest {

    @Value("${local.server.port}")
    private int port;

    @Autowired
    private AuthUserStore userStore;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void seedUsers() {
   
        if (!userStore.existsByUsername("roleadmin")) {
            userStore.save("roleadmin", passwordEncoder.encode("password123"), Set.of("ADMIN"));
        }
        if (!userStore.existsByUsername("roleuser")) {
            userStore.save("roleuser", passwordEncoder.encode("password123"), Set.of("USER"));
        }
    }

    private String loginAndGetToken(String username) throws Exception {
        String json = "{\"username\":\"" + username + "\",\"password\":\"password123\"}";
        HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/auth/login"))
                .header("Content-Type", "application/json")
                .POST(BodyPublishers.ofString(json))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        return mapper.readTree(response.body()).get("accessToken").asString();
    }

    private HttpResponse<String> getAdminPing(String token) throws Exception {
        HttpRequest.Builder builder = HttpRequest
                .newBuilder(URI.create("http://localhost:" + port + "/api/admin/ping")).GET();
        if (token != null) {
            builder.header("Authorization", "Bearer " + token);
        }
        return client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }

    @Test
    void adminGets200() throws Exception {
        HttpResponse<String> response = getAdminPing(loginAndGetToken("roleadmin"));

        assertEquals(200, response.statusCode());
        assertEquals("admin-pong", response.body());
    }

    @Test
    void userGets403WithStandardJson() throws Exception {
        HttpResponse<String> response = getAdminPing(loginAndGetToken("roleuser"));

        assertEquals(403, response.statusCode());
        assertEquals("FORBIDDEN", mapper.readTree(response.body()).get("error").get("code").asString());
    }

    @Test
    void noTokenGets401() throws Exception {
        HttpResponse<String> response = getAdminPing(null);

        assertEquals(401, response.statusCode());
    }
}