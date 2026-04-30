# AGENTS

This module defines the Gradle plugin and therefore the dependency experience seen by end users.

End-user relevance:
- Directly end-user facing.
- The plugin should make the common path easy without hiding important runtime choices.
- `xis-boot` and `xis-spring` are alternative runtime choices; both being present in one application usually does not make sense.

Dependency policy:
- `xis-test` belongs on the test side and is appropriate as `testImplementation`.
- SSE belongs to the normal runtime story and should not require a separate legacy transport module.
- Do not reintroduce WebSocket modules as defaults or optional recommendations.
- `xis-http-controller` can be useful to advanced users, but it should remain an explicit choice where possible rather than being positioned as a required end-user entry point.

Working rules:
- Changes here should be checked against the intended product story, not just compile success.
- When modifying dependency constraints, think in terms of:
  - end-user runtime choice
  - optional features
  - internal modules that should stay hidden

Important files:
- `src/main/java/one/xis/plugin/XISPlugin.java`: central dependency policy.
- `build.gradle`: plugin publication and packaging metadata.
