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
- Refresh delivery should tolerate short SSE reconnect gaps. `SseService` tracks recently known clients and queues
  refresh event keys for the reconnect window instead of logging and dropping them. Keep the reconnect window aligned
  with `ClientConfigService.PENDING_EVENT_TTL_SECONDS`, because the browser receives the same value in `ClientConfig`.
- SSE send failures are part of the reconnect path. `SseService` must observe the `CompletionStage` returned by
  `SseEmitter.send(...)`; on failure it removes/closes the emitter and keeps or queues the event for reconnect.

Working rules:
- Prefer transport-neutral abstractions in this module.
- If you touch refresh delivery, review the controller-api refresh API (`RefreshEvent`, `RefreshEventPublisher`,
  `RefreshTarget`) together with `SseService` and `MainController`.
- Do not reintroduce WebSocket or payload-push abstractions here. SSE refresh events are the active direction.

Important files:
- `src/main/java/one/xis/server/MainController.java`: public SSE subscription endpoint.
- `src/main/java/one/xis/server/SseService.java`: active emitter registry and refresh dispatch.
- `xis-controller-api/src/main/java/one/xis/RefreshEvent.java`: shared refresh trigger model for user-side publishers.
