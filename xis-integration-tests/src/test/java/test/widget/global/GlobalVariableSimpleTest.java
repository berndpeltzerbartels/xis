package test.widget.global;

import one.xis.context.IntegrationTestContext;
import one.xis.test.dom.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Disabled
class GlobalVariableSimpleTest {

    private IntegrationTestContext context;

    @BeforeEach
    void setUp() {
        context = IntegrationTestContext.builder()
                .withSingleton(GlobalVariablePage.class)
                .withSingleton(GlobalVariableWidget.class)
                .build();
    }

    @Test
    void test() {
        // Given: Page is loaded with initial shared value
        var result = context.openPage(GlobalVariablePage.class);

        Document document = result.getDocument();
        assertThat(document.getElementById("globalVariablesOnPage").getInnerText()).isEqualTo("123456");
        assertThat(document.getElementById("globalVariablesOnWidget").getInnerText()).isEqualTo("123456");
    }


}