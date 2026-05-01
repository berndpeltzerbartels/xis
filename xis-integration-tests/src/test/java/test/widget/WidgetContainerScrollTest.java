package test.widget;

import one.xis.context.IntegrationTestContext;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WidgetContainerScrollTest {

    @Test
    void widgetContainerWithScrollToTop() {
        var context = IntegrationTestContext.builder()
                .withSingleton(ScrollTestPage.class)
                .withSingleton(ScrollTestFrontlet.class)
                .withSingleton(ScrollTestWidget2.class)
                .build();

        var client = context.openPage(ScrollTestPage.class);
        
        // Initially no scroll happened (default widget loads without scroll on page open)
        assertThat(client.getWindow().getScrollToCallCount()).isEqualTo(0);
        
        // Click link to load DIFFERENT widget in container with scroll-to-top
        client.getDocument().getElementById("load-widget2").click();
        
        // Verify scroll was called when switching to different widget
        assertThat(client.getWindow().getScrollToCallCount()).isEqualTo(1);
        assertThat(client.getWindow().getScrollX()).isEqualTo(0);
        assertThat(client.getWindow().getScrollY()).isEqualTo(0);
    }

    @Test
    void widgetContainerWithoutScrollToTop() {
        var context = IntegrationTestContext.builder()
                .withSingleton(NoScrollTestPage.class)
                .withSingleton(ScrollTestFrontlet.class)
                .build();

        var client = context.openPage(NoScrollTestPage.class);
        
        // Click link to load widget in container WITHOUT scroll-to-top
        client.getDocument().getElementById("load-widget").click();
        
        // Verify scroll was NOT called
        assertThat(client.getWindow().getScrollToCallCount()).isEqualTo(0);
    }
}
