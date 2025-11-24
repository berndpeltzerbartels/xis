package test.widget.update;

import one.xis.context.IntegrationTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UpdateEventTest {

    private IntegrationTestContext testContext;

    @BeforeEach
    void init() {
        testContext = IntegrationTestContext.builder()
                .withSingleton(UpdateEventPage.class)
                .withSingleton(UpdateEventWidget1.class)
                .withSingleton(UpdateEventWidget2.class)
                .build();
    }

    @Test
    @DisplayName("Action from Widget1 triggers Widget2 reload via update event")
    void actionFromWidget1TriggersWidget2Reload() {
        var result = testContext.openPage(UpdateEventPage.class);

        // Initial state: both widgets loaded once
        assertThat(result.getDocument().getElementById("widget1-load-count").getInnerText()).isEqualTo("1");
        assertThat(result.getDocument().getElementById("widget2-load-count").getInnerText()).isEqualTo("1");

        // Click action in Widget1 - should emit "widget2-update" event
        result.getDocument().getElementById("widget1-action-link").click();

        // Widget1 should reload (because action was on it)
        assertThat(result.getDocument().getElementById("widget1-load-count").getInnerText()).isEqualTo("2");
        
        // Widget2 should also reload (because it listens to "widget2-update" event)
        assertThat(result.getDocument().getElementById("widget2-load-count").getInnerText()).isEqualTo("2");
    }

    @Test
    @DisplayName("Action from Page triggers Widget2 reload via update event, Widget1 stays in container")
    void actionFromPageTriggersWidget2Reload() {
        var result = testContext.openPage(UpdateEventPage.class);

        // Initial state: both widgets loaded once
        assertThat(result.getDocument().getElementById("widget1-load-count").getInnerText()).isEqualTo("1");
        assertThat(result.getDocument().getElementById("widget2-load-count").getInnerText()).isEqualTo("1");

        // Click action on Page - should emit "widget2-update" event
        result.getDocument().getElementById("page-action-link").click();

        // Widget1 should reload (because WidgetResponse targets container1)
        assertThat(result.getDocument().getElementById("widget1-load-count").getInnerText()).isEqualTo("2");
        
        // Widget2 should also reload (because it listens to "widget2-update" event)
        assertThat(result.getDocument().getElementById("widget2-load-count").getInnerText()).isEqualTo("2");
    }
}
