package test.frontlet.parameters;

import one.xis.context.IntegrationTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ParameterCombinationTest {

    private IntegrationTestContext testContext;

    @BeforeEach
    void init() {
        testContext = IntegrationTestContext.builder()
                .withSingleton(ParameterTestPage.class)
                .withSingleton(FirstFrontlet.class)
                .withSingleton(SecondFrontlet.class)
                .build();
    }

    @Test
    @DisplayName("Frontlet receives both action parameter and container parameter")
    void frontletReceivesBothParameters() {
        // Open page with FirstFrontlet
        var client = testContext.openPage("/parameterTest.html");

        // Verify FirstFrontlet is loaded
        assertThat(client.getDocument().getElementByTagName("h2").getInnerText())
                .isEqualTo("First Frontlet");

        // Click action to switch to SecondFrontlet with actionParam
        var button = client.getDocument().getElementByTagName("button");
        button.click();

        // Verify SecondFrontlet is now loaded
        assertThat(client.getDocument().getElementByTagName("h2").getInnerText())
                .isEqualTo("Second Frontlet");

        // Verify action parameter from FrontletResponse
        assertThat(client.getDocument().getElementById("actionParam").getInnerText())
                .isEqualTo("Action Parameter: actionValue");

        // Verify container parameter from frontlet-container
        assertThat(client.getDocument().getElementById("containerParam").getInnerText())
                .isEqualTo("Container Parameter: containerValue");

    }
}
