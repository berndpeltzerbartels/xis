package one.xis.processor;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.codehaus.groovy.control.CompilerConfiguration;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import groovy.lang.GroovyClassLoader;

import static org.assertj.core.api.Assertions.assertThat;

class XISTestProcessorTest {

    @TempDir
    Path tempDir;

    @Test
    void generatesJavaStarterTestByDefault() throws IOException {
        Path outputDir = tempDir.resolve("generated");

        compilePageWithProcessor(outputDir);

        Path generatedTest = outputDir.resolve("example/ProbePageTest.java");
        assertThat(generatedTest).exists();
        assertThat(outputDir.resolve("example/ProbePageTest.groovy")).doesNotExist();
        assertThat(Files.readString(generatedTest))
                .contains("package example;")
                .contains("import one.xis.boot.test.XisBootTest;")
                .contains("import one.xis.test.InTestContext;")
                .contains("class ProbePageTest")
                .contains("@XisBootTest")
                .contains("private IntegrationTestContext context;")
                .contains("@InTestContext")
                .contains("private ProbePage probePage;")
                .contains("void test()")
                .contains("var client = context.openPage(\"/probe.html\");")
                .contains("assertNotNull(client.getDocument());");

        compileGeneratedJavaTest(generatedTest);
    }

    @Test
    void generatesGroovyStarterTestWhenRequested() throws IOException {
        Path outputDir = tempDir.resolve("generated");

        compilePageWithProcessor(outputDir, "-Axis.testLanguage=groovy");

        Path generatedTest = outputDir.resolve("example/ProbePageTest.groovy");
        assertThat(generatedTest).exists();
        assertThat(outputDir.resolve("example/ProbePageTest.java")).doesNotExist();
        assertThat(Files.readString(generatedTest))
                .contains("package example")
                .contains("import one.xis.boot.test.XisBootTest")
                .contains("import one.xis.test.InTestContext")
                .contains("class ProbePageTest")
                .contains("@XisBootTest")
                .contains("private IntegrationTestContext context")
                .contains("@InTestContext")
                .contains("private ProbePage probePage")
                .contains("void test()")
                .contains("def client = context.openPage('/probe.html')")
                .contains("assertNotNull(client.document)");

        compileGeneratedGroovyTest(generatedTest);
    }

    private void compilePageWithProcessor(Path outputDir, String... additionalOptions) throws IOException {
        writeProbePageSource();

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        assertThat(compiler).as("Tests must run on a JDK, not a JRE").isNotNull();

        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        try (StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, Locale.ROOT, StandardCharsets.UTF_8)) {
            Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjects(probePageSourceFile());
            List<String> options = new ArrayList<>();
            options.add("-proc:only");
            options.add("-classpath");
            options.add(System.getProperty("java.class.path"));
            options.add("-Axis.testOutputDir=" + outputDir.toAbsolutePath());
            options.addAll(List.of(additionalOptions));

            JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnostics, options, null, compilationUnits);
            task.setProcessors(List.of(new XISTestProcessor()));

            Boolean success = task.call();
            assertThat(success)
                    .as(() -> diagnosticsAsText(diagnostics))
                    .isTrue();
        }
    }

    private void writeProbePageSource() throws IOException {
        Path sourceFile = probePageSourceFile();
        if (Files.exists(sourceFile)) {
            return;
        }
        Files.createDirectories(sourceFile.getParent());
        Files.writeString(sourceFile, """
                package example;

                import one.xis.Page;

                @Page("/probe.html")
                class ProbePage {
                }
                """, StandardCharsets.UTF_8);
    }

    private Path probePageSourceFile() {
        return tempDir.resolve("src/example/ProbePage.java");
    }

    private void compileGeneratedJavaTest(Path generatedTest) throws IOException {
        Path classesDir = tempDir.resolve("java-test-classes");
        Files.createDirectories(classesDir);
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        assertThat(compiler).as("Tests must run on a JDK, not a JRE").isNotNull();

        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        try (StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, Locale.ROOT, StandardCharsets.UTF_8)) {
            Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjects(
                    probePageSourceFile(),
                    generatedTest
            );
            List<String> options = new ArrayList<>();
            options.add("-proc:none");
            options.add("-classpath");
            options.add(System.getProperty("java.class.path"));
            options.add("-d");
            options.add(classesDir.toString());

            Boolean success = compiler.getTask(null, fileManager, diagnostics, options, null, compilationUnits).call();
            assertThat(success)
                    .as(() -> diagnosticsAsText(diagnostics))
                    .isTrue();
        }
    }

    private void compileGeneratedGroovyTest(Path generatedTest) throws IOException {
        Path classesDir = tempDir.resolve("groovy-test-classes");
        compileProbePageClass(classesDir);

        CompilerConfiguration configuration = new CompilerConfiguration();
        configuration.setTargetDirectory(classesDir.toFile());
        try (GroovyClassLoader loader = new GroovyClassLoader(getClass().getClassLoader(), configuration)) {
            loader.addClasspath(classesDir.toString());
            loader.parseClass(generatedTest.toFile());
        }
    }

    private void compileProbePageClass(Path classesDir) throws IOException {
        Files.createDirectories(classesDir);
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        assertThat(compiler).as("Tests must run on a JDK, not a JRE").isNotNull();

        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        try (StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, Locale.ROOT, StandardCharsets.UTF_8)) {
            Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjects(probePageSourceFile());
            List<String> options = List.of(
                    "-proc:none",
                    "-classpath", System.getProperty("java.class.path"),
                    "-d", classesDir.toString()
            );
            Boolean success = compiler.getTask(null, fileManager, diagnostics, options, null, compilationUnits).call();
            assertThat(success)
                    .as(() -> diagnosticsAsText(diagnostics))
                    .isTrue();
        }
    }

    private String diagnosticsAsText(DiagnosticCollector<JavaFileObject> diagnostics) {
        StringBuilder builder = new StringBuilder();
        for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
            builder.append(diagnostic.getKind())
                    .append(": ")
                    .append(diagnostic.getMessage(Locale.ROOT))
                    .append(System.lineSeparator());
        }
        return builder.toString();
    }
}
