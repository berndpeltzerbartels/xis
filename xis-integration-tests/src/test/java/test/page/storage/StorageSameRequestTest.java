package test.page.storage;

import one.xis.context.IntegrationTestContext;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StorageSameRequestTest {

    @Test
    void localStorageKeyUsesSameInstanceWithinOneRequest() {
        var context = IntegrationTestContext.builder()
                .withSingleton(LocalStorageSameRequestPage.class)
                .build();

        var client = context.openPage(LocalStorageSameRequestPage.class);

        assertThat(client.getDocument().getElementById("value-from-same-request").getInnerText()).isEqualTo("43");
        assertThat(client.getLocalStorage().getItem("data")).contains("\"id\":43");
    }

    @Test
    void clientStorageKeyUsesSameInstanceWithinOneRequest() {
        var context = IntegrationTestContext.builder()
                .withSingleton(ClientStorageSameRequestPage.class)
                .build();

        var client = context.openPage(ClientStorageSameRequestPage.class);

        assertThat(client.getDocument().getElementById("value-from-same-request").getInnerText()).isEqualTo("44");
    }
}
