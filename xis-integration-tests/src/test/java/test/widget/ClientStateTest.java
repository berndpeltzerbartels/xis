package test.widget;

import one.xis.context.IntegrationTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ClientStateTest {


    private IntegrationTestContext testContext;
    private ClientStatePage page;

    @BeforeEach
    void init() {
        testContext = IntegrationTestContext.builder()
                .withSingleton(ClientStatePage.class)
                .build();
    }

    // TODO test für liste mit foreach
    @Test
    void clientStateLinkTest() {
        var result = testContext.openPage(ClientStatePage.class);
        var page = testContext.getAppContext().getSingleton(ClientStatePage.class);

        result.getDocument().getElementById("action-link").click();
        assertThat(page.getInvokedMethods()).containsExactly("data", "linkAction", "data");
        assertThat(page.getClientStatePageData().getId()).isEqualTo(200);
        assertThat(page.getClientStatePageData().getValue()).isEqualTo("test2");
        assertThat(result.getSessionStorage().getItem("data")).contains("\"id\":200");
        assertThat(result.getSessionStorage().getItem("data")).contains("\"value\":\"test2\"");

        assertThat(result.getDocument().getElementById("clientStateValue").getInnerText()).isEqualTo("200");

        result.getDocument().getElementById("save-button").click();
        assertThat(page.getInvokedMethods()).containsExactly("data", "linkAction", "data", "formAction", "data");
        assertThat(page.getClientStatePageData().getId()).isEqualTo(300);
        assertThat(page.getClientStatePageData().getValue()).isEqualTo("test3");
        assertThat(result.getSessionStorage().getItem("data")).contains("\"id\":300");
        assertThat(result.getSessionStorage().getItem("data")).contains("\"value\":\"test3\"");


        assertThat(result.getDocument().getElementById("clientStateValue").getInnerText()).isEqualTo("300");
    }

}
