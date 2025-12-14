package test.page.forms;

import one.xis.context.IntegrationTestContext;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SimpleRecordFormTest {

    private IntegrationTestContext testContext;

    @BeforeAll
    void init() {
        testContext = IntegrationTestContext.builder()
                .withSingleton(SimpleRecordForm.class)
                .build();
    }

    @Test
    void testRecordAsFormData() {
        // Open page - should display form with initial record values
        var result = testContext.openPage("/simpleRecordForm/new.html");
        var document = result.getDocument();


        var firstNameInput = document.getInputElementById("firstName");
        var lastNameInput = document.getInputElementById("lastName");

        assertThat(firstNameInput.getValue()).isEqualTo("John");
        assertThat(lastNameInput.getValue()).isEqualTo("Doe");

        // Modify form values
        firstNameInput.setValue("Jane");
        lastNameInput.setValue("Smith");

        // Submit form - should trigger @Action with @FormData parameter
        document.getElementById("saveButton").click();

        // Verify saved record was received correctly in action method
        var savedFirstName = document.getElementById("savedFirstName");
        var savedLastName = document.getElementById("savedLastName");
        var savedTitle = document.getElementById("savedTitle");

        assertThat(savedFirstName.getInnerText()).isEqualTo("Jane");
        assertThat(savedLastName.getInnerText()).isEqualTo("Smith");
    }
}
