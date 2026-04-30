# AGENTS

This module is one of the two primary end-user runtime choices for XIS.

End-user relevance:
- Intended for end users integrating XIS into Spring-based applications.
- A project should use either `xis-spring` or `xis-boot`, not both together.
- This module already exposes the HTTP annotation API that Spring-side users may need.

Dependency guidance:
- `xis-http-controller` may be interesting to advanced users, but it is not meant to be a separately forced default outside the runtime choice.
- SSE support should be considered part of the Spring runtime path once it is fully adopted.
- Keep the distinction to `xis-boot` intact: `xis-context` handling differs on purpose.

Working rules:
- Preserve the Spring integration role of this module.
- Do not make this module depend on standalone boot assumptions.
- When touching refresh transport, review `SpringSseEndpoint` together with the public controller flow in `xis-server`.

Important files:
- `build.gradle`: declares the Spring-facing dependency surface.
- `src/main/java/one/xis/spring/SpringSseEndpoint.java`: SSE endpoint binding for Spring runtime.
