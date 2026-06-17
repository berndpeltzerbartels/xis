package one.xis.plugin;

import org.gradle.api.DefaultTask;
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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

public class XISGenerateNativeClassCatalogTask extends DefaultTask {

    private static final Pattern PACKAGE_PATTERN = Pattern.compile("^\\s*package\\s+([a-zA-Z0-9_.]+)\\s*;?", Pattern.MULTILINE);
    private static final Pattern TOP_LEVEL_TYPE_PATTERN = Pattern.compile("(?:(?:public|protected|private|abstract|final|static|sealed|non-sealed|strictfp|data|open|internal)\\s+)*(class|record|enum|interface|object|@interface)\\s+([A-Za-z_$][A-Za-z0-9_$]*)\\b");

    private final ConfigurableFileCollection sourceFiles = getProject().files();
    private final DirectoryProperty outputDirectory = getProject().getObjects().directoryProperty();
    private final Property<String> projectName = getProject().getObjects().property(String.class);

    @InputFiles
    @PathSensitive(PathSensitivity.RELATIVE)
    public ConfigurableFileCollection getSourceFiles() {
        return sourceFiles;
    }

    @OutputDirectory
    public DirectoryProperty getOutputDirectory() {
        return outputDirectory;
    }

    @Input
    public Property<String> getProjectName() {
        return projectName;
    }

    @TaskAction
    public void generate() throws IOException {
        var outputDir = outputDirectory.get().getAsFile();
        getProject().delete(outputDir);
        var classes = scanTopLevelClasses();
        if (classes.isEmpty()) {
            return;
        }
        var catalogDir = new File(outputDir, "META-INF/xis/native/classes");
        Files.createDirectories(catalogDir.toPath());
        Files.writeString(new File(catalogDir, projectName.get() + ".txt").toPath(),
                String.join(System.lineSeparator(), classes) + System.lineSeparator(),
                StandardCharsets.UTF_8);
    }

    private List<String> scanTopLevelClasses() throws IOException {
        var result = new ArrayList<String>();
        for (File sourceFile : sourceFiles.getFiles().stream().sorted(Comparator.comparing(File::getPath)).toList()) {
            if (!sourceFile.isFile() || !(sourceFile.getName().endsWith(".java") || sourceFile.getName().endsWith(".kt"))) {
                continue;
            }
            var source = Files.readString(sourceFile.toPath(), StandardCharsets.UTF_8);
            var packageName = readPackageName(source);
            if (packageName == null) {
                continue;
            }
            readTypeNames(source).forEach(typeName -> result.add(packageName + "." + typeName));
        }
        return result.stream().sorted().toList();
    }

    private static String readPackageName(String source) {
        var matcher = PACKAGE_PATTERN.matcher(source);
        return matcher.find() ? matcher.group(1) : null;
    }

    private static List<String> readTypeNames(String source) {
        var typeNames = new ArrayList<String>();
        var typeStack = new ArrayList<MemberType>();
        var braceDepth = 0;
        for (var rawLine : source.split("\\R")) {
            var line = rawLine.strip();
            while (!typeStack.isEmpty() && last(typeStack).bodyDepth() > braceDepth) {
                typeStack.remove(typeStack.size() - 1);
            }
            if (line.isEmpty() || line.startsWith("//") || line.startsWith("*") || line.startsWith("@")) {
                braceDepth += braceDelta(line);
                continue;
            }
            if (braceDepth == 0 || isDirectMemberDepth(typeStack, braceDepth)) {
                var matcher = TOP_LEVEL_TYPE_PATTERN.matcher(line);
                if (matcher.find()) {
                    var simpleName = matcher.group(2);
                    var typeName = typeStack.isEmpty()
                            ? simpleName
                            : last(typeStack).qualifiedName() + "$" + simpleName;
                    typeNames.add(typeName);
                    var bodyDepth = braceDepth + Math.max(1, braceDelta(line));
                    typeStack.add(new MemberType(typeName, bodyDepth));
                }
            }
            braceDepth += braceDelta(line);
        }
        return typeNames;
    }

    private static boolean isDirectMemberDepth(List<MemberType> typeStack, int braceDepth) {
        return !typeStack.isEmpty() && last(typeStack).bodyDepth() == braceDepth;
    }

    private static MemberType last(List<MemberType> typeStack) {
        return typeStack.get(typeStack.size() - 1);
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

    private record MemberType(String qualifiedName, int bodyDepth) {
    }
}
