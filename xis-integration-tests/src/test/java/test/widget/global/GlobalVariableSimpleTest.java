package test.widget.global;

import one.xis.context.IntegrationTestContext;
import one.xis.test.dom.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalVariableSimpleTest {

    private IntegrationTestContext context;

    @BeforeEach
    void setUp() {
        context = IntegrationTestContext.builder()
                .withSingleton(GlobalVariablePage.class)
                .withSingleton(GlobalVariableWidget.class)
                .withSingleton(GlobalVariableWidget2.class)
                .build();
    }

    @Test
    void test() {
        // Given: Page is loaded with initial shared value
        var result = context.openPage(GlobalVariablePage.class);

        Document document = result.getDocument();
        document.getElementByTagName("button").click(); // Sets the global variable to "456"
        document.getElementByTagName("a").click(); // Navigates to another widget that reads the global variable

        assertThat(document.getElementById("globalVariablesOnWidget").getTextContent()).isEqualTo("456");
    }


}