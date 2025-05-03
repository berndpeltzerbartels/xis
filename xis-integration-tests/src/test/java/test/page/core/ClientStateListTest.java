package test.page.core;

import one.xis.context.IntegrationTestContext;
import one.xis.test.dom.Element;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ClientStateListTest {

    private IntegrationTestContext testContext;
    private ClientStatePage page;

    @BeforeEach
    void init() {
        testContext = IntegrationTestContext.builder()
                .withSingleton(ClientStateListPage.class)
                .build();
    }

    @Test
    void clientStateListRenderingTest() {
        var result = testContext.openPage(ClientStateListPage.class);

        // Verify that the list is rendered correctly
        var li = result.getDocument().getElementsByTagName("li").stream()
                .map(Element.class::cast)
                .map(Element::getTextContent)
                .toList();
        assertThat(li).hasSize(5);
        assertThat(li)
                .containsExactly(
                        "1 test1",
                        "2 test2",
                        "3 test3",
                        "4 test4",
                        "5 test5"
                );

    }


}
