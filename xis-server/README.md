# XIS Server Core

This module contains the shared server-side runtime logic used by XIS.

Most application developers do not use this module directly. It sits behind `xis-boot` and `xis-spring` and provides
the core orchestration for controllers, rendering, actions, and refresh events.

## Refresh Events

XIS refresh events are intentionally small and transport-oriented.

They are used to tell connected browsers that something changed and that affected UI parts should reload.

They do not send business payloads.

Typical flow:

1. Server publishes an event key such as `score-updated`.
2. Browser receives the key.
3. XIS reloads the page or widget data through the normal request flow.

This keeps server push simple and avoids building a second application protocol next to the regular XIS rendering flow.

## Target Types

Refresh events can target:

- all connected clients
- specific client IDs
- specific user IDs
- all authenticated users

### Client IDs

Client IDs are always available.

They identify browser/client instances and work without authentication.

### User IDs

User IDs are only available when XIS authentication is active.

If user-targeted refresh is used without the authentication module, XIS raises an exception that points to
`xis-authentication`.

## Transport Direction

The preferred server push transport is SSE.

This module exposes the core event model and dispatch logic, while the runtime-specific wiring is implemented in:

- `xis-boot`
- `xis-spring`
