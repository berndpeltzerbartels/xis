package test.page.core;

import one.xis.context.IntegrationTestContext;
import one.xis.test.dom.Element;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ForeachAttributePageTest {

    private IntegrationTestContext testContext;

    @BeforeEach
    void init() {
        testContext = IntegrationTestContext.builder()
                .withSingleton(ForeachAttributePage.class)
                .build();
    }

    @Test
    @DisplayName("foreach-attribute with simple data")
    void test() {
        var client = testContext.openPage("/foreachAttribute.html");
        var items = client.getDocument().getElementsByClass("item")
                .stream().map(Element::getInnerText).toList();

        assertThat(items).containsExactly("Item1", "Item2", "Item3");
    }
}
