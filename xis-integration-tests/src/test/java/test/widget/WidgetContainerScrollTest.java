package test.widget;

import one.xis.context.IntegrationTestContext;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WidgetContainerScrollTest {

    @Test
    void widgetContainerWithScrollToTop() {
        var context = IntegrationTestContext.builder()
                .withSingleton(ScrollTestPage.class)
                .withSingleton(ScrollTestWidget.class)
                .withSingleton(ScrollTestWidget2.class)
                .build();

        var result = context.openPage(ScrollTestPage.class);
        
        // Initially no scroll happened (default widget loads without scroll on page open)
        assertThat(result.getWindow().getScrollToCallCount()).isEqualTo(0);
        
        // Click link to load DIFFERENT widget in container with scroll-to-top
        result.getDocument().getElementById("load-widget2").click();
        
        // Verify scroll was called when switching to different widget
        assertThat(result.getWindow().getScrollToCallCount()).isEqualTo(1);
        assertThat(result.getWindow().getScrollX()).isEqualTo(0);
        assertThat(result.getWindow().getScrollY()).isEqualTo(0);
    }

    @Test
    void widgetContainerWithoutScrollToTop() {
        var context = IntegrationTestContext.builder()
                .withSingleton(NoScrollTestPage.class)
                .withSingleton(ScrollTestWidget.class)
                .build();

        var result = context.openPage(NoScrollTestPage.class);
        
        // Click link to load widget in container WITHOUT scroll-to-top
        result.getDocument().getElementById("load-widget").click();
        
        // Verify scroll was NOT called
        assertThat(result.getWindow().getScrollToCallCount()).isEqualTo(0);
    }
}
