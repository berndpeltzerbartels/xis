package test.page.core;

import one.xis.context.IntegrationTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class MultipleAttributesPageTest {


    private IntegrationTestContext testContext;

    @BeforeEach
    void init() {
        testContext = IntegrationTestContext.builder()
                .withSingleton(MultipleAttributesPage.class)
                .build();
    }

    @Test
    void test() {
        var client = testContext.openPage(MultipleAttributesPage.class);
        var page = testContext.getAppContext().getSingleton(MultipleAttributesPage.class);

        var actionLink1 = client.getDocument().getElementById("id1");
        var actionLink2 = client.getDocument().getElementById("id2");
        var actionLink3 = client.getDocument().getElementById("id3");

        assertThat(actionLink1).isNotNull();
        assertThat(actionLink2).isNotNull();
        assertThat(actionLink3).isNotNull();

        assertThat(page.getInvokedActions()).isEmpty();
        actionLink1.click();
        assertThat(page.getInvokedActions()).containsExactly("action1");
        actionLink2.click();
        assertThat(page.getInvokedActions()).containsExactly("action1", "action2");
        actionLink3.click();
        assertThat(page.getInvokedActions()).containsExactly("action1", "action2", "action3");

    }

}
