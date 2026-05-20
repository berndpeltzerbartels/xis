package one.xis.plugin;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.ListProperty;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class XISGenerateFrameworkComponentsTask extends DefaultTask {

    private static final Pattern PACKAGE_PATTERN = Pattern.compile("^\\s*package\\s+([a-zA-Z0-9_.]+)\\s*;?", Pattern.MULTILINE);
    private static final Pattern TYPE_PATTERN = Pattern.compile("(?:(?:public|protected|private|abstract|final|static|sealed|non-sealed|strictfp|data|open|internal)\\s+)*(class|record|enum|interface|object)\\s+([A-Za-z_$][A-Za-z0-9_$]*)\\b");

    private final ConfigurableFileCollection sourceFiles = getProject().files();
    private final DirectoryProperty outputDirectory = getProject().getObjects().directoryProperty();
    private final DirectoryProperty registryIndexOutputDirectory = getProject().getObjects().directoryProperty();
    private final Property<String> projectName = getProject().getObjects().property(String.class);
    private final ListProperty<String> componentAnnotationNames = getProject().getObjects().listProperty(String.class);

    public XISGenerateFrameworkComponentsTask() {
        componentAnnotationNames.convention(List.of(
                "Component",
                "DefaultComponent",
                "Controller",
                "Page",
                "Frontlet",
                "Modal",
                "Router",
                "Include",
                "Repository",
                "MongoRepository"
        ));
    }

    @InputFiles
    @PathSensitive(PathSensitivity.RELATIVE)
    public ConfigurableFileCollection getSourceFiles() {
        return sourceFiles;
    }

    @OutputDirectory
    public DirectoryProperty getOutputDirectory() {
        return outputDirectory;
    }

    @OutputDirectory
    public DirectoryProperty getRegistryIndexOutputDirectory() {
        return registryIndexOutputDirectory;
    }

    @Input
    public Property<String> getProjectName() {
        return projectName;
    }

    @Input
    public ListProperty<String> getComponentAnnotationNames() {
        return componentAnnotationNames;
    }

    @TaskAction
    public void generate() throws IOException {
        var outputDir = outputDirectory.get().getAsFile();
        var registryIndexOutputDir = registryIndexOutputDirectory.get().getAsFile();
        getProject().delete(outputDir);
        getProject().delete(registryIndexOutputDir);
        Files.createDirectories(outputDir.toPath());
        Files.createDirectories(registryIndexOutputDir.toPath());

        var componentsByPackage = scanComponents();
        if (componentsByPackage.isEmpty()) {
            return;
        }

        var projectSuffix = toJavaIdentifierSuffix(projectName.get());
        var packageRegistryClasses = new ArrayList<RegistryClass>();
        for (var entry : componentsByPackage.entrySet()) {
            var packageName = entry.getKey();
            var className = "XisGenerated" + projectSuffix + "Components";
            writePackageRegistry(outputDir, packageName, className, entry.getValue());
            packageRegistryClasses.add(new RegistryClass(packageName, className));
        }

        var moduleRegistryName = writeModuleRegistry(outputDir, projectSuffix, packageRegistryClasses);
        writeModuleRegistryIndex(registryIndexOutputDir, moduleRegistryName);
    }

    private Map<String, List<String>> scanComponents() throws IOException {
        var annotationNames = componentAnnotationNames.get().stream()
                .map(XISGenerateFrameworkComponentsTask::simpleName)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        var componentsByPackage = new LinkedHashMap<String, Set<String>>();
        for (File sourceFile : sourceFiles.getFiles().stream().sorted(Comparator.comparing(File::getPath)).toList()) {
            if (!sourceFile.isFile() || !(sourceFile.getName().endsWith(".java") || sourceFile.getName().endsWith(".kt"))) {
                continue;
            }
            var source = Files.readString(sourceFile.toPath(), StandardCharsets.UTF_8);
            var packageName = readPackageName(source);
            if (packageName == null) {
                continue;
            }
            for (var component : readComponentTypes(source, annotationNames)) {
                componentsByPackage.computeIfAbsent(packageName, ignored -> new LinkedHashSet<>()).add(component);
            }
        }
        return componentsByPackage.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().stream().sorted().toList(),
                        (left, right) -> left,
                        LinkedHashMap::new
                ));
    }

    private static String readPackageName(String source) {
        var matcher = PACKAGE_PATTERN.matcher(source);
        return matcher.find() ? matcher.group(1) : null;
    }

    private static Collection<String> readComponentTypes(String source, Set<String> componentAnnotationNames) {
        var result = new ArrayList<String>();
        var pendingAnnotations = new LinkedHashSet<String>();
        var braceDepth = 0;
        for (var rawLine : source.split("\\R")) {
            var line = rawLine.strip();
            if (line.isEmpty() || line.startsWith("//") || line.startsWith("*")) {
                continue;
            }
            if (braceDepth == 0 && line.startsWith("@")) {
                var annotationName = readAnnotationName(line);
                if (annotationName != null) {
                    pendingAnnotations.add(simpleName(annotationName));
                }
                braceDepth += braceDelta(line);
                continue;
            }
            if (braceDepth == 0 && !pendingAnnotations.isEmpty()) {
                var matcher = TYPE_PATTERN.matcher(line);
                if (matcher.find() && !line.contains("@interface") && intersects(pendingAnnotations, componentAnnotationNames)) {
                    result.add(matcher.group(2));
                }
            }
            pendingAnnotations.clear();
            braceDepth += braceDelta(line);
        }
        return result;
    }

    private static int braceDelta(String line) {
        var delta = 0;
        for (int i = 0; i < line.length(); i++) {
            var c = line.charAt(i);
            if (c == '{') {
                delta++;
            } else if (c == '}') {
                delta--;
            }
        }
        return delta;
    }

    private static String readAnnotationName(String line) {
        var name = new StringBuilder();
        for (int i = 1; i < line.length(); i++) {
            var c = line.charAt(i);
            if (Character.isJavaIdentifierPart(c) || c == '.') {
                name.append(c);
            } else {
                break;
            }
        }
        return name.isEmpty() ? null : name.toString();
    }

    private static boolean intersects(Set<String> left, Set<String> right) {
        return left.stream().anyMatch(right::contains);
    }

    private static String simpleName(String name) {
        var index = name.lastIndexOf('.');
        return index >= 0 ? name.substring(index + 1) : name;
    }

    private void writePackageRegistry(File outputDir, String packageName, String className, List<String> componentClasses) throws IOException {
        var source = """
                package %s;

                import java.util.Collection;
                import java.util.List;

                /**
                 * Generated by Gradle. Do not edit manually.
                 */
                public final class %s {

                    private static final Collection<Class<?>> COMPONENTS = List.of(
                %s
                    );

                    public Collection<Class<?>> componentClasses() {
                        return COMPONENTS;
                    }
                }
                """.formatted(
                packageName,
                className,
                componentClasses.stream()
                        .map(component -> "            " + component + ".class")
                        .collect(Collectors.joining(",\n"))
        );
        writeJavaSource(outputDir, packageName, className, source);
    }

    private String writeModuleRegistry(File outputDir, String projectSuffix, List<RegistryClass> registryClasses) throws IOException {
        var className = "XisGenerated" + projectSuffix + "ModuleComponents";
        var source = """
                package one.xis.generated;

                import java.util.Collection;
                import java.util.List;

                /**
                 * Generated by Gradle. Do not edit manually.
                 */
                public final class %s {

                    private static final List<Collection<Class<?>>> COMPONENT_GROUPS = List.of(
                %s
                    );

                    public Collection<Class<?>> componentClasses() {
                        return COMPONENT_GROUPS.stream()
                                .flatMap(Collection::stream)
                                .toList();
                    }
                }
                """.formatted(
                className,
                registryClasses.stream()
                        .map(registryClass -> "            new " + registryClass.fullName() + "().componentClasses()")
                        .collect(Collectors.joining(",\n"))
        );
        writeJavaSource(outputDir, "one.xis.generated", className, source);
        return "one.xis.generated." + className;
    }

    private void writeModuleRegistryIndex(File outputDir, String moduleRegistryName) throws IOException {
        var indexDir = new File(outputDir, "META-INF/xis/framework-components");
        Files.createDirectories(indexDir.toPath());
        Files.writeString(new File(indexDir, projectName.get() + ".txt").toPath(),
                moduleRegistryName + System.lineSeparator(),
                StandardCharsets.UTF_8);
    }

    private static void writeJavaSource(File outputDir, String packageName, String className, String source) throws IOException {
        var packageDir = new File(outputDir, packageName.replace('.', File.separatorChar));
        Files.createDirectories(packageDir.toPath());
        Files.writeString(new File(packageDir, className + ".java").toPath(), source, StandardCharsets.UTF_8);
    }

    static String toJavaIdentifierSuffix(String rawName) {
        return Pattern.compile("[^A-Za-z0-9]+")
                .splitAsStream(Objects.requireNonNull(rawName))
                .filter(part -> !part.isBlank())
                .map(XISGenerateFrameworkComponentsTask::capitalize)
                .collect(Collectors.joining());
    }

    private static String capitalize(String value) {
        if (value.isBlank()) {
            return value;
        }
        var lower = value.toLowerCase(Locale.ROOT);
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }

    private record RegistryClass(String packageName, String className) {
        String fullName() {
            return packageName + "." + className;
        }
    }
}
