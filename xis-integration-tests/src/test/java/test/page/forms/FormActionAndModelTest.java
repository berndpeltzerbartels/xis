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
        var result = context.openPage("/formActionAndModel.html");
        var inputElement = result.getDocument().getInputElementById("field1");
        inputElement.setValue("input");
        result.getDocument().getElementById("save").click();
        // Retrieve the value provided as model data. The key corresponds to the @ModelData annotation.

        String modelData = result.getDocument().getElementById("result").getInnerText();
        assertThat(modelData).isEqualTo("Processed: input");
    }
}
