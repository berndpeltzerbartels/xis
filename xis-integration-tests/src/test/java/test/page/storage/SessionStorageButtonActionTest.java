package test.page.storage;

import one.xis.context.IntegrationTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


@Disabled
class SessionStorageButtonActionTest {

    private IntegrationTestContext context;

    @BeforeEach
    void setUp() {
        context = IntegrationTestContext.builder()
                .withSingleton(SessionStorageButtonActionTestPage.class)
                .build();
    }

    @Test
    void standaloneButtonActionsWork() {
        // Open the test page
        var result = context.openPage(SessionStorageButtonActionTestPage.class);

        // Check initial state
        var counterSpan = result.getDocument().getElementById("counter-value");
        assertThat(counterSpan.getInnerText()).isEqualTo("0");

        // Click increment button (should add 5)
        var incrementButton = result.getDocument().getElementById("increment-button");
        incrementButton.click();

        // Verify counter was incremented
        counterSpan = result.getDocument().getElementById("counter-value");
        assertThat(counterSpan.getInnerText()).isEqualTo("5");

        // Click decrement button (should subtract 3)
        var decrementButton = result.getDocument().getElementById("decrement-button");
        decrementButton.click();

        // Verify counter was decremented
        counterSpan = result.getDocument().getElementById("counter-value");
        assertThat(counterSpan.getInnerText()).isEqualTo("2");

        // Click increment again
        incrementButton = result.getDocument().getElementById("increment-button");
        incrementButton.click();

        // Final verification
        counterSpan = result.getDocument().getElementById("counter-value");
        assertThat(counterSpan.getInnerText()).isEqualTo("7");
    }

    @Test
    void standaloneButtonsHaveCorrectType() {
        // Open the test page
        var result = context.openPage(SessionStorageButtonActionTestPage.class);

        // Verify buttons have type="button" to prevent form submission
        var incrementButton = result.getDocument().getElementById("increment-button");
        var decrementButton = result.getDocument().getElementById("decrement-button");

        // The ActionButtonHandler should have set type="button"
        assertThat(incrementButton.getAttribute("type")).isEqualTo("button");
        assertThat(decrementButton.getAttribute("type")).isEqualTo("button");
    }
}