package one.xis.gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

import javax.script.Compilable;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.*;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        findJsSrcDirs(project).forEach(folder -> processJsSrcDir(folder, project));
    }

    private void processJsSrcDir(File jsSrcDir, Project project) {
        File jsOutFile = getJSOutDir(jsSrcDir, project);
        Collection<File> jsFiles = getJsFiles(jsSrcDir);
        jsFiles.forEach(this::compile);
        copyJsToFile(getJsFiles(jsSrcDir), jsOutFile);
        compile(jsOutFile);
    }

    private void copyJsToFile(Collection<File> jsSrcFiles, File jsOutFile) {
        try (PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(jsOutFile)))) {
            jsSrcFiles.forEach(file -> {
                String content = readContent(file);
                out.println(content);
                out.println();
            });

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private Collection<File> getJsFiles(File jsSrcDir) {
        return Arrays.stream(Objects.requireNonNull(jsSrcDir.listFiles((dir, name) -> name.endsWith(".js")))).collect(Collectors.toSet());
    }

    private void compile(File jsFile) {
        try {
            compiler.compile(readContent(jsFile));
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

    private File getJSOutDir(File jsSrcDir, Project project) {
        File javascriptDir = new File(project.getBuildDir(), "/resources/main/js");
        if (!javascriptDir.exists() && !javascriptDir.mkdirs()) {
            throw new IllegalStateException("can not create " + javascriptDir.getAbsolutePath());
        }
        return new File(javascriptDir, jsSrcDir.getName() + ".js");
    }

    private static String readContent(File file) {
        try {
            return Files.readString(file.toPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Stream<File> findJsSrcDirs(Project project) {
        return Arrays.stream(getJsSrcRoot(project).listFiles()).filter(Objects::nonNull).filter(this::isJsSrcDir);
    }

    private boolean isJsSrcDir(File folder) {
        if (!folder.isDirectory()) {
            return false;
        }
        return Arrays.stream(folder.listFiles()).filter(File::isFile).anyMatch(file -> file.getName().endsWith(".js"));
    }
}