package io.github.buildblocks.apiresponse.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class AutoConfigurationImportsTest {

    private static final String IMPORTS_FILE =
            "META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports";

    @Test
    void importsFileExistsAndListsAutoConfiguration() throws Exception {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(IMPORTS_FILE)) {
            assertThat(in)
                    .as("Spring Boot imports file must exist at " + IMPORTS_FILE)
                    .isNotNull();

            String content = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            assertThat(content).contains(ApiResponseAutoConfiguration.class.getName());
        }
    }
}