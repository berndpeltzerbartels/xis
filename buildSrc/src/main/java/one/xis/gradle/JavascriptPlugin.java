package one.xis.gradle;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.PolyglotAccess;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

import java.io.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JavascriptPlugin implements Plugin<Project> {

    private static final String RELEASE_JS_OUTFILE = "xis-prod.js";

    @Override
    public void apply(Project project) {
        log("apply plugin for project %s", project.getDisplayName());
        process(project);
    }

    private void process(Project project) {
        File releaseJsFile = getReleaseOutFile(project);
        deleteIfExists(releaseJsFile);

        List<JSFile> releaseJsFiles = getReleaseJsFiles(project);
        List<JSFile> allJsFiles = getAllJsFiles(project);

        writeGroupedFiles(project, allJsFiles);
        writeJsToFileIIFE(JSFileSorter.sort(releaseJsFiles), releaseJsFile, false);
        evalWithGraalVM(releaseJsFile);
    }

    private void writeGroupedFiles(Project project, List<JSFile> jsFiles) {
        var grouped = jsFiles.stream()
                .collect(Collectors.groupingBy(file -> {
                    var fullPath = file.getFile().toPath();
                    var rootPath = getJsApiSrcRoot(project).toPath();
                    return rootPath.relativize(fullPath).getName(0).toString();
                }));

        grouped.forEach((dirName, files) -> {
            File outFile = new File(getOutDir(project), dirName + ".js");
            writeJsToFile(JSFileSorter.sort(files), outFile, false);
        });
    }

    private List<JSFile> getReleaseJsFiles(Project project) {
        var rootPath = getJsApiSrcRoot(project).toPath();
        return releaseSourceDirs(project).stream()
                .flatMap(dir -> FileUtils.files(dir, "js").stream())
                .filter(file -> file.toPath().getNameCount() > rootPath.getNameCount())
                .map(this::toJSFile)
                .collect(Collectors.toList());
    }

    private List<JSFile> getAllJsFiles(Project project) {
        return allSourceDirs(project).stream()
                .flatMap(dir -> FileUtils.files(dir, "js").stream())
                .map(this::toJSFile)
                .collect(Collectors.toList());
    }

    private void writeJsToFile(Collection<JSFile> jsFiles, File outFile, boolean append) {
        log("js-outfile: '%s'", outFile);
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(outFile, append)))) {
            for (JSFile file : jsFiles) {
                log("add script content: %s", file.getFile().getName());
                writer.println(file.getContent());
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to write JS file", e);
        }
    }

    private void writeJsToFileIIFE(Collection<JSFile> jsFiles, File outFile, boolean append) {
        log("js-outfile: '%s'", outFile);
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(outFile, append)))) {
            writer.println("(function() {"); // TODO : Klappt das auch mit GraalVM?
            for (JSFile file : jsFiles) {
                log("add script content: %s", file.getFile().getName());
                writer.println(file.getContent());
            }
            writer.println("})();");
        } catch (IOException e) {
            throw new RuntimeException("Failed to write JS file", e);
        }
    }

    private void evalWithGraalVM(File jsFile) {
        var content = FileUtils.getContent(jsFile, "utf-8");
        Context context = Context.newBuilder("js")
                .allowAllAccess(true)
                .allowExperimentalOptions(true)
                .allowHostClassLoading(true)
                .allowHostClassLookup(c -> true)
                .allowPolyglotAccess(PolyglotAccess.ALL)
                .allowNativeAccess(true)
                .build();
        context.getBindings("js").putMember("window", System.out);
        var fixedContent = content + "\nvar window = {  addEventListener: function(a,b){}};\n";
        context.eval("js", content);
    }

    public static class WindowMock {
    }

    private void deleteIfExists(File file) {
        if (file.exists() && !file.delete()) {
            throw new RuntimeException("Unable to delete file: " + file);
        }
    }

    private JSFile toJSFile(File file) {
        var content = FileUtils.getContent(file, "utf-8");
        var analyzer = new JsContentAnalyzer(content);
        analyzer.analyze();
        return new JSFile(file, content, analyzer.getDeclaredClasses(), analyzer.getSuperClasses());
    }

    private File getOutDir(Project project) {
        File outDir = new File(project.getProjectDir(), "src/main/resources");
        if (!outDir.exists() && !outDir.mkdirs()) {
            throw new RuntimeException("Unable to create output directory: " + outDir);
        }
        if (!outDir.isDirectory()) {
            throw new RuntimeException("Not a directory: " + outDir);
        }
        return outDir;
    }

    private File getReleaseOutFile(Project project) {
        return new File(getOutDir(project), RELEASE_JS_OUTFILE);
    }

    private Stream<File> sourceDirs(Project project) {
        File root = getJsApiSrcRoot(project);
        File[] files = root.listFiles();
        if (files == null) return Stream.empty();
        return Arrays.stream(files).filter(File::isDirectory);
    }

    private Collection<File> releaseSourceDirs(Project project) {
        return sourceDirs(project)
                .filter(f -> !f.getName().equals("test"))
                .collect(Collectors.toSet());
    }

    private Collection<File> allSourceDirs(Project project) {
        return sourceDirs(project).collect(Collectors.toSet());
    }

    private File getJsApiSrcRoot(Project project) {
        return new File(project.getProjectDir(), "src/main/js");
    }

    private void log(String pattern, Object... args) {
        System.out.printf((pattern) + "%n", args);
    }
}