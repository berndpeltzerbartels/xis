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
    void acceptsFrameworkFormBindingAsFormDataUsage() throws IOException {
        Path templateFile = tempDir.resolve("src/main/java/example/ProbePage.html");
        Files.createDirectories(templateFile.getParent());
        Files.writeString(templateFile, """
                <!DOCTYPE html>
                <html>
                  <body>
                    <xis:form binding="contact">
                      <xis:input binding="description"/>
                    </xis:form>
                  </body>
                </html>
                """, StandardCharsets.UTF_8);

        DiagnosticCollector<JavaFileObject> diagnostics = compilePageSourceWithProcessor(true, """
                package example;

                import one.xis.FormData;
                import one.xis.Page;

                @Page("/probe.html")
                class ProbePage {
                    @FormData("contact")
                    ContactForm contact() {
                        return new ContactForm();
                    }

                    static class ContactForm {
                        String description;
                    }
                }
                """);

        assertThat(errorMessages(diagnostics)).isEmpty();
    }

    @Test
    void acceptsThemeStandardFieldsAsFormAndModelDataUsage() throws IOException {
        Path templateFile = tempDir.resolve("src/main/java/example/ProbePage.html");
        Files.createDirectories(templateFile.getParent());
        Files.writeString(templateFile, """
                <!DOCTYPE html>
                <html>
                  <body>
                    <theme:form binding="contact" action="saveContact">
                      <theme:input binding="description" title="Description"/>
                      <theme:select binding="stage" title="Stage" options="stages"/>
                      <theme:textarea binding="notes" title="Notes"/>
                      <theme:checkbox binding="newsletter" title="Newsletter"/>
                      <theme:radio binding="preferredContact" title="Preferred contact" options="contactTypes" option-value="code" option-label="label"/>
                    </theme:form>
                  </body>
                </html>
                """, StandardCharsets.UTF_8);

        DiagnosticCollector<JavaFileObject> diagnostics = compilePageSourceWithProcessor(true, """
                package example;

                import one.xis.FormData;
                import one.xis.ModelData;
                import one.xis.Page;
                import java.util.List;

                @Page("/probe.html")
                class ProbePage {
                    @FormData("contact")
                    ContactForm contact() {
                        return new ContactForm();
                    }

                    @ModelData("stages")
                    List<String> stages() {
                        return List.of();
                    }

                    @ModelData("contactTypes")
                    List<String> contactTypes() {
                        return List.of();
                    }

                    static class ContactForm {
                        String description;
                        String stage;
                        String notes;
                        boolean newsletter;
                        String preferredContact;
                    }
                }
                """);

        assertThat(errorMessages(diagnostics)).isEmpty();
    }

    @Test
    void acceptsThemeFormPageAsFormDataUsage() throws IOException {
        Path templateFile = tempDir.resolve("src/main/java/example/ProbePage.html");
        Files.createDirectories(templateFile.getParent());
        Files.writeString(templateFile, """
                <!DOCTYPE html>
                <html>
                  <body>
                    <theme:form-page title="Contact" binding="contact" action="saveContact">
                      <theme:input binding="description" title="Description"/>
                    </theme:form-page>
                  </body>
                </html>
                """, StandardCharsets.UTF_8);

        DiagnosticCollector<JavaFileObject> diagnostics = compilePageSourceWithProcessor(true, """
                package example;

                import one.xis.FormData;
                import one.xis.Page;

                @Page("/probe.html")
                class ProbePage {
                    @FormData("contact")
                    ContactForm contact() {
                        return new ContactForm();
                    }

                    static class ContactForm {
                        String description;
                    }
                }
                """);

        assertThat(errorMessages(diagnostics)).isEmpty();
    }

    @Test
    void requiresThemeFieldTitles() throws IOException {
        Path templateFile = tempDir.resolve("src/main/java/example/ProbePage.html");
        Files.createDirectories(templateFile.getParent());
        Files.writeString(templateFile, """
                <!DOCTYPE html>
                <html>
                  <body>
                    <theme:form binding="contact">
                      <theme:input binding="description"/>
                      <theme:select binding="stage" options="stages"/>
                      <theme:textarea binding="notes"/>
                      <theme:checkbox binding="newsletter"/>
                      <theme:radio binding="preferredContact" options="contactTypes"/>
                    </theme:form>
                  </body>
                </html>
                """, StandardCharsets.UTF_8);

        DiagnosticCollector<JavaFileObject> diagnostics = compilePageSourceWithProcessor(false, """
                package example;

                import one.xis.FormData;
                import one.xis.ModelData;
                import one.xis.Page;
                import java.util.List;

                @Page("/probe.html")
                class ProbePage {
                    @FormData("contact")
                    ContactForm contact() {
                        return new ContactForm();
                    }

                    @ModelData("stages")
                    List<String> stages() {
                        return List.of();
                    }

                    @ModelData("contactTypes")
                    List<String> contactTypes() {
                        return List.of();
                    }

                    static class ContactForm {
                        String description;
                        String stage;
                        String notes;
                        boolean newsletter;
                        String preferredContact;
                    }
                }
                """, "-Axis.allErrors=true");

        List<String> errors = errorMessages(diagnostics);
        assertThat(errors).hasSize(5);
        assertThat(errors.get(0)).contains("ProbePage.html:5").contains("theme:input requires title.");
        assertThat(errors.get(1)).contains("ProbePage.html:6").contains("theme:select requires title.");
        assertThat(errors.get(2)).contains("ProbePage.html:7").contains("theme:textarea requires title.");
        assertThat(errors.get(3)).contains("ProbePage.html:8").contains("theme:checkbox requires title.");
        assertThat(errors.get(4)).contains("ProbePage.html:9").contains("theme:radio requires title.");
    }

    @Test
    void validatesThemeGridAndFieldSpans() throws IOException {
        Path templateFile = tempDir.resolve("src/main/java/example/ProbePage.html");
        Files.createDirectories(templateFile.getParent());
        Files.writeString(templateFile, """
                <!DOCTYPE html>
                <html>
                  <body>
                    <theme:form binding="contact">
                      <theme:grid columns="1">
                        <theme:input binding="description" title="Description" span="0"/>
                        <theme:select binding="stage" title="Stage" options="stages" span="10"/>
                        <theme:textarea binding="notes" title="Notes" span="0"/>
                        <theme:checkbox binding="newsletter" title="Newsletter" span="10"/>
                        <theme:radio binding="preferredContact" title="Preferred contact" options="contactTypes" span="0"/>
                      </theme:grid>
                    </theme:form>
                  </body>
                </html>
                """, StandardCharsets.UTF_8);

        DiagnosticCollector<JavaFileObject> diagnostics = compilePageSourceWithProcessor(false, """
                package example;

                import one.xis.FormData;
                import one.xis.ModelData;
                import one.xis.Page;
                import java.util.List;

                @Page("/probe.html")
                class ProbePage {
                    @FormData("contact")
                    ContactForm contact() {
                        return new ContactForm();
                    }

                    @ModelData("stages")
                    List<String> stages() {
                        return List.of();
                    }

                    @ModelData("contactTypes")
                    List<String> contactTypes() {
                        return List.of();
                    }

                    static class ContactForm {
                        String description;
                        String stage;
                        String notes;
                        boolean newsletter;
                        String preferredContact;
                    }
                }
                """, "-Axis.allErrors=true");

        List<String> errors = errorMessages(diagnostics);
        assertThat(errors).hasSize(6);
        assertThat(errors.get(0)).contains("ProbePage.html:5").contains("theme:grid columns must be a number between 2 and 11.");
        assertThat(errors.get(1)).contains("ProbePage.html:6").contains("theme:input span must be a number between 1 and 9.");
        assertThat(errors.get(2)).contains("ProbePage.html:7").contains("theme:select span must be a number between 1 and 9.");
        assertThat(errors.get(3)).contains("ProbePage.html:8").contains("theme:textarea span must be a number between 1 and 9.");
        assertThat(errors.get(4)).contains("ProbePage.html:9").contains("theme:checkbox span must be a number between 1 and 9.");
        assertThat(errors.get(5)).contains("ProbePage.html:10").contains("theme:radio span must be a number between 1 and 9.");
    }

    @Test
    void acceptsMixedThemeAndRegularFormFields() throws IOException {
        Path templateFile = tempDir.resolve("src/main/java/example/ProbePage.html");
        Files.createDirectories(templateFile.getParent());
        Files.writeString(templateFile, """
                <!DOCTYPE html>
                <html>
                  <body>
                    <form xis:binding="contact">
                      <input xis:binding="description"/>
                      <theme:input binding="email" title="E-mail"/>
                    </form>
                  </body>
                </html>
                """, StandardCharsets.UTF_8);

        DiagnosticCollector<JavaFileObject> diagnostics = compilePageSourceWithProcessor(true, """
                package example;

                import one.xis.FormData;
                import one.xis.Page;

                @Page("/probe.html")
                class ProbePage {
                    @FormData("contact")
                    ContactForm contact() {
                        return new ContactForm();
                    }

                    static class ContactForm {
                        String description;
                        String email;
                    }
                }
                """);

        assertThat(errorMessages(diagnostics)).isEmpty();
    }

    @Test
    void acceptsSharedValueParameterWithProviderMethod() throws IOException {
        writeTemplateUsingName();

        DiagnosticCollector<JavaFileObject> diagnostics = compilePageSourceWithProcessor(true, """
                package example;

                import one.xis.ModelData;
                import one.xis.Page;
                import one.xis.SharedValue;

                @Page("/probe.html")
                class ProbePage {
                    @SharedValue("customer")
                    Customer customer() {
                        return new Customer();
                    }

                    @ModelData("name")
                    String name(@SharedValue("customer") Customer customer) {
                        return customer.name;
                    }

                    static class Customer {
                        String name;
                    }
                }
                """);

        assertThat(errorMessages(diagnostics)).isEmpty();
    }

    @Test
    void rejectsSharedValueParameterWithoutProviderMethod() throws IOException {
        writeTemplateUsingName();

        DiagnosticCollector<JavaFileObject> diagnostics = compilePageSourceWithProcessor(false, """
                package example;

                import one.xis.ModelData;
                import one.xis.Page;
                import one.xis.SharedValue;

                @Page("/probe.html")
                class ProbePage {
                    @ModelData("name")
                    String name(@SharedValue("customer") Customer customer) {
                        return customer.name;
                    }

                    static class Customer {
                        String name;
                    }
                }
                """);

        List<String> errors = errorMessages(diagnostics);
        assertThat(errors).hasSize(1);
        assertThat(errors.get(0))
                .contains("@SharedValue parameter \"customer\"")
                .contains("has no matching @SharedValue method");
    }

    @Test
    void validatesThemeFormFieldBindingsAgainstFormObject() throws IOException {
        Path templateFile = tempDir.resolve("src/main/java/example/ProbePage.html");
        Files.createDirectories(templateFile.getParent());
        Files.writeString(templateFile, """
                <!DOCTYPE html>
                <html>
                  <body>
                    <theme:form binding="contact">
                      <theme:input binding="missing" title="Missing"/>
                    </theme:form>
                  </body>
                </html>
                """, StandardCharsets.UTF_8);

        DiagnosticCollector<JavaFileObject> diagnostics = compilePageSourceWithProcessor(false, """
                package example;

                import one.xis.FormData;
                import one.xis.Page;

                @Page("/probe.html")
                class ProbePage {
                    @FormData("contact")
                    ContactForm contact() {
                        return new ContactForm();
                    }

                    static class ContactForm {
                        String description;
                    }
                }
                """);

        List<String> errors = errorMessages(diagnostics);
        assertThat(errors).hasSize(1);
        assertThat(errors.get(0))
                .contains("ProbePage.html:5")
                .contains("Template binds field \"missing\" on @FormData \"contact\"");
    }

    @Test
    void validatesFormFieldBindingsAgainstFormObject() throws IOException {
        Path templateFile = tempDir.resolve("src/main/java/example/ProbePage.html");
        Files.createDirectories(templateFile.getParent());
        Files.writeString(templateFile, """
                <!DOCTYPE html>
                <html>
                  <body>
                    <form xis:binding="customer">
                      <input xis:binding="name"/>
                      <input xis:binding="missing"/>
                    </form>
                  </body>
                </html>
                """, StandardCharsets.UTF_8);

        DiagnosticCollector<JavaFileObject> diagnostics = compilePageSourceWithProcessor(false, """
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
                        String name;
                    }
                }
                """);

        List<String> errors = errorMessages(diagnostics);
        assertThat(errors).hasSize(1);
        assertThat(errors.get(0))
                .contains("ProbePage.html:6")
                .contains("Template binds field \"missing\" on @FormData \"customer\"");
    }

    @Test
    void doesNotUseFormDataAsTemplateExpressionRoot() throws IOException {
        Path templateFile = tempDir.resolve("src/main/java/example/ProbePage.html");
        Files.createDirectories(templateFile.getParent());
        Files.writeString(templateFile, """
                <!DOCTYPE html>
                <html>
                  <body>
                    <xis:form binding="customer">
                      <xis:input binding="name"/>
                    </xis:form>
                    <p>${customer.name}</p>
                  </body>
                </html>
                """, StandardCharsets.UTF_8);

        DiagnosticCollector<JavaFileObject> diagnostics = compilePageSourceWithProcessor(false, """
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
                        String name;
                    }
                }
                """);

        List<String> errors = errorMessages(diagnostics);
        assertThat(errors).hasSize(1);
        assertThat(errors.get(0))
                .contains("Template uses \"customer\"")
                .contains("no @ModelData");
    }

    @Test
    void acceptsBeanPropertiesAsFormFields() throws IOException {
        Path templateFile = tempDir.resolve("src/main/java/example/ProbePage.html");
        Files.createDirectories(templateFile.getParent());
        Files.writeString(templateFile, """
                <!DOCTYPE html>
                <html>
                  <body>
                    <xis:form binding="customer">
                      <xis:input binding="firstName"/>
                      <xis:checkbox binding="active"/>
                    </xis:form>
                  </body>
                </html>
                """, StandardCharsets.UTF_8);

        DiagnosticCollector<JavaFileObject> diagnostics = compilePageSourceWithProcessor(true, """
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
                        String getFirstName() {
                            return "";
                        }

                        boolean isActive() {
                            return true;
                        }
                    }
                }
                """);

        assertThat(errorMessages(diagnostics)).isEmpty();
    }

    @Test
    void acceptsRecordComponentsAsFormFields() throws IOException {
        Path templateFile = tempDir.resolve("src/main/java/example/ProbePage.html");
        Files.createDirectories(templateFile.getParent());
        Files.writeString(templateFile, """
                <!DOCTYPE html>
                <html>
                  <body>
                    <xis:form binding="customer">
                      <xis:input binding="name"/>
                    </xis:form>
                  </body>
                </html>
                """, StandardCharsets.UTF_8);

        DiagnosticCollector<JavaFileObject> diagnostics = compilePageSourceWithProcessor(true, """
                package example;

                import one.xis.FormData;
                import one.xis.Page;

                @Page("/probe.html")
                class ProbePage {
                    @FormData("customer")
                    CustomerForm customer() {
                        return new CustomerForm("");
                    }

                    record CustomerForm(String name) {
                    }
                }
                """);

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
    void validatesModelDataPropertyPaths() throws IOException {
        Path templateFile = tempDir.resolve("src/main/java/example/ProbePage.html");
        Files.createDirectories(templateFile.getParent());
        Files.writeString(templateFile, """
                <!DOCTYPE html>
                <html>
                  <body>
                    <p>${customer.address.city}</p>
                    <p>${customer.address.IHMehl}</p>
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
                    Customer customer() {
                        return new Customer();
                    }

                    static class Customer {
                        Address address;
                    }

                    static class Address {
                        String city;
                    }
                }
                """);

        List<String> errors = errorMessages(diagnostics);
        assertThat(errors).hasSize(1);
        assertThat(errors.get(0))
                .contains("Template uses property \"IHMehl\"")
                .contains("customer.address.IHMehl");
    }

    @Test
    void validatesRepeatVariablePropertyPaths() throws IOException {
        Path templateFile = tempDir.resolve("src/main/java/example/ProbePage.html");
        Files.createDirectories(templateFile.getParent());
        Files.writeString(templateFile, """
                <!DOCTYPE html>
                <html>
                  <body>
                    <article xis:repeat="customer:customers">
                      <strong>${customer.name}</strong>
                      <span>${customer.IHMehl}</span>
                    </article>
                  </body>
                </html>
                """, StandardCharsets.UTF_8);

        DiagnosticCollector<JavaFileObject> diagnostics = compilePageSourceWithProcessor(false, """
                package example;

                import one.xis.ModelData;
                import one.xis.Page;
                import java.util.List;

                @Page("/probe.html")
                class ProbePage {
                    @ModelData("customers")
                    List<Customer> customers() {
                        return List.of();
                    }

                    static class Customer {
                        String name;
                    }
                }
                """);

        List<String> errors = errorMessages(diagnostics);
        assertThat(errors).hasSize(1);
        assertThat(errors.get(0))
                .contains("Template uses property \"IHMehl\"")
                .contains("customer.IHMehl");
    }

    @Test
    void validatesInheritedModelDataProperties() throws IOException {
        Path templateFile = tempDir.resolve("src/main/java/example/ProbePage.html");
        Files.createDirectories(templateFile.getParent());
        Files.writeString(templateFile, """
                <!DOCTYPE html>
                <html>
                  <body>
                    <p>${customer.inheritedName}</p>
                  </body>
                </html>
                """, StandardCharsets.UTF_8);

        DiagnosticCollector<JavaFileObject> diagnostics = compilePageSourceWithProcessor(true, """
                package example;

                import one.xis.ModelData;
                import one.xis.Page;

                @Page("/probe.html")
                class ProbePage {
                    @ModelData("customer")
                    Customer customer() {
                        return new Customer();
                    }

                    static class BaseCustomer {
                        String inheritedName;
                    }

                    static class Customer extends BaseCustomer {
                    }
                }
                """);

        assertThat(errorMessages(diagnostics)).isEmpty();
    }

    @Test
    void validatesRepeatExpressionsAgainstModelData() throws IOException {
        Path templateFile = tempDir.resolve("src/main/java/example/ProbePage.html");
        Files.createDirectories(templateFile.getParent());
        Files.writeString(templateFile, """
                <!DOCTYPE html>
                <html>
                  <body>
                    <article xis:repeat="customer:customers" class="${customer.active ? 'active' : ''}">
                      <strong>${customer.name}</strong>
                    </article>
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
    void rejectsComplexActionParameterValues() throws IOException {
        Path templateFile = tempDir.resolve("src/main/java/example/ProbePage.html");
        Files.createDirectories(templateFile.getParent());
        Files.writeString(templateFile, """
                <!DOCTYPE html>
                <html>
                  <body>
                    <button xis:action="select">Select</button>
                  </body>
                </html>
                """, StandardCharsets.UTF_8);

        DiagnosticCollector<JavaFileObject> diagnostics = compilePageSourceWithProcessor(false, """
                package example;

                import one.xis.Action;
                import one.xis.Page;
                import one.xis.Parameter;

                @Page("/probe.html")
                class ProbePage {
                    @Action
                    void select(@Parameter("step") StepId step) {
                    }

                    static class StepId {
                        String value;
                    }
                }
                """);

        List<String> errors = errorMessages(diagnostics);
        assertThat(errors).hasSize(1);
        assertThat(errors.get(0)).contains("@Parameter supports only simple values");
    }

    @Test
    void allowsSimpleActionParameterValues() throws IOException {
        Path templateFile = tempDir.resolve("src/main/java/example/ProbePage.html");
        Files.createDirectories(templateFile.getParent());
        Files.writeString(templateFile, """
                <!DOCTYPE html>
                <html>
                  <body>
                    <button xis:action="select">Select</button>
                  </body>
                </html>
                """, StandardCharsets.UTF_8);

        DiagnosticCollector<JavaFileObject> diagnostics = compilePageSourceWithProcessor(true, """
                package example;

                import java.math.BigDecimal;
                import java.time.LocalDate;
                import one.xis.Action;
                import one.xis.Page;
                import one.xis.Parameter;

                @Page("/probe.html")
                class ProbePage {
                    @Action
                    void select(@Parameter("id") long id,
                                @Parameter("name") String name,
                                @Parameter("amount") BigDecimal amount,
                                @Parameter("date") LocalDate date,
                                @Parameter("stage") Stage stage) {
                    }

                    enum Stage {
                        TODO
                    }
                }
                """);

        assertThat(errorMessages(diagnostics)).isEmpty();
    }

    @Test
    void allowsUnnamedParameterMapWithSimpleValues() throws IOException {
        Path templateFile = tempDir.resolve("src/main/java/example/ProbePage.html");
        Files.createDirectories(templateFile.getParent());
        Files.writeString(templateFile, """
                <!DOCTYPE html>
                <html>
                  <body>
                    <button xis:action="select">Select</button>
                  </body>
                </html>
                """, StandardCharsets.UTF_8);

        DiagnosticCollector<JavaFileObject> diagnostics = compilePageSourceWithProcessor(true, """
                package example;

                import java.util.Map;
                import one.xis.Action;
                import one.xis.Page;
                import one.xis.Parameter;

                @Page("/probe.html")
                class ProbePage {
                    @Action
                    void select(@Parameter Map<String, Integer> parameters) {
                    }
                }
                """);

        assertThat(errorMessages(diagnostics)).isEmpty();
    }

    @Test
    void rejectsUnnamedParameterMapWithoutStringKeys() throws IOException {
        Path templateFile = tempDir.resolve("src/main/java/example/ProbePage.html");
        Files.createDirectories(templateFile.getParent());
        Files.writeString(templateFile, """
                <!DOCTYPE html>
                <html>
                  <body>
                    <button xis:action="select">Select</button>
                  </body>
                </html>
                """, StandardCharsets.UTF_8);

        DiagnosticCollector<JavaFileObject> diagnostics = compilePageSourceWithProcessor(false, """
                package example;

                import java.util.Map;
                import one.xis.Action;
                import one.xis.Page;
                import one.xis.Parameter;

                @Page("/probe.html")
                class ProbePage {
                    @Action
                    void select(@Parameter Map<Integer, String> parameters) {
                    }
                }
                """);

        List<String> errors = errorMessages(diagnostics);
        assertThat(errors).hasSize(1);
        assertThat(errors.get(0)).contains("Unnamed @Parameter maps must use String keys.");
    }

    @Test
    void rejectsUnnamedParameterMapWithComplexValues() throws IOException {
        Path templateFile = tempDir.resolve("src/main/java/example/ProbePage.html");
        Files.createDirectories(templateFile.getParent());
        Files.writeString(templateFile, """
                <!DOCTYPE html>
                <html>
                  <body>
                    <button xis:action="select">Select</button>
                  </body>
                </html>
                """, StandardCharsets.UTF_8);

        DiagnosticCollector<JavaFileObject> diagnostics = compilePageSourceWithProcessor(false, """
                package example;

                import java.util.Map;
                import one.xis.Action;
                import one.xis.Page;
                import one.xis.Parameter;

                @Page("/probe.html")
                class ProbePage {
                    @Action
                    void select(@Parameter Map<String, StepId> parameters) {
                    }

                    static class StepId {
                        String value;
                    }
                }
                """);

        List<String> errors = errorMessages(diagnostics);
        assertThat(errors).hasSize(1);
        assertThat(errors.get(0)).contains("Unnamed @Parameter maps support only simple value types.");
    }

    @Test
    void rejectsLoadAttributeOnActionFormDataReturnValue() throws IOException {
        Path templateFile = tempDir.resolve("src/main/java/example/ProbePage.html");
        Files.createDirectories(templateFile.getParent());
        Files.writeString(templateFile, """
                <!DOCTYPE html>
                <html>
                  <body>
                    <form xis:binding="step">
                      <input xis:binding="name">
                      <button xis:action="select">Select</button>
                    </form>
                  </body>
                </html>
                """, StandardCharsets.UTF_8);

        DiagnosticCollector<JavaFileObject> diagnostics = compilePageSourceWithProcessor(false, """
                package example;

                import one.xis.Action;
                import one.xis.FormData;
                import one.xis.ModelDataLoad;
                import one.xis.Page;

                @Page("/probe.html")
                class ProbePage {
                    @Action
                    @FormData(value = "step", load = ModelDataLoad.INITIAL)
                    StepForm select() {
                        return new StepForm();
                    }

                    static class StepForm {
                        String name;
                    }
                }
                """);

        List<String> errors = errorMessages(diagnostics);
        assertThat(errors).hasSize(1);
        assertThat(errors.get(0)).contains("@FormData load is only supported on form initialization methods");
    }

    @Test
    void reportsTemplateDataErrorsAtTheElementLine() throws IOException {
        Path templateFile = tempDir.resolve("src/main/java/example/ProbePage.html");
        Files.createDirectories(templateFile.getParent());
        Files.writeString(templateFile, """
                <!DOCTYPE html>
                <html>
                  <head>
                    <link rel="stylesheet" href="/probe.css"/>
                  </head>
                  <body>
                    <li>${missing.name}</li>
                  </body>
                </html>
                """, StandardCharsets.UTF_8);

        DiagnosticCollector<JavaFileObject> diagnostics = compilePageWithProcessor(false);

        List<String> errors = errorMessages(diagnostics);
        assertThat(errors).hasSize(1);
        assertThat(errors.get(0))
                .contains("ProbePage.html:7")
                .contains("Template uses \"missing\"");
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

    private void writeTemplateUsingName() throws IOException {
        Path templateFile = tempDir.resolve("src/main/java/example/ProbePage.html");
        Files.createDirectories(templateFile.getParent());
        Files.writeString(templateFile, """
                <!DOCTYPE html>
                <html>
                  <body>${name}</body>
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
                        String price;
                        String name;
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
