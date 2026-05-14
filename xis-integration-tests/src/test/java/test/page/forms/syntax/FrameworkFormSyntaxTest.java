package test.page.forms.syntax;

import one.xis.context.IntegrationTestContext;
import one.xis.test.dom.Document;
import one.xis.test.dom.OptionElement;
import one.xis.test.dom.TextareaElement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FrameworkFormSyntaxTest {

    private FrameworkFormSyntaxService service;
    private IntegrationTestContext context;

    @BeforeEach
    void init() {
        service = new FrameworkFormSyntaxService();
        context = IntegrationTestContext.builder()
                .withSingleton(FrameworkFormSyntaxPage.class)
                .withSingleton(AttributeFormSyntaxPage.class)
                .withSingleton(service)
                .build();
    }

    @Test
    void attributeSyntaxSubmitsAllFormElementValues() {
        var document = context.openPage("/attributeFormSyntax.html").getDocument();

        assertHtmlFormControls(document);
        submitChangedValues(document);

        assertSubmittedValues();
    }

    @Test
    void elementSyntaxNormalizesAndSubmitsAllFormElementValues() {
        var document = context.openPage("/frameworkFormSyntax.html").getDocument();

        assertHtmlFormControls(document);
        submitChangedValues(document);

        assertSubmittedValues();
    }

    private void assertHtmlFormControls(Document document) {
        assertThat(document.getElementById("form").getLocalName()).isEqualTo("form");
        assertThat(document.getElementById("name").getLocalName()).isEqualTo("input");
        assertThat(document.getElementById("description").getLocalName()).isEqualTo("textarea");
        assertThat(document.getElementById("category").getLocalName()).isEqualTo("select");
        assertThat(document.getElementById("active").getAttribute("type")).isEqualTo("checkbox");
        assertThat(document.getElementById("draft").getAttribute("type")).isEqualTo("radio");
        assertThat(document.getElementById("save").getLocalName()).isEqualTo("button");
    }

    private void submitChangedValues(Document document) {
        document.getInputElementById("name").setValue("Updated");
        var description = (TextareaElement) document.getElementById("description");
        description.setValue("Updated description");
        assertThat(description.getValue()).isEqualTo("Updated description");
        ((OptionElement) document.querySelector("option[value=\"2\"]")).select();
        document.getInputElementById("active").click();
        document.getInputElementById("choice-alpha").click();
        document.getInputElementById("choice-beta").click();
        document.getInputElementById("published").click();
        document.getElementById("save").click();
    }

    private void assertSubmittedValues() {
        assertThat(service.saved()).isNotNull();
        assertThat(service.saved().getName()).isEqualTo("Updated");
        assertThat(service.saved().getDescription()).isEqualTo("Updated description");
        assertThat(service.saved().getCategoryId()).isEqualTo(2);
        assertThat(service.saved().isActive()).isTrue();
        assertThat(service.saved().getChoices()).containsExactly("alpha", "beta");
        assertThat(service.saved().getStatus()).isEqualTo("PUBLISHED");
    }
}
