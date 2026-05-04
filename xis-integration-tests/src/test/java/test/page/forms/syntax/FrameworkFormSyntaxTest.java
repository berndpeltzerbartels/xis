package test.page.forms.syntax;

import one.xis.context.IntegrationTestContext;
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
                .withSingleton(service)
                .build();
    }

    @Test
    void frameworkFormElementsNormalizeToWorkingHtmlControls() {
        var client = context.openPage("/frameworkFormSyntax.html");
        var document = client.getDocument();

        assertThat(document.getElementById("form").getLocalName()).isEqualTo("form");
        assertThat(document.getElementById("name").getLocalName()).isEqualTo("input");
        assertThat(document.getElementById("description").getLocalName()).isEqualTo("textarea");
        assertThat(document.getElementById("category").getLocalName()).isEqualTo("select");
        assertThat(document.getElementById("active").getAttribute("type")).isEqualTo("checkbox");
        assertThat(document.getElementById("draft").getAttribute("type")).isEqualTo("radio");
        assertThat(document.getElementById("save").getLocalName()).isEqualTo("button");

        document.getInputElementById("name").setValue("Updated");
        document.getInputElementById("active").click();
        document.getElementById("save").click();

        assertThat(service.saved()).isNotNull();
        assertThat(service.saved().getName()).isEqualTo("Updated");
        assertThat(service.saved().isActive()).isTrue();
    }
}
