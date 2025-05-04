package one.xis.gradle;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.PolyglotAccess;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

import javax.script.Compilable;
import javax.script.ScriptEngineManager;
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

    private static final String RELEASE_JS_OUTFILE = "xis-prod.js";

    @Override
    public void apply(Project project) {
        printfln("apply plugin for project %s", project.getDisplayName());
        process(project);
    }

    private void process(Project project) {
        var releaseJsFile = getReleaseOutFile(project);
        if (releaseJsFile.exists() && !releaseJsFile.delete()) {
            throw new RuntimeException("can not delete " + releaseJsFile);
        }

        var allJsFiles = sourceDirs(project).stream()
                .flatMap(dir -> FileUtils.files(dir, "js").stream())
                .map(this::toJSFile)
                .collect(Collectors.toSet());

        // 🔁 Einzelne .js-Dateien wie bisher (für Debugging oder Tests)
        var groupedByTopFolder = allJsFiles.stream()
                .collect(Collectors.groupingBy(file -> {
                    var fullPath = file.getFile().toPath();
                    var rootPath = getJsApiSrcRoot(project).toPath();
                    return rootPath.relativize(fullPath).getName(0).toString(); // top-level dir
                }));

        groupedByTopFolder.forEach((dirName, files) -> {
            var outFile = new File(getOutDir(project), dirName + ".js");
            var sorted = JSFileSorter.sort(files);
            writeToOutFile(sorted, outFile, false);
        });

        // ✅ Finale Release-Datei mit globaler Sortierung
        var globallySorted = JSFileSorter.sort(allJsFiles);
        writeJsToFile(globallySorted, releaseJsFile, false);
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


    private void writeToOutFile(List<JSFile> jsFiles, File outFile, boolean append) {
        printfln("js-outfile: '%s'", outFile);
        writeJsToFile(jsFiles, outFile, append);
        compileAndEval(outFile);
    }

    private File getReleaseOutFile(Project project) {
        return new File(getOutDir(project), RELEASE_JS_OUTFILE);
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

    private void writeJsToFile(Collection<JSFile> jsSrcFiles, File jsOutFile, boolean append) {
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(jsOutFile, append)))) {
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
        context.eval("js", content);
    }

    private Compilable getCompiler() {
        return (Compilable) new ScriptEngineManager().getEngineByName("js");
    }

    private File getJsApiSrcRoot(Project project) {
        return new File(project.getProjectDir(), "src/main/js");
    }

    private void printfln(String pattern, Object... args) {
        System.out.printf(pattern, args);
        System.out.println();
    }

}