package one.xis.context;

import java.util.Map;

/**
 * SPI for optional modules (e.g. xis-websocket) to contribute additional
 * JavaScript source code and GraalVM bindings to the integration-test script.
 * <p>
 * Implementations are discovered via {@link java.util.ServiceLoader}.
 * This keeps xis-test free of any compile-time dependency on optional modules.
 */
public interface TestScriptExtension {

    /**
     * Additional JavaScript to prepend to the test script
     * (e.g. WebsocketConnectorMock.js, WebsocketClient.js, …).
     * The returned string is inserted BEFORE test-main.js so that
     * TestApplication can already use the classes defined here.
     */
    String getAdditionalScript();

    /**
     * Additional GraalVM bindings to expose to the JS context
     * (e.g. wsBackendBridge → WsBackendBridge instance).
     *
     * @param environment the current test environment (gives access to BackendBridge etc.)
     */
    Map<String, Object> getAdditionalBindings(IntegrationTestEnvironment environment);

    /**
     * Called once the JS context and invoker are fully initialized.
     * Extensions that need to call back into JS (e.g. to simulate push events)
     * can store the invoker here.
     */
    default void onScriptReady(JavascriptFunction invoker) {
    }
}
