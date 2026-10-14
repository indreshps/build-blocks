package io.github.buildblocks.apiresponse.integration;

import io.github.buildblocks.apiresponse.autoconfigure.ApiResponseAutoConfiguration;
import io.github.buildblocks.apiresponse.filter.RequestIdFilter;
import io.github.buildblocks.apiresponse.handler.ApiExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest(
        classes = ApiResponseDisabledIntegrationTest.TestApp.class
)
@TestPropertySource(properties = {
        "buildblocks.api-response.enabled=false"
})
class ApiResponseDisabledIntegrationTest {

    @Autowired
    private ApplicationContext context;

    @Test
    void shouldNotRegisterStarterBeansWhenDisabled() {
        
        assertFalse(context.getBeansOfType(ApiExceptionHandler.class).size() > 0);

        assertFalse(context.getBeansOfType(RequestIdFilter.class).size() > 0);
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @Import(ApiResponseAutoConfiguration.class)
    static class TestApp {
    }
}