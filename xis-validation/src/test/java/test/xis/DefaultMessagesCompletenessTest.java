package test.xis;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultMessagesCompletenessTest {

    @Test
    void localizedDefaultMessagesContainTheSameKeysAsBaseBundle() throws IOException {
        var resourcesDir = resourcesDir();
        var baseKeys = load(resourcesDir.resolve("default-messages.properties")).keySet();

        try (var files = Files.list(resourcesDir)) {
            var localizedBundles = files
                    .filter(path -> path.getFileName().toString().matches("default-messages_[a-z]{2}\\.properties"))
                    .toList();

            assertThat(localizedBundles).isNotEmpty();
            localizedBundles.forEach(path -> assertThat(load(path).keySet())
                    .as(path.getFileName().toString())
                    .containsExactlyInAnyOrderElementsOf(baseKeys));
        }
    }

    private Path resourcesDir() {
        var rootRelative = Path.of("xis-validation/src/main/resources");
        if (Files.isDirectory(rootRelative)) {
            return rootRelative;
        }
        return Path.of("src/main/resources");
    }

    private Properties load(Path path) {
        var properties = new Properties();
        try (var reader = new InputStreamReader(Files.newInputStream(path), StandardCharsets.UTF_8)) {
            properties.load(reader);
        } catch (IOException e) {
            throw new IllegalStateException("Could not load " + path, e);
        }
        return properties;
    }
}
