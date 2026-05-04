package test.page.forms.textarea;

import one.xis.context.IntegrationTestContext;
import one.xis.test.dom.TextareaElement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
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

        var client = context.openPage(TextareaPage.class);
        var textarea = (TextareaElement) client.getDocument().getElementById("theTextarea");
        var submitButton = client.getDocument().getElementById("submitButton");

        textarea.setValue(""); // leer lassen
        submitButton.click();

        var liError = client.getDocument().getElementByTagName("li");
        assertThat(liError.getInnerHTML()).contains("Benutzerdefinierte globale Pflichtfeldmeldung");

        assertThat(client.getDocument().getElementById("fieldMessage").getInnerText())
                .isEqualTo("Benutzerdefinierte Pflichtfeldmeldung");
    }

    @Test
    void textareaValueIsSubmittedToAction() {
        var initialModel = new TextareaFormModel();
        when(service.getTextareaFormModel()).thenReturn(initialModel);

        var client = context.openPage(TextareaPage.class);
        var textarea = (TextareaElement) client.getDocument().getElementById("theTextarea");
        var submitButton = client.getDocument().getElementById("submitButton");

        textarea.setValue("Submitted text");
        submitButton.click();

        var formCaptor = ArgumentCaptor.forClass(TextareaFormModel.class);
        verify(service).saveTextareaFormModel(formCaptor.capture());
        assertThat(formCaptor.getValue().getText()).isEqualTo("Submitted text");
    }

    @Test
    void globalValidationMessagesAreShown() {
        var initialModel = new TextareaFormModel();
        when(service.getTextareaFormModel()).thenReturn(initialModel);

        var client = context.openPage(TextareaPage.class);
        var textarea = (TextareaElement) client.getDocument().getElementById("theTextarea");
        var submitButton = client.getDocument().getElementById("submitButton");
        textarea.setValue(""); // leer lassen

        submitButton.click();

        var liError = client.getDocument().getElementByTagName("li");
        assertThat(liError.getInnerHTML()).contains("Benutzerdefinierte globale Pflichtfeldmeldung");
        verify(service, never()).saveTextareaFormModel(any());
    }
}
