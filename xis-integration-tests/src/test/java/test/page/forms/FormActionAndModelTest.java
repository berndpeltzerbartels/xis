package test.page.forms;

import one.xis.context.IntegrationTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FormActionAndModelTest {

    private IntegrationTestContext context;

    @BeforeEach
    void init() {
        context = IntegrationTestContext.builder()
                .withSingleton(FormActionAndModel.class)
                .build();
    }

    @Test
    void testModelAction() {
        var client = context.openPage("/formActionAndModel.html");
        var inputElement = client.getDocument().getInputElementById("field1");
        inputElement.setValue("input");
        assertThat(client.getDocument().getElementById("result").getInnerText()).isEqualTo("Loaded from model method");
        assertThat(client.getDocument().getElementById("other").getInnerText()).isEqualTo("Other model call 1");

        client.getDocument().getElementById("save").click();

        String modelData = client.getDocument().getElementById("result").getInnerText();
        assertThat(modelData).isEqualTo("Processed: input");
        assertThat(client.getDocument().getElementById("other").getInnerText()).isEqualTo("Other model call 2");
    }

    @Test
    void nativeFormSubmitUsesDefaultActionButton() {
        var client = context.openPage("/formActionAndModel.html");
        var inputElement = client.getDocument().getInputElementById("field1");
        inputElement.setValue("input from submit");

        client.getDocument().querySelector("form").submit();

        String modelData = client.getDocument().getElementById("result").getInnerText();
        assertThat(modelData).isEqualTo("Processed: input from submit");
        assertThat(client.getDocument().getElementById("other").getInnerText()).isEqualTo("Other model call 2");
    }
}
