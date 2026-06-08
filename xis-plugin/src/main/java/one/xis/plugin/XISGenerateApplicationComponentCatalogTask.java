package one.xis.plugin;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

public class XISGenerateApplicationComponentCatalogTask extends DefaultTask {

    private static final String REGISTRY_INDEX_PREFIX = "META-INF/xis/framework-components/";

    private final ConfigurableFileCollection registryIndexFiles = getProject().files();
    private final DirectoryProperty outputDirectory = getProject().getObjects().directoryProperty();
    private final Property<String> nativeRuntimePackage = getProject().getObjects().property(String.class);

    public XISGenerateApplicationComponentCatalogTask() {
        nativeRuntimePackage.convention("one.xis.boot.nativeimage");
    }

    @InputFiles
    @PathSensitive(PathSensitivity.RELATIVE)
    public ConfigurableFileCollection getRegistryIndexFiles() {
        return registryIndexFiles;
    }

    @OutputDirectory
    public DirectoryProperty getOutputDirectory() {
        return outputDirectory;
    }

    @Input
    public Property<String> getNativeRuntimePackage() {
        return nativeRuntimePackage;
    }

    @TaskAction
    public void generate() throws IOException {
        var outputDir = outputDirectory.get().getAsFile();
        getProject().delete(outputDir);
        var packageDir = new File(outputDir, "one/xis/generated");
        Files.createDirectories(packageDir.toPath());
        Files.writeString(new File(packageDir, "XisGeneratedApplicationComponents.java").toPath(),
                source(readRegistryClassNames()),
                StandardCharsets.UTF_8);
    }

    private Set<String> readRegistryClassNames() throws IOException {
        var registryClassNames = new LinkedHashSet<String>();
        for (var file : registryIndexFiles.getFiles()) {
            if (!file.exists()) {
                continue;
            }
            if (file.isDirectory()) {
                readRegistryClassNamesFromDirectory(file, registryClassNames);
            } else if (file.getName().endsWith(".jar")) {
                readRegistryClassNamesFromJar(file, registryClassNames);
            }
        }
        return registryClassNames.stream()
                .filter(name -> !name.isBlank())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private static void readRegistryClassNamesFromDirectory(File directory, Set<String> registryClassNames) throws IOException {
        readRegistryClassNamesFromDirectoryOnly(directory, registryClassNames);
        for (File relatedDirectory : relatedResourceDirectories(directory)) {
            readRegistryClassNamesFromDirectoryOnly(relatedDirectory, registryClassNames);
        }
    }

    private static void readRegistryClassNamesFromDirectoryOnly(File directory, Set<String> registryClassNames) throws IOException {
        if (!directory.exists()) {
            return;
        }
        try (var paths = Files.walk(directory.toPath())) {
            var files = paths.filter(Files::isRegularFile)
                    .filter(path -> path.toString().replace(File.separatorChar, '/').contains(REGISTRY_INDEX_PREFIX))
                    .filter(path -> path.getFileName().toString().endsWith(".txt"))
                    .toList();
            for (var file : files) {
                registryClassNames.addAll(Files.readAllLines(file, StandardCharsets.UTF_8));
            }
        }
    }

    private static Collection<File> relatedResourceDirectories(File directory) {
        var normalizedPath = directory.toPath().toString().replace(File.separatorChar, '/');
        if (!normalizedPath.endsWith("/build/classes/java/main")
                && !normalizedPath.endsWith("/build/classes/groovy/main")) {
            return Set.of();
        }
        var buildDir = directory.getParentFile().getParentFile().getParentFile();
        return Set.of(
                new File(buildDir, "resources/main"),
                new File(buildDir, "generated/resources/xisFrameworkComponents/main")
        );
    }

    private static void readRegistryClassNamesFromJar(File jar, Set<String> registryClassNames) throws IOException {
        try (var jarFile = new JarFile(jar)) {
            var entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                var entry = entries.nextElement();
                if (entry.isDirectory()
                        || !entry.getName().startsWith(REGISTRY_INDEX_PREFIX)
                        || !entry.getName().endsWith(".txt")) {
                    continue;
                }
                try (var stream = jarFile.getInputStream(entry)) {
                    registryClassNames.addAll(new String(stream.readAllBytes(), StandardCharsets.UTF_8).lines().toList());
                }
            }
        } catch (java.util.zip.ZipException ex) {
            throw new GradleException("Cannot read XIS framework component registry index from " + jar, ex);
        }
    }

    private String source(Collection<String> registryClassNames) {
        return """
                package one.xis.generated;

                import %s.NativeComponentRegistry;

                import java.util.Collection;
                import java.util.List;

                /**
                 * Generated by Gradle. Do not edit manually.
                 */
                public final class XisGeneratedApplicationComponents implements NativeComponentRegistry {

                    private static final List<Collection<Class<?>>> COMPONENT_GROUPS = List.of(
                %s
                    );

                    @Override
                    public Collection<Class<?>> componentClasses() {
                        return COMPONENT_GROUPS.stream()
                                .flatMap(Collection::stream)
                                .toList();
                    }
                }
                """.formatted(nativeRuntimePackage.get(), registryClassNames.stream()
                .sorted()
                .map(className -> "            new " + className + "().componentClasses()")
                .collect(Collectors.joining(",\n")));
    }
}
