package one.xis.gradle;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.PolyglotAccess;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

import javax.script.Compilable;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


/**
 * Writes all javascript-sources into a single file and compiles the result
 */
public class JavascriptPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        printfln("apply plugin for project %s", project.getDisplayName());
        process(project);
    }

    private void process(Project project) {
        processSourceDirs(sourceDirs(project), project);
    }

    private void processSourceDirs(Collection<File> sourceDirs, Project project) {
        sourceDirs.forEach(dir -> processSourceDir(dir, project));
    }

    private void processSourceDir(File sourceDir, Project project) {
        var jsFiles = FileUtils.files(sourceDir, "js").stream()
                .map(this::toJSFile)
                .collect(Collectors.toSet());
        if (!jsFiles.isEmpty()) {
            var sortedJsFiles = JSFileSorter.sort(jsFiles);
            var outFile = outFileForSourceDir(sourceDir, project);
            writeToOutFile(sortedJsFiles, outFile);
        }
    }


    private Collection<File> sourceDirs(Project project) {
        var root = getJsApiSrcRoot(project);
        var files = root.listFiles();
        if (files == null) {
            return Collections.emptyList();
        }
        return Arrays.stream(files)
                .filter(File::isDirectory)
                .collect(Collectors.toSet());
    }


    private File outFileForSourceDir(File dir, Project project) {
        return new File(getOutDir(project), dir.getName() + ".js");
    }

    /*
    private void writeApiFile(Set<JSFile> jsFiles, Project project) {
        var files = new HashSet<>(jsFiles);
        //files.add(toJSFile(getHttpClientFile(project)));
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

     */

    private void writeToOutFile(List<JSFile> jsFiles, File outFile) {
        printfln("js-outfile: '%s'", outFile);
        writeJsToFile(jsFiles, outFile);
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
        var content = FileUtils.getContent(jsFile, "utf-8");
        Context context = Context.newBuilder("js")
                .allowAllAccess(true)
                .allowExperimentalOptions(true)
                .allowHostClassLoading(true)
                .allowHostClassLookup(c -> true)
                .allowPolyglotAccess(PolyglotAccess.ALL)
                .allowNativeAccess(true)
                .allowAllAccess(true)
                .build();
        context.eval("js",content);
    }

    private Compilable getCompiler() {
        return (Compilable) new ScriptEngineManager().getEngineByName("js");
    }

    private File getJsApiSrcRoot(Project project) {
        return new File(project.getProjectDir(), "src/main/js");
    }

    /*
    private File getHttpClientFile(Project project) {
        return new File(project.getProjectDir(), "src/main/resources/HttpClient.js");
    }

    private File getHttpClientMockFile(Project project) {
        return new File(project.getProjectDir(), "src/main/resources/HttpClientMock.js");
    }
    */

    private void printfln(String pattern, Object... args) {
        System.out.printf(pattern, args);
        System.out.println();
    }

}