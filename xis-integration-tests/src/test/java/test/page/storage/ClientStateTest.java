package test.page.storage;

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
    void clientStateLinkTest() {
        var client = testContext.openPage(ClientStatePage.class);
        var page = testContext.getAppContext().getSingleton(ClientStatePage.class);

        client.getDocument().getElementById("action-link").click();
        assertThat(page.getStoreData().getItems()).containsExactly("linkAction");

        client.getDocument().getElementById("save-button").click();
        assertThat(page.getStoreData().getItems()).containsExactly("linkAction", "formInput");
    }

}
