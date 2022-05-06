import org.gradle.api.Plugin;
import org.gradle.api.Project;

import javax.script.Compilable;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.*;
import java.nio.file.Files;
import java.util.Objects;

public class JSPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        File jsOutfile = getJSOutfile(project);
        writeAllJsToFile(jsOutfile, project);
        compile(jsOutfile);
    }

    private void writeAllJsToFile(File jsOutfile, Project project) {
        new JSComposer(getJsSrcRoot(project), jsOutfile).writeAllToOutfile();
    }

    private void compile(File jsFile) {
        try {
            getCompiler().compile(getContent(jsFile));
        } catch (ScriptException e) {
            throw new RuntimeException("Javascript-compilation failed for file " + jsFile, e);
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


    private String getContent(File file) {
        try {
            return new String(Files.readAllBytes(file.toPath()), "utf-8");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    class JSComposer {
        private final File srcRoot;
        private final File outfile;

        JSComposer(File srcRoot, File outfile) {
            this.srcRoot = srcRoot;
            this.outfile = outfile;
        }

        void writeAllToOutfile() {
            try (PrintWriter jsWriter = getJsOutputWriter(outfile)) {
                evaluateDir(srcRoot, jsWriter);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        private PrintWriter getJsOutputWriter(File jsFile) {
            try {
                return new PrintWriter(new OutputStreamWriter(new FileOutputStream(jsFile), "utf-8"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        private void copyJs(File jsFile, PrintWriter out) throws IOException {
            System.out.println("add javascript-ssource: " + jsFile.getName());
            String line;
            out.print("/*-----------------------------------------");
            out.print(jsFile.getName());
            out.println("-----------------------------------------*/");
            try (BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(jsFile), "utf-8"))) {
                while ((line = in.readLine()) != null) {
                    out.println(line);
                }
            }
        }

        private void evaluate(File file, PrintWriter jsWriter) throws IOException {
            if (file.isDirectory()) {
                evaluateDir(file, jsWriter);
            } else if (file.getName().endsWith(".js")) {
                copyJs(file, jsWriter);
            }
        }

        private void evaluateDir(File dir, PrintWriter jsWriter) throws IOException {
            for (File f : Objects.requireNonNull(dir.listFiles())) {
                evaluate(f, jsWriter);
            }
        }

    }

}