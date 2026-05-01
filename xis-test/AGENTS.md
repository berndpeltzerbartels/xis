# AGENTS

This module provides the Java-side integration-test harness for XIS.

End-user relevance:
- Potentially useful for end users writing XIS integration tests.
- It is not intended as a production runtime dependency.
- The Gradle plugin currently adds it to `testImplementation`, which matches the intended usage.

Testing direction for push updates:
- Prefer transport-neutral test language such as `simulatePushEvent(...)`.
- Tests should describe the expected DOM refresh behavior, not whether the runtime used WebSocket or SSE.
- Keep the test API stable while the internals stay transport-neutral.

Working rules:
- When changing push-event tests, review `IntegrationTestContext`, `IntegrationTestScript`, `PushEventSimulator`, and JS test helpers together.
- Avoid coupling tests to protocol-specific endpoints or transport-specific mocks.
- Framework infrastructure should be available through the normal XIS test bootstrap; end users should not need to register XIS internals manually via `withSingleton(...)`.
- Use `withSingleton(...)` for application classes, mocks, and deliberate test overrides, not to patch missing framework wiring.
- When overriding a framework default in tests, prefer registering the replacement class so the container can apply normal `@DefaultComponent` replacement rules. Avoid injecting prebuilt framework instances such as `new TestRefreshEventPublisher()`.

Important files:
- `src/main/java/one/xis/context/IntegrationTestContext.java`: high-level API used by tests.
- `src/main/java/one/xis/context/IntegrationTestScript.java`: JS runtime bootstrap for integration tests.
- `src/main/java/one/xis/context/PushEventSimulator.java`: abstraction used to trigger client refreshes in tests.
