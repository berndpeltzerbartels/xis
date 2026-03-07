package one.xis.context;

/**
 * Implemented by classes that want to receive a {@link PushEventSimulator}
 * after the integration-test JS context is ready.
 * <p>
 * {@code TestRefreshEventPublisher} in xis-websocket implements this interface
 * so it gets wired automatically by {@code IntegrationTestContext}.
 */
public interface PushEventSimulatorAware {

    void setPushEventSimulator(PushEventSimulator simulator);
}
