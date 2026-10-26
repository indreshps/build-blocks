package io.github.buildblocks.securitytest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SecurityAuthIntegrationTest {

    @Value("${local.server.port}")
    private int port;

    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    private HttpResponse<String> get(String path, String token) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path)).GET();
        if (token != null) {
            builder.header("Authorization", "Bearer " + token);
        }
        return client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> post(String path, String json) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .header("Content-Type", "application/json")
                .POST(BodyPublishers.ofString(json))
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private String credentials(String username, String password) {
        return "{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}";
    }

    @Test
    void unauthenticatedRequestGets401WithRequestId() throws Exception {
        HttpResponse<String> response = get("/api/ping", null);

        assertEquals(401, response.statusCode());
        JsonNode body = mapper.readTree(response.body());
        assertFalse(body.get("success").asBoolean());
        assertEquals("UNAUTHORIZED", body.get("error").get("code").asString());
        assertNotNull(body.get("error").get("requestId"), "requestId missing from 401 body");
        assertTrue(response.headers().firstValue("X-Request-ID").isPresent(), "X-Request-ID header missing");
    }

    @Test
    void registerLoginAndCallProtectedUrl() throws Exception {
        assertEquals(201, post("/api/auth/register", credentials("alice", "password123")).statusCode());

        HttpResponse<String> login = post("/api/auth/login", credentials("alice", "password123"));
        assertEquals(200, login.statusCode());
        JsonNode loginBody = mapper.readTree(login.body());
        assertEquals("Bearer", loginBody.get("tokenType").asString());
        String token = loginBody.get("accessToken").asString();

        HttpResponse<String> ping = get("/api/ping", token);
        assertEquals(200, ping.statusCode());
        assertEquals("pong", ping.body());
    }

    @Test
    void duplicateRegistrationGets409() throws Exception {
        assertEquals(201, post("/api/auth/register", credentials("bob", "password123")).statusCode());

        HttpResponse<String> second = post("/api/auth/register", credentials("bob", "password123"));

        assertEquals(409, second.statusCode());
        assertEquals("CONFLICT", mapper.readTree(second.body()).get("error").get("code").asString());
    }

    @Test
    void wrongPasswordGets401() throws Exception {
        assertEquals(201, post("/api/auth/register", credentials("carol", "password123")).statusCode());

        HttpResponse<String> login = post("/api/auth/login", credentials("carol", "wrong-password"));
        assertEquals(401, login.statusCode());
        assertEquals("UNAUTHORIZED", mapper.readTree(login.body()).get("error").get("code").asString());
    }

    @Test
    void invalidRegistrationGets400WithFieldErrors() throws Exception {
        HttpResponse<String> response = post("/api/auth/register", credentials("ab", "short"));

        assertEquals(400, response.statusCode());
        JsonNode body = mapper.readTree(response.body());
        assertEquals("VALIDATION_FAILED", body.get("error").get("code").asString());
        assertEquals(2, body.get("fieldErrors").size());
    }

    @Test
    void garbageTokenGets401() throws Exception {
        HttpResponse<String> response = get("/api/ping", "not-a-real-token");

        assertEquals(401, response.statusCode());
        assertEquals("UNAUTHORIZED", mapper.readTree(response.body()).get("error").get("code").asString());
    }
}