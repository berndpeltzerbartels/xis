package one.xis.resource;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResourcesTest {

    @TempDir
    Path tempDir;

    @Test
    void resolvesDevelopmentResourceFromJavaSourcesBeforeResourceSources() throws Exception {
        Path javaSource = write("src/main/java/example/Page.html", "java-source");
        write("src/main/resources/example/Page.html", "resource-source");
        Resources resources = resources();

        Resource resource = resources.getByPath("example/Page.html");

        assertEquals("java-source", resource.getContent().trim());
        assertEquals(javaSource.toFile(), resources.getDevelopmentFile("example/Page.html").orElseThrow());
    }

    @Test
    void reloadsDevelopmentResourceWhenSourceFileChanges() throws Exception {
        Path source = write("src/main/resources/example/Page.html", "first");
        Resources resources = resources();
        Resource resource = resources.getByPath("example/Page.html");

        Files.writeString(source, "second");
        source.toFile().setLastModified(System.currentTimeMillis() + 1_000);

        assertEquals("second", resource.getContent().trim());
        assertTrue(resource.getLastModified() >= source.toFile().lastModified());
    }

    @Test
    void existsChecksDevelopmentSources() throws Exception {
        write("src/main/java/example/Page.html", "content");

        assertTrue(resources().exists("example/Page.html"));
    }

    private Resources resources() {
        return Resources.withDevelopmentResourceRoots(List.of(
                tempDir.resolve("src/main/java"),
                tempDir.resolve("src/main/resources")
        ));
    }

    private Path write(String path, String content) throws Exception {
        Path file = tempDir.resolve(path);
        Files.createDirectories(file.getParent());
        Files.writeString(file, content);
        return file;
    }
}
