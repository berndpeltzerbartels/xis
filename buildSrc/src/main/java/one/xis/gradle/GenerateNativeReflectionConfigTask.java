package one.xis.gradle;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

public class GenerateNativeReflectionConfigTask extends DefaultTask {

    private static final String NATIVE_CLASS_CATALOG_PREFIX = "META-INF/xis/native/classes/";

    private final ConfigurableFileCollection classCatalogFiles = getProject().files();
    private final DirectoryProperty outputDirectory = getProject().getObjects().directoryProperty();

    @InputFiles
    @PathSensitive(PathSensitivity.RELATIVE)
    public ConfigurableFileCollection getClassCatalogFiles() {
        return classCatalogFiles;
    }

    @OutputDirectory
    public DirectoryProperty getOutputDirectory() {
        return outputDirectory;
    }

    @TaskAction
    public void generate() throws IOException {
        var outputDir = outputDirectory.get().getAsFile();
        getProject().delete(outputDir);
        var configDir = new File(outputDir, "META-INF/native-image/one.xis/" + getProject().getName());
        Files.createDirectories(configDir.toPath());
        Files.writeString(new File(configDir, "reflect-config.json").toPath(),
                reflectionConfig(readClassNames()),
                StandardCharsets.UTF_8);
    }

    private Set<String> readClassNames() throws IOException {
        var classNames = new LinkedHashSet<String>();
        for (var file : classCatalogFiles.getFiles()) {
            if (!file.exists()) {
                continue;
            }
            if (file.isDirectory()) {
                readClassNamesFromDirectory(file, classNames);
            } else if (file.getName().endsWith(".jar")) {
                readClassNamesFromJar(file, classNames);
            }
        }
        return classNames.stream()
                .filter(name -> !name.isBlank())
                .sorted()
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private static void readClassNamesFromDirectory(File directory, Set<String> classNames) throws IOException {
        try (var paths = Files.walk(directory.toPath())) {
            var files = paths.filter(Files::isRegularFile)
                    .filter(path -> path.toString().replace(File.separatorChar, '/').contains(NATIVE_CLASS_CATALOG_PREFIX))
                    .filter(path -> path.getFileName().toString().endsWith(".txt"))
                    .toList();
            for (var file : files) {
                classNames.addAll(Files.readAllLines(file, StandardCharsets.UTF_8));
            }
        }
    }

    private static void readClassNamesFromJar(File jar, Set<String> classNames) throws IOException {
        try (var jarFile = new JarFile(jar)) {
            var entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                var entry = entries.nextElement();
                if (entry.isDirectory()
                        || !entry.getName().startsWith(NATIVE_CLASS_CATALOG_PREFIX)
                        || !entry.getName().endsWith(".txt")) {
                    continue;
                }
                try (var stream = jarFile.getInputStream(entry)) {
                    classNames.addAll(new String(stream.readAllBytes(), StandardCharsets.UTF_8).lines().toList());
                }
            }
        } catch (java.util.zip.ZipException ex) {
            throw new GradleException("Cannot read XIS native class catalog from " + jar, ex);
        }
    }

    private static String reflectionConfig(Set<String> classNames) {
        var entries = classNames.stream()
                .map(GenerateNativeReflectionConfigTask::reflectionEntry)
                .collect(Collectors.joining(",\n"));
        return "[\n" + entries + "\n]\n";
    }

    private static String reflectionEntry(String className) {
        return """
                  {
                    "name": "%s",
                    "allDeclaredConstructors": true,
                    "allDeclaredMethods": true,
                    "allDeclaredFields": true
                  }""".formatted(className);
    }
}
