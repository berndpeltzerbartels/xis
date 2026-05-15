package test.page.storage;

import one.xis.context.IntegrationTestContext;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SessionStorageSameRequestTest {

    @Test
    void sameStorageKeyUsesSameInstanceWithinOneRequest() {
        var context = IntegrationTestContext.builder()
                .withSingleton(SessionStorageSameRequestPage.class)
                .build();

        var client = context.openPage(SessionStorageSameRequestPage.class);

        assertThat(client.getDocument().getElementById("value-from-same-request").getInnerText()).isEqualTo("42");
        assertThat(client.getSessionStorage().getItem("data")).contains("\"id\":42");
    }
}
