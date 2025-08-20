package test.page.forms.textarea;

import one.xis.context.IntegrationTestContext;
import one.xis.test.dom.Element;
import one.xis.test.dom.TextareaElement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TextareaPageTest {

    private IntegrationTestContext context;
    private TextareaFormService service;

    @BeforeEach
    void init() {
        service = mock(TextareaFormService.class);
        context = IntegrationTestContext.builder()
                .withSingleton(TextareaPage.class)
                .withSingleton(service)
                .build();
    }

    @Test
    void mandatoryValidationMessageIsShown() {
        var initialModel = new TextareaFormModel();
        when(service.getTextareaFormModel()).thenReturn(initialModel);

        var result = context.openPage(TextareaPage.class);
        var textarea = (TextareaElement) result.getDocument().getElementById("theTextarea");
        var submitButton = result.getDocument().getElementById("submitButton");

        textarea.setValue(""); // leer lassen
        submitButton.click();

        var li = result.getDocument()
                .getElementsByTagName("li");
        assertThat(li.length).isEqualTo(1);
        assertThat((Element) li.item(0))
                .extracting(Element::getTextContent)
                .isEqualTo("Benutzerdefinierte globale Pflichtfeldmeldung");

        assertThat(result.getDocument().getElementById("fieldMessage").getInnerText()).isEqualTo("Benutzerdefinierte Pflichtfeldmeldung");
    }

    @Test
    void incompatibleValueMessageIsShown() {
        var initialModel = new TextareaFormModel();
        when(service.getTextareaFormModel()).thenReturn(initialModel);

        var result = context.openPage(TextareaPage.class);
        var textarea = (TextareaElement) result.getDocument().getElementById("theTextarea");
        var submitButton = result.getDocument().getElementById("submitButton");

        textarea.setValue("\u0000\u0001"); // ungültige Eingabe
        submitButton.click();

        var fieldMessage = result.getDocument()
                .getElementById("theTextarea")
                .getParentElement()
                .querySelector("div")
                .getTextContent();

        assertThat(fieldMessage).contains("Benutzerdefinierte globale Pflichtfeldmeldung");
    }

    @Test
    void globalValidationMessagesAreShown() {
        var initialModel = new TextareaFormModel();
        when(service.getTextareaFormModel()).thenReturn(initialModel);

        var result = context.openPage(TextareaPage.class);
        var textarea = (TextareaElement) result.getDocument().getElementById("theTextarea");
        var submitButton = result.getDocument().getElementById("submitButton");
        textarea.setValue(""); // leer lassen

        submitButton.click();

        var globalMessages = result.getDocument().querySelectorAll("li");
        assertThat(globalMessages)
                .anyMatch(li -> li.getTextContent().contains("Benutzerdefinierte globale Pflichtfeldmeldung"));
    }
}