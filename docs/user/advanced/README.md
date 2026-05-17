# Advanced Topics

[Documentation map](../../README.md)

Advanced topics are optional or specialized XIS capabilities. They are important, but they should not block a developer
who wants to build a minimal application.

If you want quick results, start with the [Quickstart](../quickstart.md). Then read the core user documentation:

- [Core model](../core-model.md)
- [Navigation and responses](../navigation.md)
- [Template syntax](../templates.md)
- [Tags and attributes](../tags-and-attributes.md)
- [Forms and validation](../forms-and-validation.md)
- [Runtime and dependency model](../runtime-and-dependencies.md)

## Available Advanced Pages

- [Events](../events.md): declare refresh keys and publish events with `RefreshEventPublisher`.
- [XIS theme](theme.md): generated standard pages, forms, navigation, layout, and theme customization.
- [Microfrontend Architecture](microfrontend-architecture.md)
- [Reusable web artifacts](reusable-web-artifacts.md)
- [Aspects and interface advice](aspects.md)
- [Explicit SQL transactions](sql-transactions.md)
- [Custom JavaScript and custom EL functions](custom-javascript.md)
- [Custom proxies](custom-proxies.md)
- [Integration-test browser model](integration-test-browser.md)

## Planned Advanced Pages

These pages should be migrated carefully from the old documentation app and checked against code/tests before being
treated as complete. Topics already covered in the core user docs or in the available advanced pages are not repeated
here.

| Topic | Why it is advanced |
| --- | --- |
| Framework variables | Helpful once templates need request, validation, query, or storage context. |
| Client-side storage and client state | Convenience feature, not a core requirement. Applications can keep state on the server. |

## Rule For Advanced Documentation

Advanced documentation should still contain copyable examples. The difference is not quality; the difference is when a
reader needs the topic.

Core documentation should make a developer productive. Advanced documentation should make a developer complete.
