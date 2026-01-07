package test.page.storage;

import one.xis.context.IntegrationTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ClientStorageTest {


    private IntegrationTestContext testContext;

    @BeforeEach
    void init() {
        testContext = IntegrationTestContext.builder()
                .withSingleton(ClientStoragePage.class)
                .build();
    }

    @Test
    void clientStorageLinkTest() {
        var result = testContext.openPage(ClientStoragePage.class);
        var page = testContext.getAppContext().getSingleton(ClientStoragePage.class);

        result.getDocument().getElementById("action-link").click();
        assertThat(page.getStoreData().getItems()).containsExactly("linkAction");

        result.getDocument().getElementById("save-button").click();
        assertThat(page.getStoreData().getItems()).containsExactly("linkAction", "formInput");
    }

}
