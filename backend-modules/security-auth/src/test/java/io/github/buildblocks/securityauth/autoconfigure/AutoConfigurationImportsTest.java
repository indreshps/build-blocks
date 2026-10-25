package io.github.buildblocks.securityauth.autoconfigure;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import org.junit.jupiter.api.Test;

class AutoConfigurationImportsTest {

    private static final String IMPORTS_FILE =
            "META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports";

    @Test
    void importsFileListsSecurityAuthAutoConfiguration() throws IOException {
        boolean found = false;

        for (URL url : Collections.list(getClass().getClassLoader().getResources(IMPORTS_FILE))) {
            try (InputStream in = url.openStream()) {
                String content = new String(in.readAllBytes(), StandardCharsets.UTF_8);
                if (content.contains(SecurityAuthAutoConfiguration.class.getName())) {
                    found = true;
                }
            }
        }

        assertTrue(found,
                "No imports file at " + IMPORTS_FILE + " lists SecurityAuthAutoConfiguration");
    }
}