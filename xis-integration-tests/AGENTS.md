# AGENTS

This module contains framework-level integration tests for XIS behavior.

Testing focus:
- Prefer end-to-end behavior over implementation details.
- Keep tests transport-neutral where possible; assertions should focus on DOM updates and navigation outcomes.
- When finishing the terminology refactor to `Frontlet`, remember that runtime references may still live in HTML templates, `xis:frontlet`, `default-frontlet`, and test setup values even when Java code already compiles.

Framework override rules:
- Tests should assume that XIS infrastructure is already present through the normal `IntegrationTestContext` bootstrap.
- Do not require test authors to manually register framework internals just to make XIS work.
- If a framework default must be replaced in a test, prefer registering the replacement class so container-driven `@DefaultComponent` replacement still applies.
- Avoid `withSingleton(new SomeFrameworkOverride())` for XIS internals when the intention is to replace a framework default.

Relevant areas:
- `src/test/java/test/page/refresh`: refresh-event and multi-client behavior.
- frontlet interaction coverage.
- `src/test/java/test/page/core`: basic bootstrap and navigation safety checks.
