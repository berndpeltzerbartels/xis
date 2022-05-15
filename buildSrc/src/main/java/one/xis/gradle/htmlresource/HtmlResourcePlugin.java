package one.xis.gradle.htmlresource;

import lombok.NonNull;
import one.xis.utils.io.IOUtils;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class HtmlResourcePlugin implements Plugin<Project> {

    @Override
    public void apply(@NonNull Project project) {
        File srcDir = getSourceDir(project);
        File genratedSourcesRoot = getGeneratedSourceRoot(project);
        scanDir(srcDir, "", getGeneratedSourceRoot(project));
    }

    private File getGeneratedSourceRoot(Project project) {
        return new File(project.getBuildDir(), "generated/xis/java/main");
    }

    private File getSourceDir(Project project) {
        return new File(project.getProjectDir(), "src/main/java");
    }


    private void scanDir(File dir, String packageName, File outputRoot) {
        listFiles(dir).forEach(file -> {
            if (file.isFile()) {
                if (!file.getName().endsWith(".java")) {
                    generateAnnotatedSource(file, packageName, outputRoot);
                }
            } else if (file.isDirectory()) {
                String nextPackageName = packageName.isBlank() ? file.getName() : String.format("%s.%s", packageName, file.getName());
                scanDir(file, nextPackageName, outputRoot);
            }
        });
    }

    private Stream<File> listFiles(File dir) {
        File[] files = dir.listFiles();
        if (files == null) {
            return Stream.empty();
        }
        return Arrays.stream(files);
    }


    private void generateAnnotatedSource(File resource, String packageName, File outputRoot) {
        String simpleClassName = toJavaClassName(resource.getName());
        File classFolder = new File(outputRoot, packageName.replace('.', '/'));
        classFolder.mkdirs();
        File file = new File(classFolder, simpleClassName + ".java");
        try (PrintWriter writer = IOUtils.printWriter(file, StandardCharsets.UTF_8.name())) {
            writer.print("package ");
            writer.print(packageName);
            writer.println(";");

            writer.print("@Resource");
            writer.print("(\"");
            writer.print(resource.getName());
            writer.println("\")");

            writer.print("class ");
            writer.print(simpleClassName);
            writer.println(" {");
            writeContentField(resource, writer);
            writer.println(" }");
        }

    }


    private void writeContentField(File resource, PrintWriter writer) {
        writer.print("static final CONTENT = ");
        writer.print("\"");
        try {
            writer.print(Files.readAllLines(resource.toPath()).stream()
                    .map(str -> str.replace("\"", "\\\""))
                    .collect(Collectors.joining("\"+\n\"")));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        writer.println("\";");
    }

    private String randomClassName() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private String toJavaClassName(String fileName) {
        StringBuilder s = new StringBuilder();
        boolean ucase = false;
        char[] chars = fileName.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            if (i == 0 && Character.isAlphabetic(c)) {
                s.append(Character.toUpperCase(c));
                continue;
            }
            if (!Character.isAlphabetic(c) && !Character.isDigit(c)) {
                ucase = true;
                continue;
            }
            if (Character.isAlphabetic(c)) {
                if (ucase) {
                    s.append(Character.toUpperCase(c));
                } else {
                    s.append(c);
                }
            }
            ucase = false;
        }
        return s.toString();
    }

}
