package one.xis.plugin;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.ProjectDependency;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.bundling.AbstractArchiveTask;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.compile.GroovyCompile;
import org.gradle.api.tasks.testing.Test;
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
        configureFrameworkComponentGeneration(project);
        configureTemplatesTask(project);
        configureTestsTask(project);
        configureValidateTask(project);
        configureFatJarCreation(project);
        configureNativeSupport(project);
        configureGroovySupport(project);
    }

    /* ----------------------------- deps & resources ----------------------------- */

    private void configureDependencyManagement(Project project) {
        var version = pluginVersion();
        var constraints = project.getDependencies().getConstraints();
        for (String module : xisModules()) {
            constraints.add("implementation", "one.xis:" + module + ":" + version);
            constraints.add("testImplementation", "one.xis:" + module + ":" + version);
            if (project.getConfigurations().findByName("api") != null) {
                constraints.add("api", "one.xis:" + module + ":" + version);
            }
        }
        project.getDependencies().add("testImplementation", "one.xis:xis-boot-starter-test:" + version);
        project.getDependencies().add("annotationProcessor", "one.xis:xis-apt:" + version);
        project.getConfigurations().configureEach(configuration ->
                configuration.getResolutionStrategy().eachDependency(details -> {
                    var requested = details.getRequested();
                    if ("one.xis".equals(requested.getGroup())
                            && containsXisModule(requested.getName())
                            && (requested.getVersion() == null || requested.getVersion().isBlank())) {
                        details.useVersion(version);
                    }
                }));
        project.getTasks().withType(Test.class).configureEach(Test::useJUnitPlatform);
    }

    private boolean containsXisModule(String moduleName) {
        for (String module : xisModules()) {
            if (module.equals(moduleName)) {
                return true;
            }
        }
        return false;
    }

    private String[] xisModules() {
        return new String[]{
                "xis-adapter-spi",
                "xis-apt",
                "xis-authentication",
                "xis-authentication-api",
                "xis-boot",
                "xis-boot-native",
                "xis-boot-native-h2",
                "xis-boot-native-mariadb",
                "xis-boot-native-mongodb",
                "xis-boot-native-postgresql",
                "xis-boot-starter-test",
                "xis-boot-test-jupiter",
                "xis-bootstrap",
                "xis-context",
                "xis-controller-api",
                "xis-deserializer",
                "xis-distributed",
                "xis-gson",
                "xis-html",
                "xis-http-client",
                "xis-http-controller",
                "xis-i18n",
                "xis-idp-client",
                "xis-idp-server",
                "xis-javascript",
                "xis-mongodb",
                "xis-plugin",
                "xis-processor",
                "xis-resources",
                "xis-security-common",
                "xis-server",
                "xis-spring",
                "xis-sql",
                "xis-test",
                "xis-theme",
                "xis-token",
                "xis-util",
                "xis-validation"
        };
    }

    /**
     * Keep HTML templates next to Java controllers while still placing them on the runtime classpath.
     */
    private void configureResources(Project project) {
        project.getTasks().withType(ProcessResources.class).configureEach(sync ->
                sync.from(project.file("src/main/java"), copy -> copy.include("**/*.html")));
    }


    private void configureFatJarCreation(Project project) {
        project.afterEvaluate(p -> {
            boolean usesXisBoot = project.getConfigurations()
                    .getByName("implementation")
                    .getAllDependencies()
                    .stream()
                    .anyMatch(dep -> dep.getGroup() != null && dep.getGroup().equals("one.xis") && dep.getName().equals("xis-boot"));

            if (usesXisBoot) {
                var xisJar = project.getTasks().register("xisJar", XISBootJarTask.class, jar -> {
                    jar.getManifest().getAttributes().put("Main-Class", "one.xis.boot.Runner");
                    jar.configure(project);
                    jar.dependsOn(project.getTasks().getByName("classes"));
                });
                project.getTasks().register("xisRun", XISBootRunTask.class, run -> {
                    run.dependsOn(xisJar);
                    run.getJarFile().set(xisJar.flatMap(AbstractArchiveTask::getArchiveFile));
                });
            }
        });
    }

    private void configureNativeSupport(Project project) {
        project.afterEvaluate(p -> {
            if (!usesXisModule(project, "xis-boot-native")) {
                return;
            }
            configureNativeClassCatalogGeneration(project);
            configureNativeCatalogGeneration(project);
            configureNativeTasks(project);
        });
    }

    private void configureFrameworkComponentGeneration(Project project) {
        var main = mainSourceSet(project);
        var generatedFrameworkComponentsDir = project.getLayout().getBuildDirectory()
                .dir("generated/sources/xisFrameworkComponents/java/main");
        var generatedFrameworkComponentIndexDir = project.getLayout().getBuildDirectory()
                .dir("generated/resources/xisFrameworkComponents/main");
        var generateFrameworkComponents = project.getTasks().register("xisGenerateFrameworkComponents",
                XISGenerateFrameworkComponentsTask.class, task -> {
                    task.getSourceFiles().from(project.fileTree("src/main/java", tree -> tree.include("**/*.java")));
                    task.getOutputDirectory().set(generatedFrameworkComponentsDir);
                    task.getRegistryIndexOutputDirectory().set(generatedFrameworkComponentIndexDir);
                    task.getProjectName().set(project.getName());
                });

        main.getJava().srcDir(generatedFrameworkComponentsDir);
        main.getResources().srcDir(generatedFrameworkComponentIndexDir);
        project.getTasks().named("compileJava").configure(task -> task.dependsOn(generateFrameworkComponents));
        project.getTasks().named("processResources").configure(task -> task.dependsOn(generateFrameworkComponents));
        project.getTasks().matching(task -> task.getName().equals("sourcesJar"))
                .configureEach(task -> task.dependsOn(generateFrameworkComponents));
    }

    private void configureNativeClassCatalogGeneration(Project project) {
        var main = mainSourceSet(project);
        var generatedNativeClassCatalogDir = project.getLayout().getBuildDirectory()
                .dir("generated/resources/xisNativeClassCatalog/main");
        var generateNativeClassCatalog = project.getTasks().register("xisGenerateNativeClassCatalog",
                XISGenerateNativeClassCatalogTask.class, task -> {
                    task.getSourceFiles().from(project.fileTree("src/main/java", tree -> tree.include("**/*.java")));
                    task.getOutputDirectory().set(generatedNativeClassCatalogDir);
                    task.getProjectName().set(project.getName());
        });
        main.getResources().srcDir(generatedNativeClassCatalogDir);
        project.getTasks().named("processResources").configure(task -> task.dependsOn(generateNativeClassCatalog));
        project.getTasks().matching(task -> task.getName().equals("sourcesJar"))
                .configureEach(task -> task.dependsOn(generateNativeClassCatalog));
    }

    private void configureNativeCatalogGeneration(Project project) {
        var main = mainSourceSet(project);
        var generateFrameworkComponents = project.getTasks().named("xisGenerateFrameworkComponents",
                XISGenerateFrameworkComponentsTask.class);

        var generatedApplicationComponentsDir = project.getLayout().getBuildDirectory()
                .dir("generated/sources/xisApplicationComponents/java/main");
        var applicationComponentGeneratorTasks = project.getConfigurations()
                .getByName("compileClasspath")
                .getAllDependencies()
                .stream()
                .filter(ProjectDependency.class::isInstance)
                .map(ProjectDependency.class::cast)
                .map(ProjectDependency::getDependencyProject)
                .filter(dependencyProject -> dependencyProject.getTasks().findByName("xisGenerateFrameworkComponents") != null)
                .map(dependencyProject -> dependencyProject.getTasks().named("xisGenerateFrameworkComponents"))
                .toList();
        var generateApplicationComponents = project.getTasks().register("xisGenerateApplicationComponents",
                XISGenerateApplicationComponentCatalogTask.class, task -> {
                    task.getRegistryIndexFiles().from(generateFrameworkComponents.flatMap(XISGenerateFrameworkComponentsTask::getRegistryIndexOutputDirectory));
                    task.dependsOn(applicationComponentGeneratorTasks);
                    task.getRegistryIndexFiles().from(project.getConfigurations()
                            .getByName("compileClasspath")
                            .getAllDependencies()
                            .stream()
                            .filter(ProjectDependency.class::isInstance)
                            .map(ProjectDependency.class::cast)
                            .map(ProjectDependency::getDependencyProject)
                            .map(dependencyProject -> dependencyProject.getLayout().getBuildDirectory()
                                    .dir("generated/resources/xisFrameworkComponents/main"))
                            .toList());
                    task.getRegistryIndexFiles().from(project.getConfigurations().getByName("compileClasspath"));
                    task.getOutputDirectory().set(generatedApplicationComponentsDir);
                });

        var generatedNativeRunnerDir = project.getLayout().getBuildDirectory()
                .dir("generated/sources/xisNativeRunner/java/main");
        var generateNativeRunner = project.getTasks().register("xisGenerateNativeRunner",
                XISGenerateNativeRunnerTask.class, task -> {
                    task.getSourceFiles().from(project.fileTree("src/main/java", tree -> tree.include("**/*.java")));
                    task.getOutputDirectory().set(generatedNativeRunnerDir);
                });

        main.getJava().srcDir(generatedApplicationComponentsDir);
        main.getJava().srcDir(generatedNativeRunnerDir);
        project.getTasks().named("compileJava").configure(task -> {
            task.dependsOn(generateApplicationComponents);
            task.dependsOn(generateNativeRunner);
        });

        var generateNativeClassCatalog = project.getTasks().named("xisGenerateNativeClassCatalog",
                XISGenerateNativeClassCatalogTask.class);

        var generatedNativeReflectionConfigDir = project.getLayout().getBuildDirectory()
                .dir("generated/resources/xisNativeReflectionConfig/main");
        var nativeClassCatalogGeneratorTasks = project.getConfigurations()
                .getByName("compileClasspath")
                .getAllDependencies()
                .stream()
                .filter(ProjectDependency.class::isInstance)
                .map(ProjectDependency.class::cast)
                .map(ProjectDependency::getDependencyProject)
                .filter(dependencyProject -> dependencyProject.getTasks().findByName("xisGenerateNativeClassCatalog") != null)
                .map(dependencyProject -> dependencyProject.getTasks().named("xisGenerateNativeClassCatalog"))
                .toList();
        var generateNativeReflectionConfig = project.getTasks().register("xisGenerateNativeReflectionConfig",
                XISGenerateNativeReflectionConfigTask.class, task -> {
                    task.getClassCatalogFiles().from(generateNativeClassCatalog.flatMap(XISGenerateNativeClassCatalogTask::getOutputDirectory));
                    task.dependsOn(nativeClassCatalogGeneratorTasks);
                    task.getClassCatalogFiles().from(project.getConfigurations()
                            .getByName("compileClasspath")
                            .getAllDependencies()
                            .stream()
                            .filter(ProjectDependency.class::isInstance)
                            .map(ProjectDependency.class::cast)
                            .map(ProjectDependency::getDependencyProject)
                            .map(dependencyProject -> dependencyProject.getLayout().getBuildDirectory()
                                    .dir("generated/resources/xisNativeClassCatalog/main"))
                            .toList());
                    task.getClassCatalogFiles().from(project.getConfigurations().getByName("runtimeClasspath"));
                    task.getOutputDirectory().set(generatedNativeReflectionConfigDir);
                });

        var generatedNativeProxyConfigDir = project.getLayout().getBuildDirectory()
                .dir("generated/resources/xisNativeProxyConfig/main");
        var generateNativeProxyConfig = project.getTasks().register("xisGenerateNativeProxyConfig",
                XISGenerateNativeProxyConfigTask.class, task -> {
                    task.getSourceFiles().from(project.fileTree("src/main/java", tree -> tree.include("**/*.java")));
                    task.getOutputDirectory().set(generatedNativeProxyConfigDir);
                });

        var generatedNativeResourceCatalogDir = project.getLayout().getBuildDirectory()
                .dir("generated/resources/xisNativeResourceCatalog/main");
        var generateNativeResourceCatalog = project.getTasks().register("xisGenerateNativeResourceCatalog",
                XISGenerateNativeResourceCatalogTask.class, task -> {
                    task.getApplicationResourceRoots().from(project.getLayout().getProjectDirectory().dir("src/main/resources"));
                    task.getApplicationResourceRoots().from(project.getLayout().getProjectDirectory().dir("src/main/java"));
                    task.getClasspathFiles().from(project.getConfigurations().getByName("runtimeClasspath"));
                    task.getOutputDirectory().set(generatedNativeResourceCatalogDir);
                });

        main.getResources().srcDir(generatedNativeReflectionConfigDir);
        main.getResources().srcDir(generatedNativeResourceCatalogDir);
        project.getTasks().named("processResources").configure(task -> {
            task.dependsOn(generateNativeReflectionConfig);
            task.dependsOn(generateNativeResourceCatalog);
        });
        project.getTasks().matching(task -> task.getName().equals("sourcesJar"))
                .configureEach(task -> {
                    task.dependsOn(generateApplicationComponents);
                    task.dependsOn(generateNativeRunner);
                    task.dependsOn(generateNativeReflectionConfig);
                    task.dependsOn(generateNativeResourceCatalog);
                });
    }

    private void configureNativeTasks(Project project) {
        var main = mainSourceSet(project);
        var executable = project.getLayout().getBuildDirectory()
                .file("native/" + project.getName());
        var graalVmHome = project.getProviders().gradleProperty("graalVmHome")
                .orElse(project.getProviders().environmentVariable("GRAALVM_HOME"));

        var nativeCompile = project.getTasks().register("xisNativeCompile", XISNativeCompileTask.class, task -> {
            task.dependsOn(project.getTasks().named("classes"));
            task.dependsOn(project.getTasks().named("xisGenerateNativeReflectionConfig"));
            task.dependsOn(project.getTasks().named("xisGenerateNativeProxyConfig"));
            task.getNativeClasspath().from(main.getOutput());
            task.getNativeClasspath().from(project.getConfigurations().getByName("runtimeClasspath"));
            task.getGraalVmHome().set(graalVmHome);
            task.getMainClass().set("one.xis.boot.nativeimage.NativeRunner");
            task.getExecutableFile().set(executable);
            task.getReflectionConfig().set(project.getLayout().getBuildDirectory()
                    .file("generated/resources/xisNativeReflectionConfig/main/META-INF/native-image/one.xis/"
                            + project.getName() + "/reflect-config.json"));
            task.getProxyConfig().set(project.getLayout().getBuildDirectory()
                    .file("generated/resources/xisNativeProxyConfig/main/META-INF/native-image/one.xis/"
                            + project.getName() + "/proxy-config.json"));
        });

        project.getTasks().register("xisNativeRun", XISNativeRunTask.class, task -> {
            task.dependsOn(nativeCompile);
            task.getExecutableFile().set(executable);
        });

        project.getTasks().register("xisNativeSmokeTest", XISNativeSmokeTestTask.class, task -> {
            task.dependsOn(nativeCompile);
            task.getExecutableFile().set(executable);
            task.getPort().convention(8098);
        });
    }

    private boolean usesXisModule(Project project, String moduleName) {
        return project.getConfigurations()
                .getByName("implementation")
                .getAllDependencies()
                .stream()
                .anyMatch(dep -> dep.getGroup() != null && dep.getGroup().equals("one.xis") && dep.getName().equals(moduleName));
    }

    private void configureGroovySupport(Project project) {
        project.getPlugins().withId("groovy", plugin -> {
            configureGroovyAnnotationProcessing(project);
            configureGroovyTemplatesTask(project);
            configureGroovyTestsTask(project);
        });
    }

    private void configureGroovyAnnotationProcessing(Project project) {
        Configuration apClasspath = project.getConfigurations().getByName("annotationProcessor");
        project.getTasks().withType(GroovyCompile.class).configureEach(task -> {
            task.getGroovyOptions().setJavaAnnotationProcessing(true);
            task.getOptions().setAnnotationProcessorPath(apClasspath);
        });
    }



    /* ----------------------------- xisTemplates task ---------------------------- */

    private void configureTemplatesTask(Project project) {
        SourceSet main = mainSourceSet(project);

        Configuration apClasspath = buildApClasspath(project);

        // Templates should go to src/main/java, not build output
        File sourceJavaDir = javaSourceBase(main, project);

        project.getTasks().register("xisTemplates", XISTemplateTask.class, task -> {
            task.setGroup("xis");
            task.setDescription("Scaffolds missing XIS HTML templates for page and frontlet controllers.");

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

    /* ----------------------------- xisTests task -------------------------------- */

    private void configureTestsTask(Project project) {
        SourceSet main = mainSourceSet(project);
        SourceSetContainer sets = project.getExtensions().getByType(SourceSetContainer.class);
        SourceSet test = sets.getByName(SourceSet.TEST_SOURCE_SET_NAME);

        Configuration apClasspath = buildApClasspath(project);

        // Tests should go to src/test/java
        File testJavaDir = firstSourceDir(test.getJava(), new File(project.getProjectDir(), "src/test/java"));

        project.getTasks().register("xisTests", XISTestTask.class, task -> {
            task.setGroup("xis");
            task.setDescription("Scaffolds missing XIS integration tests for page controllers.");

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

    /* ----------------------------- validate task -------------------------------- */

    private void configureValidateTask(Project project) {
        SourceSet main = mainSourceSet(project);
        Configuration apClasspath = buildApClasspath(project);

        project.getTasks().register("xisValidate", XISValidateTask.class, task -> {
            task.setGroup("xis");
            task.setDescription("Validates XIS controllers and templates.");
            task.setSource(main.getAllJava());
            task.setClasspath(main.getCompileClasspath());
            task.getOptions().setAnnotationProcessorPath(apClasspath.plus(main.getCompileClasspath()));
            task.getProcessorFqcn().set("one.xis.processor.XISValidateProcessor");
            lockValidateTask(task);
            task.getOutputs().upToDateWhen(t -> false);
            task.getInputs()
                    .files(main.getAllJava().getSourceDirectories())
                    .withPropertyName("xisSources");
        });
    }

    private void configureGroovyTemplatesTask(Project project) {
        SourceSet main = mainSourceSet(project);
        SourceDirectorySet groovySources = groovySources(main);
        File resourcesDir = firstSourceDir(main.getResources(), new File(project.getProjectDir(), "src/main/resources"));
        Configuration apClasspath = buildApClasspath(project);

        var groovyTemplates = project.getTasks().register("xisGroovyTemplates", XISGroovyTemplateTask.class, task -> {
            task.setGroup("xis");
            task.setDescription("Scaffolds missing XIS HTML templates for Groovy page and frontlet controllers.");
            task.setSource(groovySources);
            task.setClasspath(main.getCompileClasspath());
            task.getOptions().setAnnotationProcessorPath(apClasspath);
            task.getProcessorFqcn().set("one.xis.processor.XISTemplateProcessor");
            task.getOutputDir().set(resourcesDir);
            lockGroovyTemplateTask(task);
            task.getOutputs().upToDateWhen(t -> false);
            task.getInputs()
                    .files(groovySources.getSourceDirectories())
                    .withPropertyName("xisGroovySources");
        });

        project.getTasks().named("xisTemplates").configure(task -> task.dependsOn(groovyTemplates));
    }

    private void configureGroovyTestsTask(Project project) {
        SourceSet main = mainSourceSet(project);
        SourceSetContainer sets = project.getExtensions().getByType(SourceSetContainer.class);
        SourceSet test = sets.getByName(SourceSet.TEST_SOURCE_SET_NAME);
        SourceDirectorySet groovySources = groovySources(main);
        File testGroovyDir = firstSourceDir(groovySources(test), new File(project.getProjectDir(), "src/test/groovy"));
        Configuration apClasspath = buildApClasspath(project);

        var groovyTests = project.getTasks().register("xisGroovyTests", XISGroovyTestTask.class, task -> {
            task.setGroup("xis");
            task.setDescription("Scaffolds missing XIS integration tests for Groovy page controllers.");
            task.setSource(groovySources);
            task.setClasspath(main.getCompileClasspath());
            task.getOptions().setAnnotationProcessorPath(apClasspath);
            task.getProcessorFqcn().set("one.xis.processor.XISTestProcessor");
            task.getOutputDir().set(testGroovyDir);
            lockGroovyTestTask(task);
            task.getOutputs().upToDateWhen(t -> false);
            task.getInputs()
                    .files(groovySources.getSourceDirectories())
                    .withPropertyName("xisGroovySources");
        });

        project.getTasks().named("xisTests").configure(task -> task.dependsOn(groovyTests));
    }

    /* ----------------------------- helpers -------------------------------------- */

    private SourceSet mainSourceSet(Project project) {
        SourceSetContainer sets = project.getExtensions().getByType(SourceSetContainer.class);
        return sets.getByName(SourceSet.MAIN_SOURCE_SET_NAME);
    }

    private SourceDirectorySet groovySources(SourceSet sourceSet) {
        return (SourceDirectorySet) sourceSet.getExtensions().getByName("groovy");
    }

    private File firstSourceDir(SourceDirectorySet sources, File fallback) {
        Set<File> srcDirs = sources.getSrcDirs();
        return srcDirs.isEmpty() ? fallback : srcDirs.iterator().next();
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

    private void lockValidateTask(XISValidateTask task) {
        task.getProcessorFqcn().finalizeValue();
        task.getProcessorFqcn().disallowChanges();
    }

    private void lockGroovyTemplateTask(XISGroovyTemplateTask task) {
        task.getProcessorFqcn().finalizeValue();
        task.getProcessorFqcn().disallowChanges();
        task.getOutputDir().finalizeValue();
        task.getOutputDir().disallowChanges();
    }

    private void lockGroovyTestTask(XISGroovyTestTask task) {
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
}
