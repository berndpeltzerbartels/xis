package test.page;

import one.xis.context.IntegrationTestContext;
import one.xis.test.dom.Element;
import one.xis.test.dom.TextNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class RepeatAttributePageTest {
    private IntegrationTestContext testContext;

    @BeforeEach
    void init() {
        testContext = IntegrationTestContext.builder()
                .withSingleton(RepeatAttributePage.class)
                .build();
    }

    @Test
    void test() {
        var result = testContext.openPage("/repeatAttr.html");
        var items = result.getDocument().getElementsByClass("item")
                .stream().map(Element::getTextNode).map(TextNode::getNodeValue).collect(Collectors.toList());

        assertThat(items).containsExactly("Item1", "Item2", "Item3");
    }
}
