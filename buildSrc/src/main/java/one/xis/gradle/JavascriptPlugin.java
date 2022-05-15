package one.xis.gradle;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

import javax.script.Compilable;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

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
        List<JavascriptSrc> javascriptSources = getJavascriptSources(project);
        compile(javascriptSources);
        File jsOutfile = getJSOutfile(project);
        writeAllJsToFile(javascriptSources, jsOutfile);
        compile(jsOutfile);
        getOutfiles(project).forEach(file -> copyContent(jsOutfile, file));
    }

    private void copyContent(File jsOutfile, File file) {
        file.getParentFile().mkdirs();
        try {
            Files.copy(jsOutfile.toPath(), file.toPath(), REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("can not copy file to " + file.getAbsolutePath());
        }
    }

    private Collection<File> getOutfiles(Project project) {
        String outFilesRaw = (String) project.findProperty("javascript.outfiles");
        if (outFilesRaw == null) {
            return Collections.emptySet();
        }
        return Arrays.stream(outFilesRaw.split(",")).map(String::trim).map(path -> new File(project.getProjectDir(), path)).collect(Collectors.toList());
    }

    private void writeAllJsToFile(List<JavascriptSrc> javascriptSources, File jsOutfile) {
        new JavascriptWriter(jsOutfile).writeAllToFile(javascriptSources);
    }

    private List<JavascriptSrc> getJavascriptSources(Project project) {
        return new JavascripSrcCollector(getJsSrcRoot(project)).findSources();
    }

    private void compile(List<JavascriptSrc> sources) {
        sources.forEach(this::compile);
    }

    private void compile(JavascriptSrc src) {
        try {
            compiler.compile(src.getContent());
        } catch (ScriptException e) {
            throw new RuntimeException("Compilation failed for " + src.getFile().getAbsolutePath(), e);
        }
    }

    private void compile(File jsFile) {
        try {
            compiler.compile(readContent(jsFile));
        } catch (ScriptException e) {
            throw new RuntimeException("Compilation failed for " + jsFile.getAbsolutePath(), e);
        }
    }

    private Compilable getCompiler() {
        return (Compilable) new ScriptEngineManager().getEngineByName("graal.js");
    }

    private File getJsSrcRoot(Project project) {
        return new File(project.getProjectDir(), "src/main/resources/js");
    }

    private File getJSOutfile(Project project) {
        File javascriptDir = new File(project.getBuildDir(), "javascript");
        if (!javascriptDir.exists() && !javascriptDir.mkdirs()) {
            throw new IllegalStateException("can not create " + javascriptDir.getAbsolutePath());
        }
        return new File(javascriptDir, "xis-api.js");
    }

    private static String readContent(File file) {
        try {
            return Files.readString(file.toPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @RequiredArgsConstructor
    private static class JavascriptWriter {
        private final File jsOutfile;

        void writeAllToFile(List<JavascriptSrc> sources) {
            try (PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(jsOutfile)))) {
                writeAllJsToFile(sources, out);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }

        private void writeAllJsToFile(List<JavascriptSrc> sources, PrintWriter out) {
            sources.forEach(src -> writeJsToFile(src, out));
        }

        private void writeJsToFile(JavascriptSrc source, PrintWriter out) {
            out.print("/*-----------------------------------------");
            out.print(source.getFile().getName());
            out.println("-----------------------------------------*/");
            out.println(source.getContent());
        }

    }

    private static class JavascripSrcCollector {
        private final File srcRoot;
        private final List<JavascriptSrc> sources = new ArrayList<>();

        JavascripSrcCollector(File srcRoot) {
            this.srcRoot = srcRoot;
        }

        List<JavascriptSrc> findSources() {
            evaluateDir(srcRoot);
            return sources;
        }

        private void evaluate(File file) {
            if (file.isDirectory()) {
                evaluateDir(file);
            } else if (file.getName().endsWith(".js")) {
                addSource(file);
            }
        }

        private void addSource(File file) {
            this.sources.add(new JavascriptSrc(file));
        }

        private void evaluateDir(File dir) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File f : files) {
                    evaluate(f);
                }

            }

        }

    }

    @Data
    private static class JavascriptSrc {
        private final File file;
        private final String content;

        JavascriptSrc(File file) {
            this.file = file;
            this.content = readContent(file);
        }


    }

}