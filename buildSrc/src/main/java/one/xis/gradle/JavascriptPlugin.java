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
        var jsFiles = FileUtils.files(getJsSrcRoot(project), "js").stream()
                .map(this::toJSFile)
                .collect(Collectors.toSet());
        var sortedJsFiles = JSFileSorter.sort(jsFiles);
        var outFile = new File(project.getBuildDir(), "/resources/main/js/xis.js");
        writeJsToFile(sortedJsFiles, outFile);
        compile(outFile);
    }

    private JSFile toJSFile(File file) {
        var content = FileUtils.getContent(file, "utf-8");
        var analyzer = new JsContentAnalyzer(content);
        analyzer.analyze();
        return new JSFile(file, content, analyzer.getDeclaredClasses(), analyzer.getSuperClasses());
    }

    private void writeJsToFile(Collection<JSFile> jsSrcFiles, File jsOutFile) {
        try (PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(jsOutFile)))) {
            jsSrcFiles.forEach(file -> {
                System.out.println("add script content: " + file.getFile().getName());
                out.println(file.getContent());
                out.println();
            });

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private void compile(File jsFile) {
        try {
            compiler.compile(FileUtils.getContent(jsFile, "utf-8"));
        } catch (ScriptException e) {
            throw new RuntimeException("Compilation failed for " + jsFile.getAbsolutePath() + ": " + e.getMessage());
        }
    }

    private Compilable getCompiler() {
        return (Compilable) new ScriptEngineManager().getEngineByName("graal.js");
    }

    private File getJsSrcRoot(Project project) {
        return new File(project.getProjectDir(), "src/main/resources/js");
    }

}