package one.xis.gradle;

import com.google.javascript.jscomp.CompilationLevel;
import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.JSError;
import com.google.javascript.jscomp.PropertyRenamingPolicy;
import com.google.javascript.jscomp.Result;
import com.google.javascript.jscomp.SourceFile;
import com.google.javascript.jscomp.SourceMap;
import com.google.javascript.jscomp.VariableRenamingPolicy;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.PolyglotAccess;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.logging.Level;

public class JavascriptPlugin implements Plugin<Project> {

    private static final String RELEASE_JS_OUTFILE = "xis.js";
    private static final String RELEASE_MIN_JS_OUTFILE = "xis.min.js";
    private static final String RELEASE_MIN_JS_MAP_OUTFILE = "xis.min.js.map";
    private static final String SERVED_JS_OUTFILE = "bundle.min.js";
    private static final String SERVED_JS_MAP_OUTFILE = "bundle.min.js.map";

    @Override
    public void apply(Project project) {
        log("apply plugin for project %s", project.getDisplayName());
        process(project);
    }

    private void process(Project project) {
        File releaseJsFile = getReleaseOutFile(project);
        File releaseMinJsFile = getReleaseMinOutFile(project);
        File releaseMinJsMapFile = getReleaseMinMapOutFile(project);
        deleteIfExists(releaseJsFile);
        deleteIfExists(releaseMinJsFile);
        deleteIfExists(releaseMinJsMapFile);

        List<JSFile> releaseJsFiles = getReleaseJsFiles(project);
        List<JSFile> allJsFiles = getAllJsFiles(project);

        writeGroupedFiles(project, allJsFiles);
        writeJsToFileIIFE(JSFileSorter.sort(releaseJsFiles), releaseJsFile, false);
        writeMinifiedJs(releaseJsFile, releaseMinJsFile, releaseMinJsMapFile);
        evalWithGraalVM(releaseJsFile);
    }

    private void writeMinifiedJs(File sourceFile, File minFile, File sourceMapFile) {
        log("js-outfile: '%s'", minFile);
        log("js-outfile: '%s'", sourceMapFile);

        Compiler.setLoggingLevel(Level.OFF);
        Compiler compiler = new Compiler();
        CompilerOptions options = new CompilerOptions();
        CompilationLevel.SIMPLE_OPTIMIZATIONS.setOptionsForCompilationLevel(options);
        options.setSourceMapOutputPath(SERVED_JS_MAP_OUTFILE);
        options.setSourceMapFormat(SourceMap.Format.V3);
        options.setSourceMapIncludeSourcesContent(true);
        options.setGenerateExports(false);
        options.setRenamingPolicy(VariableRenamingPolicy.OFF, PropertyRenamingPolicy.OFF);
        options.setPrettyPrint(false);

        SourceFile input = SourceFile.fromCode(RELEASE_JS_OUTFILE, FileUtils.getContent(sourceFile, "utf-8"));
        Result result = compiler.compile(List.of(), List.of(input), options);
        if (!result.success) {
            StringBuilder sb = new StringBuilder();
            for (JSError error : result.errors) {
                sb.append(error).append('\n');
            }
            throw new RuntimeException("JavaScript minification failed:\n" + sb);
        }

        try {
            String js = compiler.toSource().replaceFirst("(?m)\\R?//#\\s*sourceMappingURL=.*$", "");
            java.nio.file.Files.writeString(minFile.toPath(), js, StandardCharsets.UTF_8);
            try (var writer = new StringWriter()) {
                compiler.getSourceMap().appendTo(writer, SERVED_JS_OUTFILE);
                java.nio.file.Files.writeString(sourceMapFile.toPath(), writer.toString(), StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to write minified JavaScript files", e);
        }
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
            for (JSFile file : jsFiles) {
                log("add script content: %s", file.getFile().getName());
                writer.println(file.getContent());
            }
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

    private File getReleaseMinOutFile(Project project) {
        return new File(getOutDir(project), RELEASE_MIN_JS_OUTFILE);
    }

    private File getReleaseMinMapOutFile(Project project) {
        return new File(getOutDir(project), RELEASE_MIN_JS_MAP_OUTFILE);
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
