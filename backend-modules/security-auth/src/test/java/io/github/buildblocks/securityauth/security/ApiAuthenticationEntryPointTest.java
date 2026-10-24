package io.github.buildblocks.securityauth.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;

import io.github.buildblocks.apiresponse.filter.RequestIdFilter;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

class ApiAuthenticationEntryPointTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ApiAuthenticationEntryPoint entryPoint = new ApiAuthenticationEntryPoint(objectMapper);

    @Test
    void returnsStandardUnauthorizedJson() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/expenses");
        request.setAttribute(RequestIdFilter.REQUEST_ID_ATTRIBUTE, "req-test-123");
        MockHttpServletResponse response = new MockHttpServletResponse();

        entryPoint.commence(request, response, new BadCredentialsException("bad"));

        assertEquals(401, response.getStatus());
        assertTrue(response.getContentType().startsWith("application/json"));

        JsonNode body = objectMapper.readTree(response.getContentAsString());
        assertFalse(body.get("success").asBoolean());
        assertEquals("UNAUTHORIZED", body.get("error").get("code").asString());
        assertEquals("req-test-123", body.get("error").get("requestId").asString());
        assertEquals("/api/expenses", body.get("path").asString());
        assertTrue(body.get("fieldErrors").isEmpty());
    }

    @Test
    void worksWithoutARequestId() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/expenses");
        MockHttpServletResponse response = new MockHttpServletResponse();

        entryPoint.commence(request, response, new BadCredentialsException("bad"));

        assertEquals(401, response.getStatus());
        JsonNode body = objectMapper.readTree(response.getContentAsString());
        assertEquals("UNAUTHORIZED", body.get("error").get("code").asString());
    }
}