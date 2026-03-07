package one.xis.ws;

import one.xis.context.PushEventSimulator;
import one.xis.context.PushEventSimulatorAware;

/**
 * Test implementation of {@link RefreshEventPublisher} for use in integration tests.
 * Instead of sending a real WebSocket push, it delegates to the JS-side
 * {@code WebsocketConnectorMock.simulatePushEvent()} via {@link PushEventSimulator}.
 *
 * <p>Usage in tests:
 * <pre>
 *   var context = IntegrationTestContext.builder()
 *       .withSingleton(MyPage.class)
 *       .withSingleton(new TestRefreshEventPublisher())
 *       .build();
 *
 *   // Trigger a push event as the server would via WebSocket:
 *   context.getSingleton(TestRefreshEventPublisher.class)
 *          .publishRefreshEvent(new RefreshEvent("gameUpdated").addClientId("any"));
 *
 *   // Or more conveniently:
 *   context.simulatePushEvent("gameUpdated");
 * </pre>
 */
public class TestRefreshEventPublisher implements RefreshEventPublisher, PushEventSimulatorAware {

    private PushEventSimulator simulator;

    @Override
    public void setPushEventSimulator(PushEventSimulator simulator) {
        this.simulator = simulator;
    }

    /**
     * Triggers the push event in the JS mock.
     *
     * <p><b>Note:</b> {@code clientIds} and {@code userIds} in the {@link RefreshEvent}
     * are ignored in tests – there is always exactly one (virtual) client, so the event
     * always reaches the running page/widget regardless of which IDs are set.
     */
    @Override
    public void publishRefreshEvent(RefreshEvent refreshEvent) {
        assertSimulatorPresent();
        simulator.simulatePushEvent(refreshEvent.getEventKey());
    }

    @Override
    public void publishToAll(String eventKey) {
        assertSimulatorPresent();
        simulator.simulatePushEvent(eventKey);
    }

    private void assertSimulatorPresent() {
        if (simulator == null) {
            throw new IllegalStateException(
                    "TestRefreshEventPublisher: PushEventSimulator not set. " +
                            "Make sure xis-websocket is on the test classpath and " +
                            "IntegrationTestContext has been built before publishing events.");
        }
    }
}
