package test.widget.selection;

import one.xis.context.IntegrationTestContext;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SelectionIntegrationTest {
    @Test
    void selectionGroupEnsuresExclusiveSelection() {
        var context = IntegrationTestContext.builder()
                .withSingleton(SelectionPage.class)
                .build();
        var result = context.openPage(SelectionPage.class);

        var doc = result.getDocument();
        var ul1 = doc.getElementById("item1");
        var ul2 = doc.getElementById("item2");
        var ul3 = doc.getElementById("item3");

        // At the beginning no item is selected
        assertThat(ul1.getAttribute("class")).doesNotContain("selected");
        assertThat(ul2.getAttribute("class")).doesNotContain("selected");
        assertThat(ul3.getAttribute("class")).doesNotContain("selected");

        ul1.click();
        // After clicking the first item, it is selected
        assertThat(ul1.getAttribute("class")).contains("selected");
        assertThat(ul2.getAttribute("class")).doesNotContain("selected");
        assertThat(ul3.getAttribute("class")).doesNotContain("selected");

        ul1.click();
        // No changes
        assertThat(ul1.getAttribute("class")).contains("selected");
        assertThat(ul2.getAttribute("class")).doesNotContain("selected");
        assertThat(ul3.getAttribute("class")).doesNotContain("selected");

        ul2.click();
        // After clicking the second item, it is selected and the first one is unselected
        assertThat(ul1.getAttribute("class")).doesNotContain("selected");
        assertThat(ul2.getAttribute("class")).contains("selected");
        assertThat(ul3.getAttribute("class")).doesNotContain("selected");
        ul3.click();
        // After clicking the third item, it is selected and the second one is unselected
        assertThat(ul1.getAttribute("class")).doesNotContain("selected");
        assertThat(ul2.getAttribute("class")).doesNotContain("selected");
        assertThat(ul3.getAttribute("class")).contains("selected");

    }
}
