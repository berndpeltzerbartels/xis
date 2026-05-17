package one.xis.gradle;

import org.gradle.api.DefaultTask;
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
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class GenerateNativeProxyConfigTask extends DefaultTask {

    private static final Pattern PACKAGE_PATTERN = Pattern.compile("^\\s*package\\s+([a-zA-Z0-9_.]+)\\s*;", Pattern.MULTILINE);
    private static final Pattern INTERFACE_PATTERN = Pattern.compile("(?:(?:public|protected|private|abstract|static|sealed|non-sealed|strictfp)\\s+)*interface\\s+([A-Za-z_$][A-Za-z0-9_$]*)\\b");

    private final ConfigurableFileCollection sourceFiles = getProject().files();
    private final DirectoryProperty outputDirectory = getProject().getObjects().directoryProperty();

    @InputFiles
    @PathSensitive(PathSensitivity.RELATIVE)
    public ConfigurableFileCollection getSourceFiles() {
        return sourceFiles;
    }

    @OutputDirectory
    public DirectoryProperty getOutputDirectory() {
        return outputDirectory;
    }

    @TaskAction
    public void generate() throws IOException {
        var outputDir = outputDirectory.get().getAsFile();
        getProject().delete(outputDir);
        var nativeImageDir = new File(outputDir, "META-INF/native-image/one.xis/" + getProject().getName());
        Files.createDirectories(nativeImageDir.toPath());
        Files.writeString(new File(nativeImageDir, "proxy-config.json").toPath(), proxyConfig(scanProxyInterfaces()), StandardCharsets.UTF_8);
    }

    private Set<String> scanProxyInterfaces() throws IOException {
        var proxyInterfaces = new LinkedHashSet<String>();
        for (File sourceFile : sourceFiles.getFiles().stream().sorted(Comparator.comparing(File::getPath)).toList()) {
            if (!sourceFile.isFile() || !sourceFile.getName().endsWith(".java")) {
                continue;
            }
            var source = Files.readString(sourceFile.toPath(), StandardCharsets.UTF_8);
            var packageName = readPackageName(source);
            if (packageName == null) {
                continue;
            }
            readRepositoryInterfaces(source).stream()
                    .map(interfaceName -> packageName + "." + interfaceName)
                    .forEach(proxyInterfaces::add);
        }
        return proxyInterfaces;
    }

    private static Set<String> readRepositoryInterfaces(String source) {
        var result = new LinkedHashSet<String>();
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
                var matcher = INTERFACE_PATTERN.matcher(line);
                if (matcher.find() && pendingAnnotations.contains("Repository")) {
                    result.add(matcher.group(1));
                }
            }
            pendingAnnotations.clear();
            braceDepth += braceDelta(line);
        }
        return result;
    }

    private static String proxyConfig(Set<String> proxyInterfaces) {
        return """
                [
                %s
                ]
                """.formatted(proxyInterfaces.stream()
                .map(interfaceName -> """
                          {
                            "interfaces": [
                              "%s"
                            ]
                          }""".formatted(interfaceName))
                .collect(Collectors.joining(",\n")));
    }

    private static String readPackageName(String source) {
        var matcher = PACKAGE_PATTERN.matcher(source);
        return matcher.find() ? matcher.group(1) : null;
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

    private static String simpleName(String name) {
        var index = name.lastIndexOf('.');
        return index >= 0 ? name.substring(index + 1) : name;
    }
}
