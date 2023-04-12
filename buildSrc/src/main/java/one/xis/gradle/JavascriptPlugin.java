package one.xis.gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

import javax.script.Compilable;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.*;
import java.util.Collection;
import java.util.stream.Collectors;


/**
 * Writes all javascript-sources into a single file and compiles the result
 */
public class JavascriptPlugin implements Plugin<Project> {

    private final Compilable compiler;

    public JavascriptPlugin() {
        this.compiler = getCompiler();
    }

    @Override
    public void apply(Project project) {
        printfln("apply plugin for project %s", project.getDisplayName());
        var outFile = getOutFile(project);
        var jsFiles = FileUtils.files(getJsApiSrcRoot(project), "js").stream()
                .map(this::toJSFile)
                .filter(jsFile -> !jsFile.getFile().equals(outFile))
                .collect(Collectors.toSet());
        var sortedJsFiles = JSFileSorter.sort(jsFiles);
        printfln("js-outfile: '%s'", outFile);
        writeJsToFile(sortedJsFiles, outFile);
        compileAndEval(outFile);
    }


    private File getOutFile(Project project) {
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
        return new File(outDir, "xis.js");
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
            var script = compiler.compile(FileUtils.getContent(jsFile, "utf-8"));
            script.eval();
        } catch (ScriptException e) {
            throw new RuntimeException("Compilation failed for " + jsFile.getAbsolutePath() + ": " + e.getMessage());
        }

    }

    private Compilable getCompiler() {
        return (Compilable) new ScriptEngineManager().getEngineByName("graal.js");
    }

    private File getJsApiSrcRoot(Project project) {
        return new File(project.getProjectDir(), "src/main/resources/js");
    }

    private void printfln(String pattern, Object... args) {
        System.out.printf(pattern, args);
        System.out.println();
    }

}