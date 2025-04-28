package test.page.core;

import one.xis.context.IntegrationTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ClientStateTest {


    private IntegrationTestContext testContext;

    @BeforeEach
    void init() {
        testContext = IntegrationTestContext.builder()
                .withSingleton(ClientStatePage.class)
                .build();
    }

    @Test
    void clientStateTest() {
        var result = testContext.openPage(ClientStatePage.class);
        var page = testContext.getAppContext().getSingleton(ClientStatePage.class);

        assertThat(page.getInvokateddMethods()).containsExactly("data");

        result.getDocument().getElementById("action-link").click();
        assertThat(page.getInvokateddMethods()).containsExactly("data", "linkAction");
        assertThat(page.getPageData().getId()).isEqualTo(200);
        assertThat(page.getPageData().getValue()).isEqualTo("test2");
    }

}
