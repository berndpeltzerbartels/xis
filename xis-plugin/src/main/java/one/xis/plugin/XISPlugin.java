package one.xis.plugin;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.Sync;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;


/**
 * XISPlugin is a Gradle plugin that configures a project to use the XIS framework.
 * It ensures that the project uses Java 17, adds necessary dependencies, and configures resources.
 */
public class XISPlugin implements Plugin<Project> {
    @Override
    public void apply(@NotNull Project project) {
        configureDependencyManagement(project);
        configureResources(project);
    }


    /**
     * Configures dependency constraints for optional XIS modules. This allows users
     * to add a dependency without specifying the version.
     *
     * @param project The project to configure.
     */
    private void configureDependencyManagement(Project project) {
        var version = pluginVersion();
        var constraints = project.getDependencies().getConstraints();
        constraints.add("implementation", "one.xis:xis-spring:" + version);
        constraints.add("implementation", "one.xis:xis-authentication:" + version);
        constraints.add("implementation", "one.xis:xis-idp-server:" + version);
        constraints.add("implementation", "one.xis:xis-bootstrap:" + version);
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


    private String pluginVersion() {
        // Die Ressource befindet sich im Stammverzeichnis des JARs, daher der führende Schrägstrich.
        try (InputStream input = getClass().getResourceAsStream("/plugin-version.txt")) {
            if (input == null) {
                // Diese Datei sollte vom Build-Prozess erstellt und verpackt worden sein.
                // Wenn sie fehlt, liegt ein Build-Problem vor.
                throw new IllegalStateException("Konnte die Ressource 'plugin-version.txt' nicht finden. Stellen Sie sicher, dass das Plugin korrekt gebaut wurde.");
            }
            // Lese den gesamten Inhalt des Streams als String.
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
                return reader.lines().collect(Collectors.joining(System.lineSeparator())).trim();
            }
        } catch (IOException ex) {
            throw new RuntimeException("Fehler beim Lesen von 'plugin-version.txt'", ex);
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
