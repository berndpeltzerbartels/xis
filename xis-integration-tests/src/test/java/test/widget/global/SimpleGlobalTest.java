package test.widget.global;

import one.xis.context.IntegrationTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SimpleGlobalTest {
    private IntegrationTestContext context;

    @BeforeEach
    void init() {
        context = IntegrationTestContext.builder()
                .withSingleton(SimpleGlobalPage.class)
                .withSingleton(SimpleGlobalWidget.class)
                .build();
    }

    @Test
    void showStateVariable() {
        var result = context.openPage("/simpleReactive.html");
        var counterOnPage = result.getDocument().getElementById("counter-value-page").getInnerText();
        var counterOnWidget = result.getDocument().getElementById("counter-value-widget").getInnerText();

        assertThat(counterOnPage).isEqualTo("1");
        assertThat(counterOnWidget).isEqualTo("1");

        result.getDocument().getElementById("increment-link").click();

        counterOnPage = result.getDocument().getElementById("counter-value-page").getInnerText();
        counterOnWidget = result.getDocument().getElementById("counter-value-widget").getInnerText();

        assertThat(counterOnPage).isEqualTo("2");
        assertThat(counterOnWidget).isEqualTo("2");
    }
}
