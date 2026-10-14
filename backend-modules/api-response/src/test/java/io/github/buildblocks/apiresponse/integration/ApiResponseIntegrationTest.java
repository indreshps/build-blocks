package io.github.buildblocks.apiresponse.integration;

import io.github.buildblocks.apiresponse.autoconfigure.ApiResponseAutoConfiguration;
import io.github.buildblocks.apiresponse.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.test.context.TestPropertySource;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = ApiResponseIntegrationTest.TestApp.class)
@AutoConfigureMockMvc
class ApiResponseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnConsistentErrorAndRequestId() throws Exception {
        mockMvc.perform(get("/test/missing")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(header().exists("X-Request-ID"))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code")
                        .value("RESOURCE_NOT_FOUND"))
                .andExpect(jsonPath("$.error.message")
                        .value("Test resource not found"))
                .andExpect(jsonPath("$.error.requestId").isNotEmpty())
                .andExpect(jsonPath("$.path").value("/test/missing"))
                .andExpect(jsonPath("$.fieldErrors").isArray());
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @Import({
            ApiResponseAutoConfiguration.class,
            TestController.class
    })
    static class TestApp {
    }

    @RestController
    static class TestController {

        @GetMapping("/test/missing")
        String missingResource() {
            throw new ResourceNotFoundException(
                    "Test resource not found"
            );
        }
    }
}