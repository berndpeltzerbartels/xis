package one.xis.gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

import javax.script.Compilable;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * Writes all javascript-sources into a single file and compiles the result
 */
public class JavascriptPlugin implements Plugin<Project> {
    private static final String API_OUT_FILE_NAME = "xis.js";
    private static final String API_TEST_OUT_FILE_NAME = "xis-test.js";

    @Override
    public void apply(Project project) {
        printfln("apply plugin for project %s", project.getDisplayName());
        var jsFiles = FileUtils.files(getJsApiSrcRoot(project), "js").stream()
                .filter(file -> !file.getName().equals(API_OUT_FILE_NAME))
                .filter(file -> !file.getName().equals(API_TEST_OUT_FILE_NAME))
                .map(this::toJSFile).collect(Collectors.toUnmodifiableSet());
        writeApiFile(jsFiles, project);
        writeApiTestFile(jsFiles, project);
    }

    private void writeApiFile(Set<JSFile> jsFiles, Project project) {
        var files = new HashSet<>(jsFiles);
        files.add(toJSFile(getHttpClientFile(project)));
        var outFile = getOutFile(project);
        outFile.delete();
        writeToOutFile(files, outFile);
    }

    private void writeApiTestFile(Set<JSFile> jsFiles, Project project) {
        var files = new HashSet<>(jsFiles);
        files.add(toJSFile(getHttpClientMockFile(project)));
        var outFile = getTestOutFile(project);
        outFile.delete();
        writeToOutFile(files, outFile);
    }

    private void writeToOutFile(Set<JSFile> jsFiles, File outFile) {
        var sortedJsFiles = JSFileSorter.sort(jsFiles);
        printfln("js-outfile: '%s'", outFile);
        writeJsToFile(sortedJsFiles, outFile);
        compileAndEval(outFile);

    }

    private File getOutFile(Project project) {
        return new File(getOutDir(project), "xis.js");
    }

    private File getTestOutFile(Project project) {
        return new File(getOutDir(project), "xis-test.js");
    }

    private File getOutDir(Project project) {
        var outDir = new File(project.getProjectDir(), "src/main/resources");
        if (!outDir.exists()) {
            printfln("creating %s", outDir);
            if (!outDir.mkdirs()) {
                throw new RuntimeException("can not create " + outDir);
            }
        } else {
            printfln("already present: %s", outDir);
        }
        if (!outDir.isDirectory()) {
            throw new RuntimeException("not a directory: " + outDir);
        }
        return outDir;
    }

    private JSFile toJSFile(File file) {
        var content = FileUtils.getContent(file, "utf-8");
        var analyzer = new JsContentAnalyzer(content);
        analyzer.analyze();
        return new JSFile(file, content, analyzer.getDeclaredClasses(), analyzer.getSuperClasses());
    }

    private void writeJsToFile(Collection<JSFile> jsSrcFiles, File jsOutFile) {
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(jsOutFile)))) {
            jsSrcFiles.forEach(file -> {
                printfln("add script content: %s", file.getFile().getName());
                writer.println(file.getContent());
            });
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private void compileAndEval(File jsFile) {
        try {
            var script = getCompiler().compile(FileUtils.getContent(jsFile, "utf-8"));
            script.eval();
        } catch (ScriptException e) {
            throw new RuntimeException("Compilation failed for " + jsFile.getAbsolutePath() + ": " + e.getMessage() + " at line " + e.getLineNumber() + ", column " + e.getColumnNumber());
        }

    }

    private Compilable getCompiler() {
        return (Compilable) new ScriptEngineManager().getEngineByName("graal.js");
    }

    private File getJsApiSrcRoot(Project project) {
        return new File(project.getProjectDir(), "src/main/resources/js");
    }

    private File getHttpClientFile(Project project) {
        return new File(project.getProjectDir(), "src/main/resources/HttpClient.js");
    }

    private File getHttpClientMockFile(Project project) {
        return new File(project.getProjectDir(), "src/main/resources/HttpClientMock.js");
    }

    private void printfln(String pattern, Object... args) {
        System.out.printf(pattern, args);
        System.out.println();
    }

}