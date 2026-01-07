package test.reactive.buttonlocalstorage;

import one.xis.context.IntegrationTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import test.page.storage.ButtonActionLocalStoragePage;

import static org.assertj.core.api.Assertions.assertThat;


@Disabled
class ButtonActionLocalStorageTest {

    private IntegrationTestContext testContext;

    @BeforeEach
    void setUp() {
        testContext = IntegrationTestContext.builder()
                .withSingleton(ButtonActionLocalStoragePage.class)
                .build();
    }

    @Test
    void standaloneButtonLocalStorageActionsWork() {
        // Open the page
        var result = testContext.openPage(ButtonActionLocalStoragePage.class);

        // Debug: Was steht wirklich im HTML?
        var counterElement = result.getDocument().getElementById("counter-value");

        // Click increment button
        var incrementButton = result.getDocument().getElementById("increment-standalone");
        incrementButton.click();

        // Debug: Was passiert nach dem Klick?
        counterElement = result.getDocument().getElementById("counter-value");

        // Check that the action worked using the new parsed methods
        assertThat(counterElement.getInnerText()).isNotNull();
        assertThat(result.getLocalStorage().getItem("counter")).isNotNull();

        // Test the new convenient API
        Integer counterValue = result.getLocalStorage().getIntValue("counter");
        assertThat(counterValue).isNotNull();
        System.out.println("Parsed counter value: " + counterValue);
    }

    @Test
    void localStorageButtonsHaveCorrectType() {
        var result = testContext.openPage(ButtonActionLocalStoragePage.class);

        // Check that buttons have correct type attribute
        var incrementButton = result.getDocument().getElementById("increment-standalone");
        var decrementButton = result.getDocument().getElementById("decrement-standalone");

        // Both should be type="button" to prevent form submission
        assertThat(incrementButton.getAttribute("type")).isEqualTo("button");
        assertThat(decrementButton.getAttribute("type")).isEqualTo("button");

        // Verify they have xis:action attributes
        assertThat(incrementButton.hasAttribute("xis:action")).isTrue();
        assertThat(decrementButton.hasAttribute("xis:action")).isTrue();

        assertThat(incrementButton.getAttribute("xis:action")).isEqualTo("increment-standalone");
        assertThat(decrementButton.getAttribute("xis:action")).isEqualTo("decrement-standalone");
    }

    @Test
    void localStorageReactiveUpdatesWork() {
        var result = testContext.openPage(ButtonActionLocalStoragePage.class);

        // Test that reactive updates work with localStorage
        var counterElement = result.getDocument().getElementById("counter-value");

        // Initial state
        assertThat(counterElement.getInnerText()).isEqualTo("0");

        // Multiple clicks should trigger reactive updates
        var incrementButton = result.getDocument().getElementById("increment-standalone");

        for (int i = 1; i <= 5; i++) {
            incrementButton.click();

            // Each click should update both localStorage and the reactive display
            assertThat(counterElement.getInnerText()).isEqualTo(String.valueOf(i));

            // Use the new convenient API to check localStorage values
            Integer localStorageValue = result.getLocalStorage().getIntValue("counter");
            assertThat(localStorageValue).isEqualTo(i);
        }

        // Test decrement as well
        var decrementButton = result.getDocument().getElementById("decrement-standalone");

        for (int i = 4; i >= 0; i--) {
            decrementButton.click();

            assertThat(counterElement.getInnerText()).isEqualTo(String.valueOf(i));

            // Use the convenient API for localStorage assertions
            Integer localStorageValue = result.getLocalStorage().getIntValue("counter");
            assertThat(localStorageValue).isEqualTo(i);
        }
    }
}