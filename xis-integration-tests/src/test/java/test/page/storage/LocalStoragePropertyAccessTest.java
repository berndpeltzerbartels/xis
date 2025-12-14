package test.page.storage;

import one.xis.context.IntegrationTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LocalStoragePropertyAccessTest {

    private IntegrationTestContext testContext;

    @BeforeEach
    void init() {
        testContext = IntegrationTestContext.builder()
                .withSingleton(LocalStoragePropertyAccessPage.class)
                .build();
    }

    @Test
    void testLocalStorageDarkModeProperty() {
        // Set darkMode in localStorage before opening the page
        testContext.getLocalStorage().setItem("darkMode", "{\"value\":\"true\"}");

        var result = testContext.openPage(LocalStoragePropertyAccessPage.class);

        // Check if the darkMode div is displayed
        var darkModeDiv = result.getDocument().getElementById("darkModeDisplay");
        assertThat(darkModeDiv).isNotNull();
        assertThat(darkModeDiv.getInnerText()).contains("Dark mode is active");
    }

    @Test
    void testLocalStorageThemeProperty() {
        // Set theme in localStorage before opening the page
        testContext.getLocalStorage().setItem("theme", "{\"value\":\"dark\"}");

        var result = testContext.openPage(LocalStoragePropertyAccessPage.class);

        // Check if theme is displayed
        var themeDiv = result.getDocument().getElementById("themeDisplay");
        assertThat(themeDiv).isNotNull();
        assertThat(themeDiv.getInnerText()).contains("Theme: dark");
    }

    @Test
    void testLocalStorageNestedUserProperty() {
        // Set user object in localStorage with nested properties before opening the page
        testContext.getLocalStorage().setItem("user", "{\"value\":{\"name\":\"John\",\"age\":30}}");

        var result = testContext.openPage(LocalStoragePropertyAccessPage.class);

        // Check if user name is displayed
        var userNameDiv = result.getDocument().getElementById("userNameDisplay");
        assertThat(userNameDiv).isNotNull();
        assertThat(userNameDiv.getInnerText()).contains("User: John");

        // Check if user age is displayed
        var userAgeDiv = result.getDocument().getElementById("userAgeDisplay");
        assertThat(userAgeDiv).isNotNull();
        assertThat(userAgeDiv.getInnerText()).contains("Age: 30");
    }

    @Test
    void testLocalStorageDarkModeNotSet() {
        // Don't set darkMode - div should not be displayed
        testContext.getLocalStorage().reset();
        var result = testContext.openPage(LocalStoragePropertyAccessPage.class);

        var darkModeDiv = result.getDocument().getElementById("darkModeDisplay");
        // The div should not exist when condition is false
        assertThat(darkModeDiv).isNull();
    }
}
