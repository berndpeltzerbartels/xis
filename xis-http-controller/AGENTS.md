# AGENTS

This module contains the HTTP annotation and controller API used by XIS internals and selected integrations.

End-user relevance:
- Usually not a first-choice dependency for end users.
- It may still be useful for advanced users who explicitly want to work with the HTTP/controller abstraction.
- It should not be treated as a default standalone transitive dependency for every user scenario beyond the chosen runtime.

Dependency guidance:
- Keep this module explicit when possible.
- Do not assume that end users should write their own endpoints; the primary XIS story is that they should not need to.
- If public usage becomes more important later, document that intentionally rather than letting it happen accidentally through transitive spread.

Working rules:
- Preserve stable annotation semantics and lightweight abstractions.
- When adding transport-related interfaces such as SSE contracts, keep them framework-agnostic.

Important files:
- `src/main/java/one/xis/http/RequestHeader.java`
- `src/main/java/one/xis/http/UrlParameter.java`
- `src/main/java/one/xis/http/SseEndpoint.java`
- `src/main/java/one/xis/http/SseEmitter.java`
