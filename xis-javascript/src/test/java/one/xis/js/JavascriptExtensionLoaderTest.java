package one.xis.js;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JavascriptExtensionLoaderTest {

    private JavascriptExtensionLoader loader;

    @BeforeEach
    void setUp() {
        loader = new JavascriptExtensionLoader();
    }

    @Test
    void shouldLoadExtensionsFromClasspath() {
        // Act
        List<String> extensions = loader.loadExtensions();

        // Assert
        assertThat(extensions).isNotNull();
        assertThat(extensions).hasSize(2); // Assuming there are 2 extensions in the test resources

        // Check that the loaded extensions contain expected content
        assertTrue(extensions.stream().anyMatch(js -> js.contains("console.log(\"Extension 1 loaded\");")));
        assertTrue(extensions.stream().anyMatch(js -> js.contains("console.log(\"Extension 2 loaded\");")));
    }
}
