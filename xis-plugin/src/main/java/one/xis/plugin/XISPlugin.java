package one.xis.plugin;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.Sync;
import org.gradle.api.tasks.compile.JavaCompile;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;


/**
 * XISPlugin is a Gradle plugin that configures a project to use the XIS framework.
 * It ensures that the project uses Java 17, adds necessary dependencies, and configures resources.
 */
public class XISPlugin implements Plugin<Project> {
    @Override
    public void apply(@NotNull Project project) {
        configureDependencyManagement(project);
        configureResources(project);
        // checkJavaHome();
        // checkGradleJavaVersion(project);
        // checkCompilerJavaVersion(project);
    }

    /**
     * Adds the required dependencies to the project
     *
     * @param project
     */
    private void addDependencies(Project project) {
        var version = xisVersion();
        /*
        project.getDependencies().add("implementation", "one.xis:xis-spring:"+version);
        project.getDependencies().add("implementation", "one.xis:xis-remote-core:"+version);
        project.getDependencies().add("testImplementation", "one.xis:xis-test:"+version);

         */
    }

    /**
     * Configures dependency constraints for optional XIS modules. This allows users
     * to add a dependency without specifying the version.
     *
     * @param project The project to configure.
     */
    private void configureDependencyManagement(Project project) {
        var version = xisVersion();
        var constraints = project.getDependencies().getConstraints();
        constraints.add("implementation", "one.xis:xis-spring:" + version);
        constraints.add("implementation", "one.xis:xis-authentication:" + version);
        constraints.add("implementation", "one.xis:xis-idp-server:" + version);
        constraints.add("testImplementation", "one.xis:xis-test:" + version);
    }

    /**
     * Configures to look for resources in the src/main/java directory
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
     * Checks if the Gradle Java version is 17
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
     * Checks if the JAVA_HOME environment variable is set and points to a valid Java installation
     * of version 17
     */
    private void checkJavaHome() {
        String javaHome = System.getProperty("java.home");
        if (javaHome == null) {
            throw new IllegalStateException("JAVA_HOME environment variable is not set");
        }
        var targetVersion = javaTargetVersion();
        if (!javaHome.contains(javaTargetVersion())) {
            throw new IllegalStateException("JAVA_HOME: '" + javaHome + "' environment variable does not point to a Java " + targetVersion + " installation");
        }
    }

    /**
     * Checks if the compiler Java version is 17
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
        try (InputStream input = getClass().getResourceAsStream("/xis.properties")) {
            Properties prop = new Properties();
            if (input == null) {
                throw new IllegalStateException("Sorry, unable to find xis.properties");
            }
            prop.load(input);
            return prop.getProperty("version");
        } catch (IOException ex) {
            throw new RuntimeException("Error reading xis.properties", ex);
        }
    }

    private String javaTargetVersion() {
        return "17";
    }

    private String readContent(String resource) {
        try {
            return Files.readString(Paths.get(getClass().getResource(resource).toURI()));
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }


}
