package one.xis.plugin;

import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class XISGenerateNativeRunnerTaskTest {

    @TempDir
    Path projectDir;

    @Test
    void generatesNativeAppEntryPoint() throws IOException {
        var sourceFile = write("src/main/java/example/DemoApplication.java", """
                package example;

                import one.xis.boot.XISBootApplication;

                @XISBootApplication
                public class DemoApplication {
                }
                """);
        var project = ProjectBuilder.builder()
                .withProjectDir(projectDir.toFile())
                .build();
        var task = project.getTasks().create("xisGenerateNativeRunner", XISGenerateNativeRunnerTask.class);
        task.getSourceFiles().from(sourceFile);
        task.getOutputDirectory().set(projectDir.resolve("build/generated").toFile());

        task.generate();

        var generatedFile = projectDir.resolve("build/generated/one/xis/boot/nativeimage/NativeApp.java");
        assertTrue(Files.exists(generatedFile));
        var generatedSource = Files.readString(generatedFile);
        assertTrue(generatedSource.contains("public final class NativeApp"));
        assertTrue(generatedSource.contains("XISBootNativeRunner.run("));
        assertTrue(generatedSource.contains("example.DemoApplication.class"));
        assertFalse(generatedSource.contains("public final class NativeRunner"));
    }

    private Path write(String relativePath, String content) throws IOException {
        var path = projectDir.resolve(relativePath);
        Files.createDirectories(path.getParent());
        Files.writeString(path, content);
        return path;
    }
}
