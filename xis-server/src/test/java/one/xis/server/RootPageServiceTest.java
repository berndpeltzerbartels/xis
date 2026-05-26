package one.xis.server;

import one.xis.resource.Resources;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class RootPageServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void addsPublicJavascriptFromClasspathArtifactsToRootPage() throws Exception {
        var artifactRoot = resourceRoot("web-artifact");
        Files.writeString(artifactRoot.resolve("index.html"), """
                <!DOCTYPE html>
                <html>
                <head><title>Test</title></head>
                <body></body>
                </html>
                """);
        write(artifactRoot, "public/js/artifact-extension.js", "window.artifactExtensionLoaded = true;\n");

        try (var classLoader = new URLClassLoader(new java.net.URL[]{artifactRoot.toUri().toURL()},
                Thread.currentThread().getContextClassLoader())) {
            var rootPageHtml = withContextClassLoader(classLoader, () -> {
                var service = new RootPageService(new Resources());
                service.init();
                return service.getRootPageHtml();
            });

            assertThat(rootPageHtml).contains("src=\"/js/artifact-extension.js\"");
        }
    }

    @Test
    void addsReplaceableDefaultMainCssOnlyOnce() throws Exception {
        var artifactRoot = resourceRoot("default-css-artifact");
        Files.writeString(artifactRoot.resolve("index.html"), """
                <!DOCTYPE html>
                <html>
                <head><title>Test</title></head>
                <body></body>
                </html>
                """);
        write(artifactRoot, "public/default-main.css", "body { color: black; }\n");

        try (var classLoader = new URLClassLoader(new java.net.URL[]{artifactRoot.toUri().toURL()},
                Thread.currentThread().getContextClassLoader())) {
            var rootPageHtml = withContextClassLoader(classLoader, () -> {
                var service = new RootPageService(new Resources());
                service.init();
                return service.getRootPageHtml();
            });

            assertThat(rootPageHtml).contains("href=\"/default-main.css\"");
            assertThat(countOccurrences(rootPageHtml, "href=\"/default-main.css\"")).isEqualTo(1);
        }
    }

    private int countOccurrences(String value, String needle) {
        var count = 0;
        var index = value.indexOf(needle);
        while (index >= 0) {
            count++;
            index = value.indexOf(needle, index + needle.length());
        }
        return count;
    }

    private Path resourceRoot(String directory) throws Exception {
        var root = tempDir.resolve(directory);
        Files.createDirectories(root);
        return root;
    }

    private void write(Path root, String path, String content) throws Exception {
        var file = root.resolve(path);
        Files.createDirectories(file.getParent());
        Files.writeString(file, content);
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
