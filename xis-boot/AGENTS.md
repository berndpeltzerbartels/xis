# AGENTS

This module is one of the two primary end-user runtime choices for XIS.

End-user relevance:
- Intended for end users building standalone XIS applications.
- A project should use either `xis-boot` or `xis-spring`, not both together.
- `xis-boot` intentionally exposes `xis-context` because standalone boot applications need that integration path.

Dependency guidance:
- Treat SSE support as part of the runtime, not as a separate end-user addon.
- Do not introduce a second explicit transport dependency for SSE if the functionality is already covered by the runtime stack.
- If transport features are added here, keep in mind that `xis-http-controller` is already part of the internal runtime wiring.

Working rules:
- Preserve the standalone-runtime role of this module.
- Changes here should not assume Spring is present.
- When touching transport behavior, review Netty HTTP handling and SSE endpoint registration together.

Important files:
- `build.gradle`: declares the standalone dependency surface.
- `src/main/java/one/xis/boot/netty/NettyServer.java`: Netty pipeline setup.
- `src/main/java/one/xis/boot/netty/NettySseEndpoint.java`: SSE endpoint binding for boot runtime.
