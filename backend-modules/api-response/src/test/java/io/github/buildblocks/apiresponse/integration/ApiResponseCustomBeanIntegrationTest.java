package io.github.buildblocks.apiresponse.integration;


import io.github.buildblocks.apiresponse.filter.RequestIdFilter;
import io.github.buildblocks.apiresponse.handler.ApiExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(
        classes = ApiResponseCustomBeanIntegrationTest.TestApp.class
)
class ApiResponseCustomBeanIntegrationTest {

    @Autowired
    private ApplicationContext context;

    @Test
    void shouldRegisterOnlyOneHandlerAndOneFilter() {
        assertEquals(
                1,
                context.getBeansOfType(ApiExceptionHandler.class).size()
        );

        assertEquals(
                1,
                context.getBeansOfType(RequestIdFilter.class).size()
        );
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    static class TestApp {

        @Bean
        ApiExceptionHandler customApiExceptionHandler() {
            return new ApiExceptionHandler();
        }

        @Bean
        RequestIdFilter customRequestIdFilter() {
            return new RequestIdFilter();
        }
    }
}