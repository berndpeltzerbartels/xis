package test.widget;

import one.xis.context.IntegrationTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SimpleWidgetTest {

    private IntegrationTestContext testContext;

    @BeforeEach
    void init() {
        testContext = IntegrationTestContext.builder()
                .withSingleton(SimpleWidget.class)
                .withSingleton(WidgetPage.class)
                .build();
    }

    @Test
    void test() {
        testContext.getSingleton(WidgetPage.class).setWidgetId("SimpleWidget");
        var result = testContext.openPage(WidgetPage.class);

        assertThat(result.getDocument().getElementById("greeting").innerText).isEqualTo("Huhu !");
    }
}
