package test.widget.parameters;

import one.xis.context.IntegrationTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WidgetParameterCombinationTest {

    private IntegrationTestContext testContext;

    @BeforeEach
    void init() {
        testContext = IntegrationTestContext.builder()
                .withSingleton(ParameterTestPage.class)
                .withSingleton(FirstWidget.class)
                .withSingleton(SecondWidget.class)
                .build();
    }

    @Test
    @DisplayName("Widget receives both action parameter and container parameter")
    void widgetReceivesBothParameters() {
        // Open page with FirstWidget
        var result = testContext.openPage("/parameterTest.html");

        // Verify FirstWidget is loaded
        assertThat(result.getDocument().getElementByTagName("h2").getInnerText())
                .isEqualTo("First Widget");

        // Click action to switch to SecondWidget with actionParam
        var button = result.getDocument().getElementByTagName("button");
        button.click();

        // Verify SecondWidget is now loaded
        assertThat(result.getDocument().getElementByTagName("h2").getInnerText())
                .isEqualTo("Second Widget");

        // Verify action parameter from WidgetResponse
        assertThat(result.getDocument().getElementById("actionParam").getInnerText())
                .isEqualTo("Action Parameter: actionValue");

        // Verify container parameter from widget-container
        assertThat(result.getDocument().getElementById("containerParam").getInnerText())
                .isEqualTo("Container Parameter: containerValue");

    }
}
