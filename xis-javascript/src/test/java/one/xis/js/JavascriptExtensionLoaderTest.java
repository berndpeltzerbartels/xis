package one.xis.js;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class JavascriptExtensionLoaderTest {

    private JavascriptExtensionLoader loader;

    @BeforeEach
    void setUp() {
        loader = new JavascriptExtensionLoader();
    }

    @Test
    void shouldLoadExtensionsFromClasspath() {
        // Act
        Map<String, String> extensions = loader.loadExtensions();

        // Assert
        assertThat(extensions).isNotNull();
        assertThat(extensions.size()).isGreaterThan(1); // 2 are always present

        // Check that the loaded extensions contain expected content
        assertThat(extensions.get("extension1.js")).isEqualTo("console.log(\"Extension 1 loaded\");\n");
        assertThat(extensions.get("extension2.js")).isEqualTo("console.log(\"Extension 2 loaded\");\n");
    }
}
