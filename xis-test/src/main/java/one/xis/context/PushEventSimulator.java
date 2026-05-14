package one.xis.context;

/**
 * Optional interface that a {@link TestScriptExtension} can implement to
 * support simulating server-push events in integration tests.
 */
public interface PushEventSimulator {

    /**
     * Simulates a server-push update-event.
     *
     * @param updateEventKey the event key, e.g. "gameUpdated"
     */
    void simulatePushEvent(String updateEventKey);
}
