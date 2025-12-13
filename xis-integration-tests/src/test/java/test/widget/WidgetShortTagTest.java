package test.widget;

import one.xis.context.IntegrationTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WidgetShortTagTest {

    private IntegrationTestContext testContext;

    @BeforeEach
    void init() {
        testContext = IntegrationTestContext.builder()
                .withSingleton(SimpleWidget.class)
                .withSingleton(WidgetShortTagPage.class)
                .build();
    }

    @Test
    void test() {
        testContext.getSingleton(WidgetShortTagPage.class).setWidgetId("SimpleWidget");
        var result = testContext.openPage(WidgetPage.class);

        System.out.println(result.getDocument().asString());
        assertThat(result.getDocument().getElementById("greeting").getInnerText()).isEqualTo("Huhu !");
    }
}
