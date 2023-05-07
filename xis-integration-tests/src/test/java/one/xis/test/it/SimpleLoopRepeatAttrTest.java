package one.xis.test.it;

import one.xis.context.IntegrationTestContext;
import one.xis.test.dom.Element;
import one.xis.test.dom.TextNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class SimpleLoopRepeatAttrTest {
    private IntegrationTestContext testContext;

    @BeforeEach
    void init() {
        testContext = IntegrationTestContext.builder()
                .withSingleton(SimpleLoopRepeatAttr.class)
                .build();
    }

    @Test
    void test() {
        testContext.openPage("/simpleLoopRepeatAttr.html");
        var items = testContext.getDocument().getElementsByClass("item")
                .stream().map(Element::getTextNode).map(TextNode::getNodeValue).collect(Collectors.toList());

        assertThat(items).containsExactly("Item1", "Item2", "Item3");
    }
}
