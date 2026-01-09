package test.widget.store.actionbutton;

import one.xis.context.IntegrationTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WidgetActionTest {
    private IntegrationTestContext context;

    @BeforeEach
    void init() {
        context = IntegrationTestContext.builder()
                .withSingleton(WidgetActionPage.class)
                .withSingleton(WidgetActionWidget.class)
                .build();
    }

    @Test
    void actionInWidgetUpdatesPageAndWidget() {
        var result = context.openPage("/widgetAction.html");

        // Initial values should be 5
        var counterOnPage = result.getDocument().getElementById("counter-value-page").getInnerText();
        var counterOnWidget = result.getDocument().getElementById("counter-value-widget").getInnerText();

        //  assertThat(counterOnPage).isEqualTo("5");
        assertThat(counterOnWidget).isEqualTo("0");

        // Click button in widget - this should trigger reactive state update
        result.getDocument().getElementById("increment-button").click();

        // Both page and widget should now show 6
        counterOnPage = result.getDocument().getElementById("counter-value-page").getInnerText();
        counterOnWidget = result.getDocument().getElementById("counter-value-widget").getInnerText();

        assertThat(counterOnPage).isEqualTo("1");
        assertThat(counterOnWidget).isEqualTo("1");
    }
}