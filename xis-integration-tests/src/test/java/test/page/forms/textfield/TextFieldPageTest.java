package test.page.forms.textfield;

import one.xis.context.IntegrationTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TextFieldPageTest {

    private IntegrationTestContext context;
    private TextFieldFormService service;

    @BeforeEach
    void init() {
        service = mock(TextFieldFormService.class);
        context = IntegrationTestContext.builder()
                .withSingleton(TextFieldPage.class)
                .withSingleton(service)
                .build();
    }

    @Test
    void mandatoryValidationMessageIsShown() {
        var initialModel = new TextFieldFormModel();
        when(service.getTextFieldFormModel()).thenReturn(initialModel);

        var result = context.openPage(TextFieldPage.class);
        var textField = result.getDocument().getInputElementById("theTextField");
        var submitButton = result.getDocument().getElementById("submitButton");

        textField.setValue(""); // leer lassen
        submitButton.click();

        // Feldspezifische Nachricht aus dem passenden <div> unter dem Textfeld
        var liError = result.getDocument().getElementByTagName("li");
        assertThat(liError.getInnerHTML()).contains("Benutzerdefinierte globale Pflichtfeldmeldung");
        assertThat(result.getDocument().getElementById("fieldMessage").getInnerText()).isEqualTo("Benutzerdefinierte Pflichtfeldmeldung");
    }

    @Test
    void incompatibleValueMessageIsShown() {
        var initialModel = new TextFieldFormModel();
        when(service.getTextFieldFormModel()).thenReturn(initialModel);

        var result = context.openPage(TextFieldPage.class);
        var textField = result.getDocument().getInputElementById("theTextField");
        var submitButton = result.getDocument().getElementById("submitButton");

        textField.setValue("!@#");
        submitButton.click();

        // Feldspezifische Nachricht aus dem passenden <div> unter dem Textfeld
        var fieldMessage = result.getDocument()
                .getElementByTagName("xis:message")
                .getTextContent();

        assertThat(fieldMessage).contains("Ungültige Eingabe");
    }

    @Test
    void globalValidationMessagesAreShown() {
        var initialModel = new TextFieldFormModel();
        when(service.getTextFieldFormModel()).thenReturn(initialModel);

        var result = context.openPage(TextFieldPage.class);
        var textField = result.getDocument().getInputElementById("theTextField");
        var submitButton = result.getDocument().getElementById("submitButton");
        textField.setValue(""); // leer lassen

        submitButton.click();

        var liError = result.getDocument().getElementByTagName("li");
        assertThat(liError.getInnerHTML()).contains("Benutzerdefinierte globale Pflichtfeldmeldung");
    }
}