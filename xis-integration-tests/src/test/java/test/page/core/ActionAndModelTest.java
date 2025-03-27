package test.page.core;

import one.xis.context.IntegrationTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ActionAndModelTest {

    private IntegrationTestContext testContext;

    @BeforeEach
    void init() {
        testContext = IntegrationTestContext.builder()
                .withSingleton(ActionAndModel.class)
                .build();
    }

    @Test
    @DisplayName("In case a methode has @Action and @ModelData, the model data is provided to the page after the action is executed.")
    void testModelAction() {
        var result = testContext.openPage("/actionAndModel.html");
        result.getDocument().getElementById("action-link").click();
        // Retrieve the value provided as model data. The key corresponds to the @ModelData annotation.
        String modelData = result.getDocument().getElementById("result").innerText;
        assertThat(modelData).isEqualTo("Processed: input");
    }
}