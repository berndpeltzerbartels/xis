package one.xis.ws;

import one.xis.context.IntegrationTestEnvironment;
import one.xis.context.JavascriptFunction;
import one.xis.context.PushEventSimulator;
import one.xis.context.TestScriptExtension;
import one.xis.resource.Resources;

import java.util.Map;

/**
 * Contributes WebSocket JS classes to the integration-test script and implements
 * {@link PushEventSimulator} so {@code TestRefreshEventPublisher} can delegate to it.
 * <p>
 * Registered via META-INF/services/one.xis.context.TestScriptExtension.
 */
public class WsTestScriptExtension implements TestScriptExtension, PushEventSimulator {

    private JavascriptFunction simulatePushEventFn;

    @Override
    public String getAdditionalScript() {
        var resources = new Resources();
        return resources.getByPath("js/WebsocketServerResponse.js").getContent() + "\n"
                + resources.getByPath("js/WebsocketClient.js").getContent() + "\n"
                + resources.getByPath("js/WebsocketConnectorMock.js").getContent() + "\n";
    }

    @Override
    public Map<String, Object> getAdditionalBindings(IntegrationTestEnvironment environment) {
        return Map.of();
    }

    /**
     * Receives the simulatePushEvent JS-function (index 2 from test-main.js array).
     */
    @Override
    public void onScriptReady(JavascriptFunction simulatePushEventFn) {
        this.simulatePushEventFn = simulatePushEventFn;
    }

    @Override
    public void simulatePushEvent(String updateEventKey) {
        if (simulatePushEventFn == null) {
            throw new IllegalStateException(
                    "WsTestScriptExtension: JS context not ready. " +
                            "Call IntegrationTestContext.builder().build() before simulatePushEvent().");
        }
        simulatePushEventFn.execute(updateEventKey);
    }
}
