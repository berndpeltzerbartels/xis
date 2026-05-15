package test.page.storage;

import one.xis.context.IntegrationTestContext;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StorageFrontletTimingTest {

    @Test
    void localStorageFromPageResponseIsVisibleToFrontletLoadedDuringRendering() {
        var context = IntegrationTestContext.builder()
                .withSingleton(LocalStorageFrontletTimingPage.class)
                .withSingleton(LocalStorageFrontletTimingFrontlet.class)
                .build();

        var client = context.openPage(LocalStorageFrontletTimingPage.class);

        assertThat(client.getDocument().getElementById("frontlet-value").getInnerText()).isEqualTo("75");
        assertThat(client.getLocalStorage().getItem("data")).contains("\"id\":75");
    }

    @Test
    void clientStorageFromPageResponseIsVisibleToFrontletLoadedDuringRendering() {
        var context = IntegrationTestContext.builder()
                .withSingleton(ClientStorageFrontletTimingPage.class)
                .withSingleton(ClientStorageFrontletTimingFrontlet.class)
                .build();

        var client = context.openPage(ClientStorageFrontletTimingPage.class);

        assertThat(client.getDocument().getElementById("frontlet-value").getInnerText()).isEqualTo("76");
    }
}
