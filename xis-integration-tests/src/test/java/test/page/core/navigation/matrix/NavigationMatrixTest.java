package test.page.core.navigation.matrix;

import one.xis.context.IntegrationTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NavigationMatrixTest {

    private IntegrationTestContext context;

    @BeforeEach
    void setUp() {
        context = IntegrationTestContext.builder()
                .withSingleton(new NavigationMatrixService())
                .withSingleton(NavigationMatrixPage.class)
                .withSingleton(NavigationMatrixTargetPage.class)
                .withSingleton(NavigationMatrixDetailPage.class)
                .withSingleton(NavigationMatrixFrontletOne.class)
                .withSingleton(NavigationMatrixFrontletTwo.class)
                .withSingleton(NavigationMatrixParameterizedFrontlet.class)
                .withSingleton(NavigationMatrixSideInitialFrontlet.class)
                .withSingleton(NavigationMatrixSideReplacementFrontlet.class)
                .build();
    }

    @Test
    void pageActionCanNavigateToPageByClassResponseAndString() {
        assertPageClassNavigation("page-class");
        assertDetailNavigation("page-response", "42", "page-response");
        assertDetailNavigation("page-string", "43", "page-string");
    }

    @Test
    void frontletActionCanNavigateToPageByClassAndPageResponse() {
        assertPageClassNavigation("frontlet-page-class");
        assertDetailNavigation("frontlet-page-response", "44", "frontlet-page-response");
    }

    @Test
    void pageActionCanChangeFrontletWhenTargetContainerIsGiven() {
        var client = context.openPage("/navigation/core.html?mode=keep");

        client.getDocument().getElementById("page-frontlet-class").click();

        assertThat(client.getDocument().getElementById("frontlet-two")).isNotNull();
    }

    @Test
    void pageActionCanChangeFrontletWithFrontletResponseAndParameters() {
        var client = context.openPage("/navigation/core.html?mode=keep");

        client.getDocument().getElementById("page-frontlet-response").click();

        assertThat(client.getDocument().getElementById("frontlet-parameterized")).isNotNull();
        assertThat(client.getDocument().getElementById("frontlet-parameterized-message").getInnerText())
                .isEqualTo("from-page-response");
    }

    @Test
    void frontletActionCanChangeFrontletByClassAndFrontletResponse() {
        var client = context.openPage("/navigation/core.html?mode=keep");

        client.getDocument().getElementById("frontlet-class").click();
        assertThat(client.getDocument().getElementById("frontlet-two")).isNotNull();

        client = context.openPage("/navigation/core.html?mode=keep");
        client.getDocument().getElementById("frontlet-response").click();
        assertThat(client.getDocument().getElementById("frontlet-parameterized")).isNotNull();
        assertThat(client.getDocument().getElementById("frontlet-parameterized-message").getInnerText())
                .isEqualTo("from-frontlet-response");
    }

    @Test
    void frontletActionCanChangeFrontletInAnotherContainer() {
        var client = context.openPage("/navigation/core.html?mode=keep");

        client.getDocument().getElementById("frontlet-side-container").click();

        assertThat(client.getDocument().getElementById("frontlet-one")).isNotNull();
        assertThat(client.getDocument().getElementById("side-replacement")).isNotNull();
    }

    @Test
    void voidPageActionKeepsPathVariablesAndQueryParameters() {
        var client = context.openPage("/navigation/core.html?mode=keep");
        var before = client.getDocument().getElementById("page-refresh-count").getInnerText();

        client.getDocument().getElementById("page-void").click();

        assertThat(client.getDocument().getElementById("page-section").getInnerText()).isEqualTo("core");
        assertThat(client.getDocument().getElementById("page-mode").getInnerText()).isEqualTo("keep");
        assertThat(client.getDocument().getElementById("page-refresh-count").getInnerText()).isNotEqualTo(before);
    }

    @Test
    void voidFrontletActionKeepsPagePathQueryAndFrontletParameters() {
        var client = context.openPage("/navigation/core.html?mode=keep");
        var before = client.getDocument().getElementById("frontlet-refresh-count").getInnerText();

        client.getDocument().getElementById("frontlet-void").click();

        assertThat(client.getDocument().getElementById("frontlet-section").getInnerText()).isEqualTo("core");
        assertThat(client.getDocument().getElementById("frontlet-mode").getInnerText()).isEqualTo("keep");
        assertThat(client.getDocument().getElementById("frontlet-message").getInnerText()).isEqualTo("initial");
        assertThat(client.getDocument().getElementById("frontlet-refresh-count").getInnerText()).isNotEqualTo(before);
    }

    private void assertPageClassNavigation(String triggerId) {
        var client = context.openPage("/navigation/core.html?mode=keep");

        client.getDocument().getElementById(triggerId).click();

        assertThat(client.getDocument().getElementByTagName("title").getInnerText()).isEqualTo("Navigation Target");
        assertThat(client.getDocument().getElementById("target-page").getInnerText()).isEqualTo("Target page");
    }

    private void assertDetailNavigation(String triggerId, String id, String mode) {
        var client = context.openPage("/navigation/core.html?mode=keep");

        client.getDocument().getElementById(triggerId).click();

        assertThat(client.getDocument().getElementByTagName("title").getInnerText()).isEqualTo("Navigation Detail");
        assertThat(client.getDocument().getElementById("detail-id").getInnerText()).isEqualTo(id);
        assertThat(client.getDocument().getElementById("detail-mode").getInnerText()).isEqualTo(mode);
    }
}
