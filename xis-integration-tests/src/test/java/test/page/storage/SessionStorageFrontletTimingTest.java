package test.page.storage;

import one.xis.context.IntegrationTestContext;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SessionStorageFrontletTimingTest {

    @Test
    void frontletLoadedDuringPageRenderingUsesSessionStorageFromPageResponse() {
        var context = IntegrationTestContext.builder()
                .withSingleton(SessionStorageFrontletTimingPage.class)
                .withSingleton(SessionStorageFrontletTimingFrontlet.class)
                .build();

        var client = context.openPage(SessionStorageFrontletTimingPage.class);

        assertThat(client.getDocument().getElementById("frontlet-value").getInnerText()).isEqualTo("74");
        assertThat(client.getSessionStorage().getItem("data")).contains("\"id\":74");
    }
}
