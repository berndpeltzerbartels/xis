package test.page.forms;


import one.xis.context.IntegrationTestContext;
import org.junit.jupiter.api.BeforeAll;
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

    @Test
    void testEmptyFormShowsValidationErrors() {
        // Seite öffnen
        var result = testContext.openPage("/submitFormWithoutData.html");
        var document = result.getDocument();

        document.getElementById("save").click();

        assertThat(testContext.getSingleton(SubmitFormWithoutData.class).isInvoked()).isTrue();
    }
}
