package one.xis.processor;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

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

import static org.assertj.core.api.Assertions.assertThat;

class XISValidateProcessorTest {

    @TempDir
    Path tempDir;

    @Test
    void failsFastByDefault() throws IOException {
        writeInvalidTemplate();

        DiagnosticCollector<JavaFileObject> diagnostics = compilePageWithProcessor(false);

        List<String> errors = errorMessages(diagnostics);
        assertThat(errors).hasSize(1);
        assertThat(errors.get(0))
                .contains("src/main/java/example/ProbePage.html:5")
                .contains("xis:format requires xis:binding");
    }

    @Test
    void canReportAllErrors() throws IOException {
        writeInvalidTemplate();

        DiagnosticCollector<JavaFileObject> diagnostics = compilePageWithProcessor(false, "-Axis.allErrors=true");

        List<String> errors = errorMessages(diagnostics);
        assertThat(errors).hasSize(3);
        assertThat(errors.get(0)).contains("xis:format requires xis:binding");
        assertThat(errors.get(1)).contains("xis:error-class requires xis:binding or xis:error-binding");
        assertThat(errors.get(2)).contains("Template binds \"customer\", but no @ModelData or @FormData");
    }

    @Test
    void acceptsBindingAwareTemplateAttributes() throws IOException {
        writeValidTemplate();

        DiagnosticCollector<JavaFileObject> diagnostics = compilePageSourceWithProcessor(true, validPageSource());

        assertThat(errorMessages(diagnostics)).isEmpty();
    }

    @Test
    void validatesIterationExpressionsAgainstModelData() throws IOException {
        Path templateFile = tempDir.resolve("src/main/java/example/ProbePage.html");
        Files.createDirectories(templateFile.getParent());
        Files.writeString(templateFile, """
                <!DOCTYPE html>
                <html>
                  <body>
                    <ul xis:foreach="customer:${customers}">
                      <li>${customer.firstName}</li>
                    </ul>
                  </body>
                </html>
                """, StandardCharsets.UTF_8);

        DiagnosticCollector<JavaFileObject> diagnostics = compilePageSourceWithProcessor(true, """
                package example;

                import one.xis.ModelData;
                import one.xis.Page;

                @Page("/probe.html")
                class ProbePage {
                    @ModelData("customers")
                    Object customers() {
                        return new Object();
                    }
                }
                """);

        assertThat(errorMessages(diagnostics)).isEmpty();
    }

    @Test
    void reportsUnusedModelDataAndMissingExpressionRoot() throws IOException {
        Path templateFile = tempDir.resolve("src/main/java/example/ProbePage.html");
        Files.createDirectories(templateFile.getParent());
        Files.writeString(templateFile, """
                <!DOCTYPE html>
                <html>
                  <body>
                    <p>${customer.firstName}</p>
                  </body>
                </html>
                """, StandardCharsets.UTF_8);

        DiagnosticCollector<JavaFileObject> diagnostics = compilePageSourceWithProcessor(false, """
                package example;

                import one.xis.ModelData;
                import one.xis.Page;

                @Page("/probe.html")
                class ProbePage {
                    @ModelData("unused")
                    Object unused() {
                        return new Object();
                    }
                }
                """, "-Axis.allErrors=true");

        List<String> errors = errorMessages(diagnostics);
        assertThat(errors).hasSize(2);
        assertThat(errors.get(0)).contains("@ModelData \"unused\" is not used");
        assertThat(errors.get(1)).contains("Template uses \"customer\"");
        assertThat(errors).noneMatch(error -> error.contains("firstName"));
    }

    @Test
    void validatesDragDropAndSelectionRules() throws IOException {
        Path templateFile = tempDir.resolve("src/main/java/example/ProbePage.html");
        Files.createDirectories(templateFile.getParent());
        Files.writeString(templateFile, """
                <!DOCTYPE html>
                <html>
                  <body>
                    <div xis:selection-class="selected">Standalone</div>
                    <span xis:drag="piece">${piece}</span>
                    <div xis:drop="move">Drop</div>
                  </body>
                </html>
                """, StandardCharsets.UTF_8);

        DiagnosticCollector<JavaFileObject> diagnostics = compilePageSourceWithProcessor(false, """
                package example;

                import one.xis.ModelData;
                import one.xis.Page;

                @Page("/probe.html")
                class ProbePage {
                    @ModelData("piece")
                    Object piece() {
                        return new Object();
                    }
                }
                """, "-Axis.allErrors=true");

        List<String> errors = errorMessages(diagnostics);
        assertThat(errors).anyMatch(error -> error.contains("xis:selection-class requires a surrounding element"));
        assertThat(errors).anyMatch(error -> error.contains("xis:drag must use name:expression syntax"));
        assertThat(errors).anyMatch(error -> error.contains("xis:drop must use actionName(arg1, arg2) syntax"));
    }

    @Test
    void validatesHandlerAndNormalizerBackedAttributes() throws IOException {
        Path templateFile = tempDir.resolve("src/main/java/example/ProbePage.html");
        Files.createDirectories(templateFile.getParent());
        Files.writeString(templateFile, """
                <!DOCTYPE html>
                <html>
                  <body>
                    <a xis:page="">Broken page link</a>
                    <button xis:action="">Broken action</button>
                    <xis:a>Broken framework link</xis:a>
                    <xis:button>Broken framework button</xis:button>
                    <xis:parameter>Missing name</xis:parameter>
                    <section xis:storage-binding="unknown">
                      <span>${customer.name}</span>
                    </section>
                    <xis:form>
                      <xis:input binding="name"/>
                      <xis:message/>
                    </xis:form>
                    <main xis:default-frontlet="DetailsFrontlet"></main>
                  </body>
                </html>
                """, StandardCharsets.UTF_8);

        DiagnosticCollector<JavaFileObject> diagnostics = compilePageSourceWithProcessor(false, """
                package example;

                import one.xis.ModelData;
                import one.xis.Page;

                @Page("/probe.html")
                class ProbePage {
                    @ModelData("customer")
                    Object customer() {
                        return new Object();
                    }
                }
                """, "-Axis.allErrors=true");

        List<String> errors = errorMessages(diagnostics);
        assertThat(errors).anyMatch(error -> error.contains("xis:page requires a page URL or page id"));
        assertThat(errors).anyMatch(error -> error.contains("xis:action requires an action name"));
        assertThat(errors).anyMatch(error -> error.contains("xis:a requires page, frontlet, or modal"));
        assertThat(errors).anyMatch(error -> error.contains("xis:button requires page, frontlet, modal, or action"));
        assertThat(errors).anyMatch(error -> error.contains("xis:parameter requires name"));
        assertThat(errors).anyMatch(error -> error.contains("xis:storage-binding must be one of localStorage, sessionStorage, or clientStorage"));
        assertThat(errors).anyMatch(error -> error.contains("xis:form requires binding"));
        assertThat(errors).anyMatch(error -> error.contains("xis:message requires message-for"));
        assertThat(errors).anyMatch(error -> error.contains("xis:default-frontlet requires xis:frontlet-container"));
    }

    private void writeInvalidTemplate() throws IOException {
        Path templateFile = tempDir.resolve("src/main/java/example/ProbePage.html");
        Files.createDirectories(templateFile.getParent());
        Files.writeString(templateFile, """
                <!DOCTYPE html>
                <html>
                  <body>
                    <form xis:binding="customer">
                      <span xis:format="currency">12.00</span>
                      <label xis:error-class="error">Name</label>
                    </form>
                  </body>
                </html>
                """, StandardCharsets.UTF_8);
    }

    private void writeValidTemplate() throws IOException {
        Path templateFile = tempDir.resolve("src/main/java/example/ProbePage.html");
        Files.createDirectories(templateFile.getParent());
        Files.writeString(templateFile, """
                <!DOCTYPE html>
                <html>
                  <body>
                    <form xis:binding="customer">
                      <input xis:binding="price" xis:format="currency"/>
                      <label xis:error-binding="name" xis:error-class="error">Name</label>
                    </form>
                  </body>
                </html>
                """, StandardCharsets.UTF_8);
    }

    private DiagnosticCollector<JavaFileObject> compilePageWithProcessor(boolean expectedSuccess, String... additionalOptions) throws IOException {
        return compilePageSourceWithProcessor(expectedSuccess, defaultPageSource(), additionalOptions);
    }

    private DiagnosticCollector<JavaFileObject> compilePageSourceWithProcessor(boolean expectedSuccess,
                                                                              String source,
                                                                              String... additionalOptions) throws IOException {
        Path sourceFile = tempDir.resolve("src/main/java/example/ProbePage.java");
        Files.createDirectories(sourceFile.getParent());
        Files.writeString(sourceFile, source, StandardCharsets.UTF_8);

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        assertThat(compiler).as("Tests must run on a JDK, not a JRE").isNotNull();

        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        try (StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, Locale.ROOT, StandardCharsets.UTF_8)) {
            Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjects(sourceFile);
            List<String> options = compilerOptions(additionalOptions);
            JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnostics, options, null, compilationUnits);
            task.setProcessors(List.of(new XISValidateProcessor()));

            Boolean success = task.call();
            assertThat(success).isEqualTo(expectedSuccess);
        }
        return diagnostics;
    }

    private String defaultPageSource() {
        return """
                package example;

                import one.xis.Page;

                @Page("/probe.html")
                class ProbePage {
                }
                """;
    }

    private String validPageSource() {
        return """
                package example;

                import one.xis.FormData;
                import one.xis.Page;

                @Page("/probe.html")
                class ProbePage {
                    @FormData("customer")
                    CustomerForm customer() {
                        return new CustomerForm();
                    }

                    static class CustomerForm {
                    }
                }
                """;
    }

    private List<String> compilerOptions(String... additionalOptions) {
        List<String> options = new ArrayList<>();
        options.add("-proc:only");
        options.add("-classpath");
        options.add(System.getProperty("java.class.path"));
        options.add("-Axis.projectDir=" + tempDir.toAbsolutePath());
        options.addAll(List.of(additionalOptions));
        return options;
    }

    private List<String> errorMessages(DiagnosticCollector<JavaFileObject> diagnostics) {
        List<String> errors = new ArrayList<>();
        for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
            if (diagnostic.getKind() == Diagnostic.Kind.ERROR) {
                errors.add(diagnostic.getMessage(Locale.ROOT));
            }
        }
        return errors;
    }
}
