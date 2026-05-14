package test.frontlet;

import one.xis.context.IntegrationTestContext;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FrontletContainerScrollTest {

    @Test
    void frontletContainerWithScrollToTop() {
        var context = IntegrationTestContext.builder()
                .withSingleton(ScrollTestPage.class)
                .withSingleton(ScrollTestFrontlet.class)
                .withSingleton(ScrollTestFrontlet2.class)
                .build();

        var client = context.openPage(ScrollTestPage.class);
        
        // Initially no scroll happened (default frontlet loads without scroll on page open)
        assertThat(client.getWindow().getScrollToCallCount()).isEqualTo(0);
        
        // Click link to load DIFFERENT frontlet in container with scroll-to-top
        client.getDocument().getElementById("load-frontlet2").click();
        
        // Verify scroll was called when switching to different frontlet
        assertThat(client.getWindow().getScrollToCallCount()).isEqualTo(1);
        assertThat(client.getWindow().getScrollX()).isEqualTo(0);
        assertThat(client.getWindow().getScrollY()).isEqualTo(0);
    }

    @Test
    void frontletContainerWithoutScrollToTop() {
        var context = IntegrationTestContext.builder()
                .withSingleton(NoScrollTestPage.class)
                .withSingleton(ScrollTestFrontlet.class)
                .build();

        var client = context.openPage(NoScrollTestPage.class);
        
        // Click link to load frontlet in container WITHOUT scroll-to-top
        client.getDocument().getElementById("load-frontlet").click();
        
        // Verify scroll was NOT called
        assertThat(client.getWindow().getScrollToCallCount()).isEqualTo(0);
    }
}
