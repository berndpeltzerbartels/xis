package one.xis.plugin;

import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class XISBootRunTaskTest {

    @TempDir
    Path projectDir;

    @Test
    void xisRunHelpCanRenderTaskOptions() throws IOException {
        write("settings.gradle", "rootProject.name = 'xis-run-task-test'\n");
        write("build.gradle", """
                plugins {
                    id 'java'
                    id 'one.xis.plugin'
                }

                repositories {
                    mavenLocal()
                    mavenCentral()
                }

                dependencies {
                    implementation 'one.xis:xis-boot:0.11.1'
                }
                """);

        var result = GradleRunner.create()
                .withProjectDir(projectDir.toFile())
                .withPluginClasspath()
                .withArguments("help", "--task", "xisRun")
                .build();

        assertEquals(SUCCESS, result.task(":help").getOutcome());
        assertTrue(result.getOutput().contains("--port"));
        assertTrue(result.getOutput().contains("--application-args"));
    }

    private void write(String relativePath, String content) throws IOException {
        var path = projectDir.resolve(relativePath);
        Files.createDirectories(path.getParent() == null ? projectDir : path.getParent());
        Files.writeString(path, content);
    }
}
