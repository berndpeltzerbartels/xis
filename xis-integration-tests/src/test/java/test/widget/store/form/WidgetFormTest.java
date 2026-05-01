package test.widget.store.form;

import one.xis.context.IntegrationTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class WidgetFormTest {
    private IntegrationTestContext context;

    @BeforeEach
    void init() {
        context = IntegrationTestContext.builder()
                .withSingleton(WidgetFormPage.class)
                .withSingleton(WidgetFormWidget.class)
                .build();
    }

    @Test
    void formActionInWidgetUpdatesPageAndWidget() {
        var client = context.openPage("/widgetForm.html");

        // Initial values should be 10
        var counterOnPage = client.getDocument().getElementById("counter-value-page").getInnerText();
        var counterOnWidget = client.getDocument().getElementById("counter-value-widget").getInnerText();

        assertThat(counterOnPage).isEqualTo("0");
        assertThat(counterOnWidget).isEqualTo("0");

        // Enter a value in the input field before submitting
        var inputField = client.getDocument().getElementById("increment-input");
        inputField.setAttribute("value", "3");

        // Submit form in widget - this should trigger reactive state update
        // Form has input value "3", so counter should become 13
        client.getDocument().getElementById("increment-form-button").click();

        // Both page and widget should now show 13 (10 + 3)
        counterOnPage = client.getDocument().getElementById("counter-value-page").getInnerText();
        counterOnWidget = client.getDocument().getElementById("counter-value-widget").getInnerText();

        assertThat(counterOnPage).isEqualTo("3");
        assertThat(counterOnWidget).isEqualTo("3");
    }
}