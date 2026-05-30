package test.page.theme;

import one.xis.context.IntegrationTestContext;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ThemeDependencyIntegrationTest {

    @Test
    void themeTagsAreAvailableWhenThemeDependencyIsPresent() {
        var context = IntegrationTestContext.builder()
                .withSingleton(ThemeDependencyPage.class)
                .build();

        var document = context.openPage("/theme-dependency.html").getDocument();

        assertThat(document.getInputElementById("name").getValue()).isEqualTo("Ada");
        assertThat(document.querySelector("label[for='name']").getInnerText()).isEqualTo("Name");
        assertThat(document.getElementByTagName("form")).isNotNull();
        assertThat(document.getElementByTagName("button").getInnerText()).isEqualTo("Save");
    }
}
