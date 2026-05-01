package test.widget.store.form;

import one.xis.context.IntegrationTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class FrontletFormTest {
    private IntegrationTestContext context;

    @BeforeEach
    void init() {
        context = IntegrationTestContext.builder()
                .withSingleton(WidgetFormPage.class)
                .withSingleton(WidgetFormFrontlet.class)
                .build();
    }

    @Test
    void formActionInWidgetUpdatesPageAndWidget() {
        var client = context.openPage("/widgetForm.html");

        // Initial values should be 10
        var counterOnPage = client.getDocument().getElementById("counter-value-page").getInnerText();
        var counterOnFrontlet = client.getDocument().getElementById("counter-value-widget").getInnerText();

        assertThat(counterOnPage).isEqualTo("0");
        assertThat(counterOnFrontlet).isEqualTo("0");

        // Enter a value in the input field before submitting
        var inputField = client.getDocument().getElementById("increment-input");
        inputField.setAttribute("value", "3");

        // Submit form in widget - this should trigger reactive state update
        // Form has input value "3", so counter should become 13
        client.getDocument().getElementById("increment-form-button").click();

        // Both page and widget should now show 13 (10 + 3)
        counterOnPage = client.getDocument().getElementById("counter-value-page").getInnerText();
        counterOnFrontlet = client.getDocument().getElementById("counter-value-widget").getInnerText();

        assertThat(counterOnPage).isEqualTo("3");
        assertThat(counterOnFrontlet).isEqualTo("3");
    }
}
