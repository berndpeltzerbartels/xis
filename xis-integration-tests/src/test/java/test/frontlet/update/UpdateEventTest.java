package test.frontlet.update;

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
                .withSingleton(UpdateEventFrontlet1.class)
                .withSingleton(UpdateEventFrontlet2.class)
                .build();
    }

    @Test
    @DisplayName("Action from Frontlet1 triggers Frontlet2 reload via update event")
    void actionFromFrontlet1TriggersFrontlet2Reload() {
        var client = testContext.openPage(UpdateEventPage.class);

        // Initial state: both frontlets loaded once
        assertThat(client.getDocument().getElementById("frontlet1-load-count").getInnerText()).isEqualTo("1");
        assertThat(client.getDocument().getElementById("frontlet2-load-count").getInnerText()).isEqualTo("1");

        // Click action in Frontlet1 - should emit "frontlet2-update" event
        client.getDocument().getElementById("frontlet1-action-link").click();

        // Frontlet1 should reload (because action was on it)
        assertThat(client.getDocument().getElementById("frontlet1-load-count").getInnerText()).isEqualTo("2");
        
        // Frontlet2 should also reload (because it listens to "frontlet2-update" event)
        assertThat(client.getDocument().getElementById("frontlet2-load-count").getInnerText()).isEqualTo("2");
    }

    @Test
    @DisplayName("Action from Page triggers Frontlet2 reload via update event, Frontlet1 stays in container")
    void actionFromPageTriggersFrontlet2Reload() {
        var client = testContext.openPage(UpdateEventPage.class);

        // Initial state: both frontlets loaded once
        assertThat(client.getDocument().getElementById("frontlet1-load-count").getInnerText()).isEqualTo("1");
        assertThat(client.getDocument().getElementById("frontlet2-load-count").getInnerText()).isEqualTo("1");

        // Click action on Page - should emit "frontlet2-update" event
        client.getDocument().getElementById("page-action-link").click();

        // Frontlet1 should stay unchanged; the page action only emits the update event.
        assertThat(client.getDocument().getElementById("frontlet1-load-count").getInnerText()).isEqualTo("1");
        
        // Frontlet2 should also reload (because it listens to "frontlet2-update" event)
        assertThat(client.getDocument().getElementById("frontlet2-load-count").getInnerText()).isEqualTo("2");
    }
}
