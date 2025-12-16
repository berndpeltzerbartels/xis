package test.page.core.navigation;

import one.xis.context.IntegrationTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NavigationPageTest {

    private IntegrationTestContext testContext;

    @BeforeEach
    void init() {
        testContext = IntegrationTestContext.builder()
                .withSingleton(NavigationPage1.class)
                .withSingleton(NavigationPage2.class)
                .withSingleton(NavigationPage3.class)
                .build();
    }

    @Test
    @DisplayName("Return value of the action is a page class.")
    void simpleNavigationAction() {
        var result = testContext.openPage("/page1.html");
        var document = result.getDocument();
        document.getElementById("action-link1").click();

        assertThat(document.getElementByTagName("title").getInnerText()).isEqualTo("Page 3");
    }


    @Test
    @DisplayName("Action is triggered by form submission  and return value is another page's class.")
    void formAction() {
        var result = testContext.openPage("/page1.html");
        var document = result.getDocument();
        document.getElementById("action-link2").click();

        assertThat(document.getElementByTagName("title").getInnerText()).isEqualTo("Page 3");
    }

    @Test
    @DisplayName("Return value of the action is a url with path-variable and parameter.")
    void urlAction() {
        var result = testContext.openPage("/page1.html");
        var document = result.getDocument();
        document.getElementById("action-link3").click();

        assertThat(document.getElementByTagName("title").getInnerText()).isEqualTo("Page 2");
        assertThat(document.getElementById("pathVariable").getInnerText()).isEqualTo("xyz");
        assertThat(document.getElementById("queryParameter").getInnerText()).isEqualTo("123");
    }
}
