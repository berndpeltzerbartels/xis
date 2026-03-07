package one.xis.context;

/**
 * Optional interface that a {@link TestScriptExtension} can implement to
 * support simulating server-push events in integration tests.
 * <p>
 * If xis-websocket is on the test classpath, its {@code WsTestScriptExtension}
 * implements this interface. {@code IntegrationTestContext.simulatePushEvent()}
 * delegates to it.
 */
public interface PushEventSimulator {

    /**
     * Simulates a server-push update-event arriving via WebSocket.
     *
     * @param updateEventKey the event key, e.g. "gameUpdated"
     */
    void simulatePushEvent(String updateEventKey);
}
