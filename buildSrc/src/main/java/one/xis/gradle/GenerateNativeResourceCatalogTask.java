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
import java.nio.file.Path;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

public class GenerateNativeResourceCatalogTask extends DefaultTask {

    private static final String CATALOG_PATH = "META-INF/xis/native/resources/catalog.txt";
    private static final String JAVASCRIPT_EXTENSION_INDEX = "META-INF/xis/js/extensions";

    private final ConfigurableFileCollection applicationResourceRoots = getProject().files();
    private final ConfigurableFileCollection classpathFiles = getProject().files();
    private final DirectoryProperty outputDirectory = getProject().getObjects().directoryProperty();

    @InputFiles
    @PathSensitive(PathSensitivity.RELATIVE)
    public ConfigurableFileCollection getApplicationResourceRoots() {
        return applicationResourceRoots;
    }

    @InputFiles
    @PathSensitive(PathSensitivity.RELATIVE)
    public ConfigurableFileCollection getClasspathFiles() {
        return classpathFiles;
    }

    @OutputDirectory
    public DirectoryProperty getOutputDirectory() {
        return outputDirectory;
    }

    @TaskAction
    public void generate() throws IOException {
        var outputDir = outputDirectory.get().getAsFile();
        getProject().delete(outputDir);

        var resources = new LinkedHashMap<String, byte[]>();
        readApplicationResources(resources);
        readClasspathResources(resources);

        var catalog = resources.entrySet().stream()
                .map(entry -> entry.getKey() + "\t" + Base64.getEncoder().encodeToString(entry.getValue()))
                .collect(Collectors.joining(System.lineSeparator()));

        var catalogFile = new File(outputDir, CATALOG_PATH);
        Files.createDirectories(catalogFile.toPath().getParent());
        Files.writeString(catalogFile.toPath(), catalog + System.lineSeparator(), StandardCharsets.UTF_8);
    }

    private void readApplicationResources(Map<String, byte[]> resources) throws IOException {
        for (var root : applicationResourceRoots.getFiles()) {
            if (!root.isDirectory()) {
                continue;
            }
            readDirectoryResources(root.toPath(), resources, true);
        }
    }

    private void readClasspathResources(Map<String, byte[]> resources) throws IOException {
        for (var file : classpathFiles.getFiles()) {
            if (!file.exists()) {
                continue;
            }
            if (file.isDirectory()) {
                readDirectoryResources(file.toPath(), resources, false);
            } else if (file.getName().endsWith(".jar")) {
                readJarResources(file, resources);
            }
        }
    }

    private static void readDirectoryResources(Path root, Map<String, byte[]> resources, boolean overrideExisting) throws IOException {
        try (var paths = Files.walk(root)) {
            var files = paths.filter(Files::isRegularFile).sorted().toList();
            for (var file : files) {
                var resourcePath = normalize(root.relativize(file).toString());
                if (!include(resourcePath)) {
                    continue;
                }
                put(resources, resourcePath, Files.readAllBytes(file), overrideExisting);
            }
        }
    }

    private static void readJarResources(File jar, Map<String, byte[]> resources) throws IOException {
        try (var jarFile = new JarFile(jar)) {
            var entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                var entry = entries.nextElement();
                if (entry.isDirectory() || !include(entry.getName())) {
                    continue;
                }
                try (var stream = jarFile.getInputStream(entry)) {
                    put(resources, entry.getName(), stream.readAllBytes(), false);
                }
            }
        } catch (java.util.zip.ZipException ex) {
            throw new GradleException("Cannot read native resources from " + jar, ex);
        }
    }

    private static void put(Map<String, byte[]> resources, String resourcePath, byte[] content, boolean overrideExisting) {
        if (JAVASCRIPT_EXTENSION_INDEX.equals(resourcePath) && resources.containsKey(resourcePath)) {
            resources.put(resourcePath, mergeLines(resources.get(resourcePath), content));
            return;
        }
        if (overrideExisting) {
            resources.put(resourcePath, content);
        } else {
            resources.putIfAbsent(resourcePath, content);
        }
    }

    private static byte[] mergeLines(byte[] existing, byte[] additional) {
        var merged = new String(existing, StandardCharsets.UTF_8).stripTrailing()
                + System.lineSeparator()
                + new String(additional, StandardCharsets.UTF_8).stripLeading();
        return merged.getBytes(StandardCharsets.UTF_8);
    }

    private static boolean include(String resourcePath) {
        if (resourcePath.endsWith(".class")
                || resourcePath.equals(CATALOG_PATH)
                || resourcePath.startsWith("META-INF/native-image/")
                || resourcePath.startsWith("META-INF/xis/native/")) {
            return false;
        }
        return resourcePath.endsWith(".html")
                || resourcePath.endsWith(".css")
                || resourcePath.endsWith(".js")
                || resourcePath.endsWith(".map")
                || resourcePath.endsWith(".json")
                || resourcePath.endsWith(".properties")
                || resourcePath.endsWith(".txt")
                || resourcePath.endsWith(".xsd")
                || resourcePath.equals(JAVASCRIPT_EXTENSION_INDEX);
    }

    private static String normalize(String path) {
        return path.replace(File.separatorChar, '/');
    }
}
