# AGENTS

This module contains the transport-independent server orchestration for XIS.

End-user relevance:
- Not a module that end users should normally depend on directly.
- It is part of the runtime stack behind `xis-boot` and `xis-spring`.
- Changes here affect both runtime models, so treat this as shared infrastructure.

Current transport direction:
- `MainController` exposes `GET /xis/events` for SSE subscriptions.
- Browser clients may identify themselves via `clientId` query parameter.
- Existing refresh events are intended as UI refresh triggers, not payload-bearing messages.

Working rules:
- Prefer transport-neutral abstractions in this module.
- If you touch refresh delivery, review `RefreshEvent`, `RefreshEventPublisher`, `SseService`, and `MainController` together.
- Do not reintroduce WebSocket or payload-push abstractions here. SSE refresh events are the active direction.

Important files:
- `src/main/java/one/xis/server/MainController.java`: public SSE subscription endpoint.
- `src/main/java/one/xis/server/SseService.java`: active emitter registry and refresh dispatch.
- `src/main/java/one/xis/server/RefreshEvent.java`: shared refresh trigger model for server-side publishers.
