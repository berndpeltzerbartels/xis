package one.xis.context;

/**
 * Implemented by classes that want to receive a {@link PushEventSimulator}
 * after the integration-test JS context is ready.
 */
public interface PushEventSimulatorAware {

    void setPushEventSimulator(PushEventSimulator simulator);
}
