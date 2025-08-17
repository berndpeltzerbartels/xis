package test.page.forms;

import one.xis.context.IntegrationTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RadioButtonPageTest {

    private IntegrationTestContext context;
    private RadioButtonFormService service;

    @BeforeEach
    void init() {
        service = new RadioButtonFormService();
        context = IntegrationTestContext.builder()
                .withSingleton(RadioButtonPage.class)
                .withSingleton(service)
                .build();
    }

    @Test
    void selectRadioAndSubmit() {
        // Initial: kein Wert gewählt
        var initialModel = new RadioButtonFormModel();
        service.setModel(initialModel);

        var result = context.openPage(RadioButtonPage.class);
        var radio1 = result.getDocument().getInputElementById("radio-1");
        var radio2 = result.getDocument().getInputElementById("radio-2");

        // User wählt radio1 und submitted
        radio1.click();
        assertThat(radio1.isChecked()).isTrue();
        assertThat(radio2.isChecked()).isFalse();

        result.getDocument().getElementById("submitButton").click();

        // Service prüfen
        var savedModel = service.getModel();
        assertThat(savedModel.getValue()).isEqualTo(1);

        // Seite neu prüfen
        var newRadio1 = result.getDocument().getInputElementById("radio-1");
        var newRadio2 = result.getDocument().getInputElementById("radio-2");
        assertThat(newRadio1.isChecked()).isTrue();
        assertThat(newRadio2.isChecked()).isFalse();
    }

    @Test
    void selectRadio2AndSubmit() {
        var initialModel = new RadioButtonFormModel();
        service.setModel(initialModel);

        var result = context.openPage(RadioButtonPage.class);
        var radio2 = result.getDocument().getInputElementById("radio-2");

        radio2.click();
        assertThat(radio2.isChecked()).isTrue();

        result.getDocument().getElementById("submitButton").click();

        var savedModel = service.getModel();
        assertThat(savedModel.getValue()).isEqualTo(2);
    }

    @Test
    void submitWithNoSelection() {
        var initialModel = new RadioButtonFormModel();
        service.setModel(initialModel);

        var result = context.openPage(RadioButtonPage.class);
        result.getDocument().getElementById("submitButton").click();

        var savedModel = service.getModel();
        assertThat(savedModel.getValue()).isNull();
    }
}