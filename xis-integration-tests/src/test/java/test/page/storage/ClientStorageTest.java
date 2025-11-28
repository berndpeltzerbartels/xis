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
        assertThat(page.getInvokedMethods()).containsExactly("data", "linkAction", "data");
        assertThat(page.getClientStoragePageData().getId()).isEqualTo(200);
        assertThat(page.getClientStoragePageData().getValue()).isEqualTo("test2");

        assertThat(result.getDocument().getElementById("clientStorageValue").getInnerText()).isEqualTo("200");

        result.getDocument().getElementById("save-button").click();
        assertThat(page.getInvokedMethods()).containsExactly("data", "linkAction", "data", "formAction", "data");
        assertThat(page.getClientStoragePageData().getId()).isEqualTo(300);
        assertThat(page.getClientStoragePageData().getValue()).isEqualTo("test3");

        assertThat(result.getDocument().getElementById("clientStorageValue").getInnerText()).isEqualTo("300");
    }

}
