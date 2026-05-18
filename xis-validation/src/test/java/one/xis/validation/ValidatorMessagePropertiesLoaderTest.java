package one.xis.validation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

class ValidatorMessagePropertiesLoaderTest {

    @TempDir
    Path tempDir;

    @Test
    void loadsMessagesFromMultipleClasspathLocations() throws Exception {
        Path first = resourceRoot("first",
                "messages.properties",
                """
                        first.message=from first
                        shared.message=from first
                        """);
        Path second = resourceRoot("second",
                "messages.properties",
                """
                        second.message=from second
                        shared.message=from second
                        """);

        try (var classLoader = new URLClassLoader(new java.net.URL[]{first.toUri().toURL(), second.toUri().toURL()}, null)) {
            var loader = loaderWithContextClassLoader(classLoader);

            assertThat(loader.getMessage("first.message", Locale.ROOT)).isEqualTo("from first");
            assertThat(loader.getMessage("second.message", Locale.ROOT)).isEqualTo("from second");
            assertThat(loader.getMessage("shared.message", Locale.ROOT)).isEqualTo("from first");
        }
    }

    @Test
    void localeSpecificMessagesOverrideBaseMessages() throws Exception {
        Path root = resourceRoot("localized",
                "messages.properties",
                "totp.enrollment.link=Register authenticator\n");
        Files.writeString(root.resolve("messages_de.properties"), "totp.enrollment.link=Authenticator registrieren\n");

        try (var classLoader = new URLClassLoader(new java.net.URL[]{root.toUri().toURL()}, null)) {
            var loader = loaderWithContextClassLoader(classLoader);

            assertThat(loader.getMessage("totp.enrollment.link", Locale.GERMAN))
                    .isEqualTo("Authenticator registrieren");
        }
    }

    private ValidatorMessagePropertiesLoader loaderWithContextClassLoader(ClassLoader classLoader) {
        var original = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(classLoader);
        try {
            return new ValidatorMessagePropertiesLoader();
        } finally {
            Thread.currentThread().setContextClassLoader(original);
        }
    }

    private Path resourceRoot(String directory, String file, String content) throws Exception {
        Path root = tempDir.resolve(directory);
        Files.createDirectories(root);
        Files.writeString(root.resolve(file), content);
        return root;
    }
}
