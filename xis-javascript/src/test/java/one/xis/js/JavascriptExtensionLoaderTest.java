package one.xis.js;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class JavascriptExtensionLoaderTest {

    @TempDir
    Path tempDir;

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

    @Test
    void loadsExtensionsFromSeveralClasspathArtifacts() throws Exception {
        var firstRoot = resourceRoot("first", "js/first-artifact-extension.js", "window.firstArtifactExtension = true;\n");
        var secondRoot = resourceRoot("second", "js/second-artifact-extension.js", "window.secondArtifactExtension = true;\n");

        try (var classLoader = new URLClassLoader(new java.net.URL[]{firstRoot.toUri().toURL(), secondRoot.toUri().toURL()},
                Thread.currentThread().getContextClassLoader())) {
            var extensions = withContextClassLoader(classLoader, () -> loader.loadExtensions());

            assertThat(extensions)
                    .containsEntry("js/first-artifact-extension.js", "window.firstArtifactExtension = true;\n")
                    .containsEntry("js/second-artifact-extension.js", "window.secondArtifactExtension = true;\n");
        }
    }

    private Path resourceRoot(String directory, String scriptPath, String scriptContent) throws Exception {
        var root = tempDir.resolve(directory);
        var registry = root.resolve("META-INF/xis/js/extensions");
        var script = root.resolve(scriptPath);
        Files.createDirectories(registry.getParent());
        Files.createDirectories(script.getParent());
        Files.writeString(registry, scriptPath + "\n");
        Files.writeString(script, scriptContent);
        return root;
    }

    private <T> T withContextClassLoader(ClassLoader classLoader, ThrowingSupplier<T> supplier) throws Exception {
        var original = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(classLoader);
        try {
            return supplier.get();
        } finally {
            Thread.currentThread().setContextClassLoader(original);
        }
    }

    private interface ThrowingSupplier<T> {
        T get() throws Exception;
    }
}
