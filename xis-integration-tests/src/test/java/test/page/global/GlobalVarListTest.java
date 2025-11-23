package test.page.global;

import one.xis.context.IntegrationTestContext;
import one.xis.test.dom.ElementImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalVarListTest {

    private IntegrationTestContext testContext;

    @BeforeEach
    void init() {
        testContext = IntegrationTestContext.builder()
                .withSingleton(GlobalVarListPage.class)
                .build();
    }

    @Test
    void globalListRenderingTest() {
        var result = testContext.openPage(GlobalVarListPage.class);

        // Verify that the list is rendered correctly
        var li = result.getDocument().getElementsByTagName("li").stream()
                .map(ElementImpl.class::cast)
                .map(ElementImpl::getInnerText)
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
