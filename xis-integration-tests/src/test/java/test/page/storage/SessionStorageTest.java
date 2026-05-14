package test.page.storage;

import one.xis.context.IntegrationTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Disabled
class SessionStorageTest {


    private IntegrationTestContext testContext;

    @BeforeEach
    void init() {
        testContext = IntegrationTestContext.builder()
                .withSingleton(SessionStoragePage.class)
                .build();
    }

    // TODO test für liste mit foreach
    @Test
    void sessionStorageLinkTest() {
        var client = testContext.openPage(SessionStoragePage.class);
        var page = testContext.getAppContext().getSingleton(SessionStoragePage.class);

        client.getDocument().getElementById("action-link").click();
        assertThat(page.getInvokedMethods()).containsExactly("data", "linkAction", "data");
        assertThat(page.getSessionStoragePageData().getId()).isEqualTo(200);
        assertThat(page.getSessionStoragePageData().getValue()).isEqualTo("test2");
        assertThat(client.getSessionStorage().getItem("data")).contains("\"id\":200");
        assertThat(client.getSessionStorage().getItem("data")).contains("\"value\":\"test2\"");

        assertThat(client.getDocument().getElementById("sessionStorageValue").getInnerText()).isEqualTo("200");

        client.getDocument().getElementById("save-button").click();
        assertThat(page.getInvokedMethods()).containsExactly("data", "linkAction", "data", "formAction", "data");
        assertThat(page.getSessionStoragePageData().getId()).isEqualTo(300);
        assertThat(page.getSessionStoragePageData().getValue()).isEqualTo("test3");
        assertThat(client.getSessionStorage().getItem("data")).contains("\"id\":300");
        assertThat(client.getSessionStorage().getItem("data")).contains("\"value\":\"test3\"");


        assertThat(client.getDocument().getElementById("sessionStorageValue").getInnerText()).isEqualTo("300");
    }

}
