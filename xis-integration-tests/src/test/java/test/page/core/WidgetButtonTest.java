package test.page.core;

import one.xis.context.IntegrationTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WidgetButtonTest {

    private IntegrationTestContext testContext;

    @BeforeEach
    void init() {
        testContext = IntegrationTestContext.builder()
                .withSingleton(WidgetButtonPage.class)
                .withSingleton(WidgetButton1.class)
                .withSingleton(WidgetButton2.class)
                .build();
    }

    @Test
    @DisplayName("Click button to load widget, then click button to load another widget")
    void test() {
        var pageController = testContext.getSingleton(WidgetButtonPage.class);
        var widget1 = testContext.getSingleton(WidgetButton1.class);
        var widget2 = testContext.getSingleton(WidgetButton2.class);

        var result = testContext.openPage("/widgetButtonPage.html");
        assertThat(result.getDocument().getElementByTagName("title").getInnerText()).isEqualTo("Widget Button Test");
        assertThat(pageController.getInvocations()).isEqualTo(1);

        // Click button to load widget1
        result.getDocument().getElementById("loadWidget1").click();
        assertThat(widget1.getInvocations()).isEqualTo(1);
        assertThat(result.getDocument().getElementById("widgetContent").getInnerText()).contains("Widget 1 Content");

        // Click button to load widget2
        result.getDocument().getElementById("loadWidget2").click();
        assertThat(widget2.getInvocations()).isEqualTo(1);
        assertThat(result.getDocument().getElementById("widgetContent").getInnerText()).contains("Widget 2 Content");
    }
}
