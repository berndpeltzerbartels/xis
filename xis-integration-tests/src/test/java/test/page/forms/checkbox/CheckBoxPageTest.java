package test.page.forms.checkbox;

import one.xis.context.IntegrationTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class CheckBoxPageTest {

    private IntegrationTestContext context;
    private CheckBoxFormService checkBoxFormService;

    @BeforeEach
    void init() {
        checkBoxFormService = mock(CheckBoxFormService.class);
        context = IntegrationTestContext.builder()
                .withSingleton(CheckBoxPage.class)
                .withSingleton(checkBoxFormService)
                .build();
    }

    @Test
    void checkTheBox() {
        // Initial state: checkbox is not checked
        var initialModel = new CheckBoxFormModel();
        initialModel.setAccepted(false);
        when(checkBoxFormService.getCheckBoxFormModel()).thenReturn(initialModel);

        var result = context.openPage(CheckBoxPage.class);
        var checkbox = result.getDocument().getInputElementById("theCheckbox");
        var submitButton = result.getDocument().getElementById("submitButton");

        assertThat(checkbox.isChecked()).isFalse();

        // User checks the box and submits
        checkbox.click();
        assertThat(checkbox.isChecked()).isTrue();

        submitButton.click();

        //

        // Verify the saved state
        var formModelCaptor = ArgumentCaptor.forClass(CheckBoxFormModel.class);
        verify(checkBoxFormService).saveCheckBoxFormModel(formModelCaptor.capture());

        assertThat(formModelCaptor.getValue().isAccepted()).isTrue();
    }

    @Test
    void uncheckTheBox() {
        // Initial state: checkbox is checked
        var initialModel = new CheckBoxFormModel();
        initialModel.setAccepted(true);
        when(checkBoxFormService.getCheckBoxFormModel()).thenReturn(initialModel);

        var result = context.openPage(CheckBoxPage.class);
        var checkbox = result.getDocument().getInputElementById("theCheckbox");
        var submitButton = result.getDocument().getElementById("submitButton");

        assertThat(checkbox.isChecked()).isTrue();

        // User unchecks the box and submits
        checkbox.click();
        assertThat(checkbox.isChecked()).isFalse();

        submitButton.click();

        // Verify the saved state
        var formModelCaptor = ArgumentCaptor.forClass(CheckBoxFormModel.class);
        verify(checkBoxFormService).saveCheckBoxFormModel(formModelCaptor.capture());

        assertThat(formModelCaptor.getValue().isAccepted()).isFalse();
    }
}