package test.page.core;

import one.xis.context.IntegrationTestContext;
import one.xis.test.dom.Element;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ForeachTagPageTest {

    private IntegrationTestContext testContext;

    @BeforeEach
    void init() {
        testContext = IntegrationTestContext.builder()
                .withSingleton(ForeachTagPage.class)
                .build();
    }

    @Test
    @DisplayName("foreach-tag with simple data")
    void test() {
        var result = testContext.openPage("/foreachTag.html");
        
        // Debug: print document to see what's happening
        System.out.println("=== DOCUMENT HTML ===");
        System.out.println(result.getDocument().asString());
        System.out.println("=== END DOCUMENT ===");
        
        var items = result.getDocument().getElementsByClass("item")
                .stream().map(Element::getInnerText).toList();

        assertThat(items).containsExactly("Item1", "Item2", "Item3");
    }
}
