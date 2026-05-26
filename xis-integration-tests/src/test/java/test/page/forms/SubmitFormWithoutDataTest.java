package test.page.forms;


import one.xis.context.IntegrationTestContext;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SubmitFormWithoutDataTest {

    private IntegrationTestContext testContext;

    @BeforeAll
    void setUp() {
        testContext = IntegrationTestContext.builder()
                .withSingleton(SubmitFormWithoutData.class)
                .build();
    }

    @BeforeEach
    void resetPage() {
        testContext.getSingleton(SubmitFormWithoutData.class).reset();
    }

    @Test
    void actionReceivesFormDataObjectWithoutFormDataInitializerWhenSubmittedEmpty() {
        var client = testContext.openPage("/submitFormWithoutData.html");
        var document = client.getDocument();

        document.getElementById("save").click();

        var page = testContext.getSingleton(SubmitFormWithoutData.class);
        assertThat(page.isInvoked()).isTrue();
        assertThat(page.getSubmittedObject()).isNotNull();
        assertThat(page.getSubmittedObject().getProperty1()).isNull();
        assertThat(page.getSubmittedObject().getProperty2()).isNull();
    }

    @Test
    void actionReceivesFormDataObjectWithoutFormDataInitializerWhenSubmittedWithValues() {
        var client = testContext.openPage("/submitFormWithoutData.html");
        var document = client.getDocument();

        document.getInputElementById("field1").setValue("first");
        document.getInputElementById("field2").setValue("second");
        document.getElementById("save").click();

        var page = testContext.getSingleton(SubmitFormWithoutData.class);
        assertThat(page.isInvoked()).isTrue();
        assertThat(page.getSubmittedObject()).isNotNull();
        assertThat(page.getSubmittedObject().getProperty1()).isEqualTo("first");
        assertThat(page.getSubmittedObject().getProperty2()).isEqualTo("second");
    }
}
