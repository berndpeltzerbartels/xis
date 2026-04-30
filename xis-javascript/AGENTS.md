# AGENTS

This module contains the browser runtime for XIS.

End-user relevance:
- Not a direct dependency that users typically choose manually.
- Comes in transitively through the selected runtime (`xis-boot` or `xis-spring`).
- Treat this as runtime internals, even though it directly affects user-visible browser behavior.

Current transport direction:
- Prefer Server-Sent Events over WebSocket for server-triggered refreshes.
- The browser connects to `/xis/events` via `EventSource`.
- The client id is passed as a query parameter because native `EventSource` cannot attach custom request headers.

Working rules:
- Keep push-event handling transport-lightweight: events are refresh triggers, not data payloads.
- When changing `Application.js`, check both runtime code in `src/main/js/app` and test code in `src/main/js/test`.
- Keep integration-test helpers transport-neutral where possible. `simulatePushEvent(...)` should describe behavior, not protocol.

Important files:
- `src/main/js/app/classes/Application.js`: app bootstrap and SSE connector.
- `src/main/js/test/classes/TestApplication.js`: JS runtime used by integration tests.
- `src/main/js/test/Functions.js`: test bridge functions exposed to Java integration tests.
