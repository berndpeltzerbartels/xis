package test.page.forms;


import one.xis.context.IntegrationTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CheckboxListPageTest {

    private IntegrationTestContext context;
    private CheckboxListFormService service;

    @BeforeEach
    void init() {
        service = new CheckboxListFormService();
        context = IntegrationTestContext.builder()
                .withSingleton(CheckboxListPage.class)
                .withSingleton(service)
                .build();
    }

    @Test
    void checkAnItemAndSubmit() {
        // Initial state: one box checked, one not
        var initialModel = new CheckboxListFormModel();
        service.setModel(initialModel);

        var result = context.openPage(CheckboxListPage.class);
        var checkbox1 = result.getDocument().getInputElementById("checkbox-1");
        var checkbox2 = result.getDocument().getInputElementById("checkbox-2");

        // User checks the first box and submits
        checkbox1.click();
        assertThat(checkbox1.isChecked()).isTrue();
        assertThat(checkbox2.isChecked()).isFalse();

        result.getDocument().getElementById("submitButton").click();

        // Verify the state in the service
        var savedModel = service.getModel();
        assertThat(savedModel.getValues()).hasSize(1);
        assertThat(savedModel.getValues().get(0)).isEqualTo(1);

        // Verify the re-rendered page state
        var newCheckbox1 = result.getDocument().getInputElementById("checkbox-1");
        var newCheckbox2 = result.getDocument().getInputElementById("checkbox-2");
        assertThat(newCheckbox1.isChecked()).isTrue();
        assertThat(newCheckbox2.isChecked()).isFalse();
    }


    @Test
    void checkAnItemsAndSubmit() {
        // Initial state: one box checked, one not
        var initialModel = new CheckboxListFormModel();
        service.setModel(initialModel);

        var result = context.openPage(CheckboxListPage.class);
        var checkbox1 = result.getDocument().getInputElementById("checkbox-1");
        var checkbox2 = result.getDocument().getInputElementById("checkbox-2");

        // User checks the first box and submits
        checkbox1.click();
        checkbox2.click();
        assertThat(checkbox1.isChecked()).isTrue();
        assertThat(checkbox2.isChecked()).isTrue();

        result.getDocument().getElementById("submitButton").click();

        // Verify the state in the service
        var savedModel = service.getModel();
        assertThat(savedModel.getValues()).hasSize(2);
        assertThat(savedModel.getValues().get(0)).isEqualTo(1);
        assertThat(savedModel.getValues().get(1)).isEqualTo(2);

        // Verify the re-rendered page state
        var newCheckbox1 = result.getDocument().getInputElementById("checkbox-1");
        var newCheckbox2 = result.getDocument().getInputElementById("checkbox-2");
        assertThat(newCheckbox1.isChecked()).isTrue();
        assertThat(newCheckbox2.isChecked()).isTrue();
    }

    @Test
    void submitWithNoSelection() {
        // Initial state: both boxes checked
        var initialModel = new CheckboxListFormModel();
        service.setModel(initialModel);

        var result = context.openPage(CheckboxListPage.class);
        result.getDocument().getElementById("submitButton").click();

        // Verify the state in the service
        var savedModel = service.getModel();
        assertThat(savedModel.getValues()).isNotNull().isEmpty();
    }
}