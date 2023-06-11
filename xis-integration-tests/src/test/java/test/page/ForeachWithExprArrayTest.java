package test.page;

import one.xis.context.IntegrationTestContext;
import one.xis.test.dom.Element;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ForeachWithExprArrayTest {

    private IntegrationTestContext testContext;

    @BeforeEach
    void init() {
        testContext = IntegrationTestContext.builder()
                .withSingleton(ForeachWithExprArray.class)
                .build();
    }

    @Test
    void test() {
        var result = testContext.openPage(ForeachWithExprArray.class);

        var div1 = result.getDocument().getElementById("list1");
        var div2 = result.getDocument().getElementById("list2");
        var list1 = div1.getDescendantElementsByClassName("item1");
        var list2 = div2.getDescendantElementsByClassName("item2");

        assertThat(list1.stream().map(Element::getTextContent)).containsExactly("1", "2", "3");
        assertThat(list2.stream().map(Element::getTextContent)).containsExactly("4", "5", "6");
    }

}
