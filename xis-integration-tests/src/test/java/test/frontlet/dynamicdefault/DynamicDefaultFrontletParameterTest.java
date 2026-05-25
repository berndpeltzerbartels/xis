package test.frontlet.dynamicdefault;

import one.xis.context.IntegrationTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DynamicDefaultFrontletParameterTest {

    private IntegrationTestContext context;

    @BeforeEach
    void init() {
        context = IntegrationTestContext.builder()
                .withSingleton(DynamicDefaultFrontletPage.class)
                .withSingleton(DynamicOuterFrontlet.class)
                .withSingleton(DynamicInnerFormFrontlet.class)
                .build();
    }

    @Test
    void dynamicDefaultFrontletQueryParametersAreAvailableInNestedFormDataRequest() {
        var client = context.openPage("/dynamic-default-frontlet.html");

        assertThat(client.getDocument().getInputElementById("pipelineId").getValue()).isEqualTo("42");
        assertThat(client.getDocument().getInputElementById("stepId").getValue()).isEqualTo("7");
    }

    @Test
    void nestedDefaultFrontletKeepsOwnQueryParametersWhenParentRefreshes() {
        var client = context.openPage("/dynamic-default-frontlet.html");

        client.getDocument().getElementById("reload").click();

        assertThat(client.getDocument().getElementById("reloads").getInnerText()).isEqualTo("1");
        assertThat(client.getDocument().getInputElementById("pipelineId").getValue()).isEqualTo("42");
        assertThat(client.getDocument().getInputElementById("stepId").getValue()).isEqualTo("7");
    }
}
