package one.xis.context;

import one.xis.server.RefreshEvent;
import one.xis.server.RefreshEventPublisher;

/**
 * Test implementation of {@link RefreshEventPublisher} for integration tests.
 *
 * <p>Instead of opening a real network transport, this publisher triggers the
 * active JS-side push simulation directly.
 */
public class TestRefreshEventPublisher implements RefreshEventPublisher, PushEventSimulatorAware {

    private PushEventSimulator simulator;

    @Override
    public void setPushEventSimulator(PushEventSimulator simulator) {
        this.simulator = simulator;
    }

    @Override
    public void publish(RefreshEvent refreshEvent) {
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
                            "Build the IntegrationTestContext before publishing refresh events."
            );
        }
    }
}
