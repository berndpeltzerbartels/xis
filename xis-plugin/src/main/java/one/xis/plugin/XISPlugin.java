package one.xis.plugin;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.Sync;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Set;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public class XISPlugin implements Plugin<Project> {

    @Override
    public void apply(@NotNull Project project) {
        project.getPlugins().apply(JavaPlugin.class);

        configureDependencyManagement(project);
        configureResources(project);
        configureTemplatesTask(project);
    }

    /* ----------------------------- deps & resources ----------------------------- */

    private void configureDependencyManagement(Project project) {
        var version = pluginVersion();
        var constraints = project.getDependencies().getConstraints();
        constraints.add("implementation", "one.xis:xis-spring:" + version);
        constraints.add("implementation", "one.xis:xis-authentication:" + version);
        constraints.add("implementation", "one.xis:xis-idp-server:" + version);
        constraints.add("implementation", "one.xis:xis-bootstrap:" + version);
        constraints.add("implementation", "one.xis:xis-theme:" + version);
        constraints.add("testImplementation", "one.xis:xis-test:" + version);
    }

    /**
     * keep: copy src/main/java into resources
     */
    private void configureResources(Project project) {
        project.getTasks().withType(Sync.class).configureEach(sync -> {
            sync.from(project.file("src/main/java"));
            sync.into(project.getBuildDir().toPath().resolve("resources/main"));
        });
    }

    /* ----------------------------- templates task ------------------------------- */

    private void configureTemplatesTask(Project project) {
        SourceSet main = mainSourceSet(project);

        Configuration apClasspath = buildApClasspath(project);

        File javaSrcBase = javaSourceBase(main, project);         // default
        File resourcesOutput = new File(project.getProjectDir(), "src/main/resources"); // --useResources

        project.getTasks().register("templates", XISTemplateTask.class, task -> {
            task.setGroup("xis");
            task.setDescription("Runs the XIS annotation processor to scaffold missing templates (no compilation).");

            // inputs for javac/AP
            task.setSource(main.getAllJava());
            task.setClasspath(main.getCompileClasspath());

            // AP path: only the processor, pinned to plugin version
            task.getOptions().setAnnotationProcessorPath(apClasspath);

            // fixed processor FQCN & output roots
            task.getProcessorFqcn().set("one.xis.processor.TemplateProcessor");
            task.getDefaultJavaOutputDir().set(javaSrcBase);
            task.getResourcesOutputDir().set(resourcesOutput);

            // lock to prevent user mutation
            lock(task);

            // up-to-date inputs
            task.getInputs()
                    .files(main.getAllJava().getSourceDirectories())
                    .withPropertyName("xisSources");
        });
    }

    /* ----------------------------- helpers -------------------------------------- */

    private SourceSet mainSourceSet(Project project) {
        SourceSetContainer sets = project.getExtensions().getByType(SourceSetContainer.class);
        return sets.getByName(SourceSet.MAIN_SOURCE_SET_NAME);
    }

    /**
     * Build AP classpath: only xis-apt pinned to this plugin's version (user cannot override).
     */
    private Configuration buildApClasspath(Project project) {
        String apCoord = "one.xis:xis-apt:" + pluginVersion();
        Dependency apDep = project.getDependencies().create(apCoord);
        Configuration apConf = project.getConfigurations().detachedConfiguration(apDep);
        apConf.setCanBeConsumed(false);
        apConf.setCanBeResolved(true);
        apConf.setTransitive(true);
        return apConf;
    }

    /**
     * First Java src dir or fallback to src/main/java.
     */
    private File javaSourceBase(SourceSet main, Project project) {
        Set<File> srcDirs = main.getJava().getSrcDirs();
        return srcDirs.isEmpty()
                ? new File(project.getProjectDir(), "src/main/java")
                : srcDirs.iterator().next();
    }

    private void lock(XISTemplateTask task) {
        task.getProcessorFqcn().finalizeValue();
        task.getProcessorFqcn().disallowChanges();
        task.getDefaultJavaOutputDir().finalizeValue();
        task.getDefaultJavaOutputDir().disallowChanges();
        task.getResourcesOutputDir().finalizeValue();
        task.getResourcesOutputDir().disallowChanges();
    }

    /* ----------------------------- version & io --------------------------------- */

    private String pluginVersion() {
        try (InputStream input = getClass().getResourceAsStream("/plugin-version.txt")) {
            if (input == null) {
                throw new IllegalStateException("Unable to find 'plugin-version.txt' in classpath.");
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
                return reader.lines().collect(Collectors.joining(System.lineSeparator())).trim();
            }
        } catch (IOException ex) {
            throw new RuntimeException("Error reading 'plugin-version.txt' from classpath.", ex);
        }
    }

    @SuppressWarnings("unused")
    private String readContent(String resource) {
        try {
            return Files.readString(Paths.get(getClass().getResource(resource).toURI()));
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
