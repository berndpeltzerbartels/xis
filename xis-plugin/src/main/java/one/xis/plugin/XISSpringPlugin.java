package one.xis.plugin;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.api.tasks.Sync;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class XISSpringPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        addDependencies(project);
        configureResources(project);
        checkGradleJavaVersion(project);
        checkCompilerJavaVersion(project);
    }

    /**
     *  Adds the required dependencies to the project
     *
     * @param project
     */
    private void addDependencies(Project project) {
        var version = xisVersion();
        project.getDependencies().add("implementation", "one.xis:xis-spring:"+version);
        project.getDependencies().add("implementation", "one.xis:xis-remote-core:"+version);
        project.getDependencies().add("testImplementation", "one.xis:xis-test:"+version);
    }

    /**
     *  Configures to look for resources in the src/main/java directory
     *
     * @param project
     */
    private void configureResources(Project project) {
        // Zugriff auf das ProcessResources-Task
        project.getTasks().withType(Sync.class).configureEach(sync -> {
            // Füge Ressourcen aus dem Verzeichnis src/main/resources hinzu
            sync.from(project.file("src/main/java"));
            sync.into(project.getBuildDir().toPath().resolve("resources/main"));
        });
    }

    /**
     *  Checks if the Gradle Java version is 17
     *
     * @param project
     */
    private void checkGradleJavaVersion(Project project) {
        String gradleJavaVersion = System.getProperty("java.version");
        if (!gradleJavaVersion.startsWith(javaTargetVersion())) {
            throw new IllegalStateException("Gradle requires Java 17 but found Java " + gradleJavaVersion);
        }
    }

    /**
     *  Checks if the compiler Java version is 17
     *
     * @param project
     */
    private void checkCompilerJavaVersion(Project project) {
        project.getTasks().withType(JavaCompile.class, javaCompile -> {
            String compilerVersion = javaCompile.getOptions().getCompilerArgs().stream()
                    .filter(arg -> arg.startsWith("-source"))
                    .findFirst()
                    .map(arg -> arg.split("=")[1])
                    .orElse("Unknown");

            if (!compilerVersion.equals(javaTargetVersion())) {
                throw new IllegalStateException("The compiler Java version must be 17, but found " + compilerVersion);
            }
        });
    }

    private String xisVersion() {
        return readContent("/xis-version.txt").trim();
    }

    private String javaTargetVersion() {
            return readContent("/java-target-version.txt").trim();
    }

    private String readContent(String resource) {
        try {
            return Files.readString(Paths.get(getClass().getResource(resource).toURI()));
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }


}
