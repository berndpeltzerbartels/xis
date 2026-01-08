package one.xis.plugin;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.language.jvm.tasks.ProcessResources;
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
        configureTestsTask(project);
        configureFatJarCreation(project);
    }

    /* ----------------------------- deps & resources ----------------------------- */

    private void configureDependencyManagement(Project project) {
        var version = pluginVersion();
        var constraints = project.getDependencies().getConstraints();
        constraints.add("implementation", "one.xis:xis-boot:" + version);
        constraints.add("implementation", "one.xis:xis-spring:" + version);
        constraints.add("implementation", "one.xis:xis-authentication:" + version);
        constraints.add("implementation", "one.xis:xis-idp-server:" + version);
        constraints.add("implementation", "one.xis:xis-bootstrap:" + version);
        constraints.add("implementation", "one.xis:xis-theme:" + version);
        constraints.add("implementation", "one.xis:xis-util:" + version);
        constraints.add("implementation", "one.xis:xis-http:" + version);
        constraints.add("testImplementation", "one.xis:xis-test:" + version);
        project.getDependencies().add("annotationProcessor", "one.xis:xis-apt:" + version);
    }

    /**
     * keep: copy src/main/java into build/resources/main (not src/main/resources to avoid Git issues)
     */
    private void configureResources(Project project) {
        project.getTasks().withType(ProcessResources.class).configureEach(sync -> {
            File javaSrcBase = javaSourceBase(mainSourceSet(project), project);
            File buildResourceDir = new File(project.getBuildDir(), "resources/main");
            new HtmlSynchronizer(javaSrcBase, buildResourceDir).sync();
        });
    }


    private void configureFatJarCreation(Project project) {
        project.afterEvaluate(p -> {
            boolean usesXisBoot = project.getConfigurations()
                    .getByName("implementation")
                    .getAllDependencies()
                    .stream()
                    .anyMatch(dep -> dep.getGroup() != null && dep.getGroup().equals("one.xis") && dep.getName().equals("xis-boot"));

            if (usesXisBoot) {
                project.getTasks().register("runnableJar", XISBootRunnableJarTask.class, jar -> {
                    jar.getManifest().getAttributes().put("Main-Class", "one.xis.boot.Runner");
                    jar.configure(project);
                    jar.dependsOn(
                            project.getTasks().getByName("processResources"),
                            project.getTasks().getByName("compileJava")
                    );
                });
            }
        });
    }



    /* ----------------------------- templates task ------------------------------- */

    private void configureTemplatesTask(Project project) {
        SourceSet main = mainSourceSet(project);

        Configuration apClasspath = buildApClasspath(project);

        // Templates should go to src/main/java, not build output
        File sourceJavaDir = main.getAllJava().getSourceDirectories().getSingleFile();

        project.getTasks().register("templates", XISTemplateTask.class, task -> {
            task.setGroup("xis");
            task.setDescription("Runs the XIS annotation processor to scaffold missing templates (no compilation).");

            // inputs for javac/AP
            task.setSource(main.getAllJava());
            task.setClasspath(main.getCompileClasspath());

            // AP path: only the processor, pinned to plugin version
            task.getOptions().setAnnotationProcessorPath(apClasspath);

            // fixed processor FQCN & output to src/main/java
            task.getProcessorFqcn().set("one.xis.processor.XISTemplateProcessor");
            task.getOutputDir().set(sourceJavaDir);

            // lock to prevent user mutation
            lock(task);

            // Always run - don't cache based on inputs
            // This ensures deleted templates are regenerated
            task.getOutputs().upToDateWhen(t -> false);

            // up-to-date inputs
            task.getInputs()
                    .files(main.getAllJava().getSourceDirectories())
                    .withPropertyName("xisSources");
        });
    }

    /* ----------------------------- tests task ----------------------------------- */

    private void configureTestsTask(Project project) {
        SourceSet main = mainSourceSet(project);
        SourceSetContainer sets = project.getExtensions().getByType(SourceSetContainer.class);
        SourceSet test = sets.getByName(SourceSet.TEST_SOURCE_SET_NAME);

        Configuration apClasspath = buildApClasspath(project);

        // Tests should go to src/test/java
        File testJavaDir = test.getAllJava().getSourceDirectories().getSingleFile();

        project.getTasks().register("tests", XISTestTask.class, task -> {
            task.setGroup("xis");
            task.setDescription("Runs the XIS annotation processor to scaffold missing test files (no compilation).");

            // inputs for javac/AP
            task.setSource(main.getAllJava());
            task.setClasspath(main.getCompileClasspath());

            // AP path: only the processor, pinned to plugin version
            task.getOptions().setAnnotationProcessorPath(apClasspath);

            // fixed processor FQCN & output to src/test/java
            task.getProcessorFqcn().set("one.xis.processor.XISTestProcessor");
            task.getOutputDir().set(testJavaDir);

            // lock to prevent user mutation
            lockTestTask(task);

            // Always run - don't cache based on inputs
            // This ensures deleted tests are regenerated
            task.getOutputs().upToDateWhen(t -> false);

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
        task.getOutputDir().finalizeValue();
        task.getOutputDir().disallowChanges();
    }

    private void lockTestTask(XISTestTask task) {
        task.getProcessorFqcn().finalizeValue();
        task.getProcessorFqcn().disallowChanges();
        task.getOutputDir().finalizeValue();
        task.getOutputDir().disallowChanges();
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

    static class HtmlSynchronizer {
        private final File src;
        private final File dest;

        HtmlSynchronizer(File src, File dest) {
            this.src = src;
            this.dest = dest;
        }

        void sync() {
            syncFile(src, dest);
        }

        private void syncFile(File sourceFile, File destFile) {
            File[] files = sourceFile.listFiles();
            if (files == null) {
                return;
            }
            for (File file : files) {
                File targetFile = new File(destFile, file.getName());
                if (file.isDirectory()) {
                    syncFile(file, targetFile);
                } else if (file.isFile() && file.getName().endsWith(".html")) {
                    if (targetFile.exists()) {
                        targetFile.delete();
                    }
                    File dir = targetFile.getParentFile();
                    if (!dir.exists() && !dir.mkdirs()) {
                        throw new IllegalStateException("Failed to create directory: " + dir);
                    }
                    try {
                        Files.copy(file.toPath(), targetFile.toPath());
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                }
            }
        }
    }
}
