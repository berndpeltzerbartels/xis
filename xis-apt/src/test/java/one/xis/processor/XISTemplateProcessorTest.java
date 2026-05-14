package one.xis.processor;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

class XISTemplateProcessorTest {

    @TempDir
    Path tempDir;

    @Test
    void generatedTemplateUsesModelDataFormDataAndActions() throws IOException {
        Path outputDir = tempDir.resolve("generated");

        boolean success = compile("""
                package example;

                import one.xis.Action;
                import one.xis.FormData;
                import one.xis.ModelData;
                import one.xis.Page;

                @Page("/customers.html")
                class CustomerPage {
                    @ModelData("customers")
                    java.util.List<Customer> customers() {
                        return java.util.List.of();
                    }

                    @FormData("customer")
                    CustomerForm customer() {
                        return new CustomerForm("", "", 0, false);
                    }

                    @Action("save")
                    void save(@FormData("customer") CustomerForm form) {
                    }

                    @Action
                    void refresh() {
                    }

                    record Customer(String name) {
                    }

                    record CustomerForm(String name, String email, int age, boolean active) {
                    }
                }
                """, outputDir);

        assertThat(success).isTrue();
        String html = Files.readString(outputDir.resolve("example/CustomerPage.html"), StandardCharsets.UTF_8);
        assertThat(html).contains("<ul xis:foreach=\"customer:${customers}\">");
        assertThat(html).contains("<li>${customer}</li>");
        assertThat(html).contains("<form xis:binding=\"customer\">");
        assertThat(html).contains("<xis:global-messages/>");
        assertThat(html).contains("<label for=\"customer-name\" xis:error-binding=\"name\" xis:error-class=\"error\">Name</label>");
        assertThat(html).contains("<input id=\"customer-email\" type=\"text\" xis:binding=\"email\" xis:error-class=\"error\"/>");
        assertThat(html).contains("<input id=\"customer-age\" type=\"number\" xis:binding=\"age\" xis:error-class=\"error\"/>");
        assertThat(html).contains("<input id=\"customer-active\" type=\"checkbox\" xis:binding=\"active\" xis:error-class=\"error\"/>");
        assertThat(html).contains("<div xis:message-for=\"active\"></div>");
        assertThat(html).contains("<button type=\"button\" xis:action=\"save\">Save</button>");
        assertThat(html).contains("<button type=\"button\" xis:action=\"refresh\">Refresh</button>");
    }

    @Test
    void existingTemplateIsNotOverwritten() throws IOException {
        Path outputDir = tempDir.resolve("generated");
        Path existingTemplate = outputDir.resolve("example/ExistingPage.html");
        Files.createDirectories(existingTemplate.getParent());
        Files.writeString(existingTemplate, "<html>keep me</html>", StandardCharsets.UTF_8);

        boolean success = compile("""
                package example;

                import one.xis.ModelData;
                import one.xis.Page;

                @Page("/existing.html")
                class ExistingPage {
                    @ModelData("message")
                    String message() {
                        return "Hello";
                    }
                }
                """, outputDir);

        assertThat(success).isTrue();
        assertThat(Files.readString(existingTemplate, StandardCharsets.UTF_8)).isEqualTo("<html>keep me</html>");
    }

    private boolean compile(String source, Path outputDir) throws IOException {
        Path sourceFile = tempDir.resolve("src/main/java/example/TestPage.java");
        Files.createDirectories(sourceFile.getParent());
        Files.writeString(sourceFile, source, StandardCharsets.UTF_8);

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        assertThat(compiler).as("Tests must run on a JDK, not a JRE").isNotNull();

        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        try (StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, Locale.ROOT, StandardCharsets.UTF_8)) {
            Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjects(sourceFile);
            JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnostics, options(outputDir), null, compilationUnits);
            task.setProcessors(List.of(new XISTemplateProcessor()));
            return Boolean.TRUE.equals(task.call());
        }
    }

    private List<String> options(Path outputDir) {
        return List.of(
                "-proc:only",
                "-classpath",
                System.getProperty("java.class.path"),
                "-Axis.outputDir=" + outputDir.toAbsolutePath()
        );
    }
}
