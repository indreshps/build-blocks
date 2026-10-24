package io.github.buildblocks.securityauth.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;

import io.github.buildblocks.apiresponse.filter.RequestIdFilter;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

class ApiAccessDeniedHandlerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ApiAccessDeniedHandler handler = new ApiAccessDeniedHandler(objectMapper);

    @Test
    void returnsStandardForbiddenJson() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("DELETE", "/api/admin/users/1");
        request.setAttribute(RequestIdFilter.REQUEST_ID_ATTRIBUTE, "req-test-456");
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.handle(request, response, new AccessDeniedException("denied"));

        assertEquals(403, response.getStatus());
        assertTrue(response.getContentType().startsWith("application/json"));

        JsonNode body = objectMapper.readTree(response.getContentAsString());
        assertFalse(body.get("success").asBoolean());
        assertEquals("FORBIDDEN", body.get("error").get("code").asString());
        assertEquals("req-test-456", body.get("error").get("requestId").asString());
        assertEquals("/api/admin/users/1", body.get("path").asString());
        assertTrue(body.get("fieldErrors").isEmpty());
    }

    @Test
    void worksWithoutARequestId() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/admin");
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.handle(request, response, new AccessDeniedException("denied"));

        assertEquals(403, response.getStatus());
        JsonNode body = objectMapper.readTree(response.getContentAsString());
        assertEquals("FORBIDDEN", body.get("error").get("code").asString());
    }
}