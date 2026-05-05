# Advanced Topics

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

- [Refresh events](refresh-events.md)

## Planned Advanced Pages

These pages should be migrated carefully from the old documentation app and checked against code/tests before being
treated as complete:

| Topic | Why it is advanced |
| --- | --- |
| Security, roles, SSO, XIS as IDP, external IDP | Many applications need security, but the IDP/SSO details are integration-specific and should not complicate the first app. |
| Distributed applications and micro-frontends | Important architecture topic, but not needed for same-origin applications. |
| Frontlet containers, parameters, and update events | Needed for richer composition, but the basics belong in core/navigation first. |
| Custom JavaScript | Optional escape hatch for specialized browser behavior. |
| Custom expression-language functions | Optional extension point for project-specific template helpers. Built-in EL functions are part of core template syntax. |
| Formatters | Useful when display/parsing rules become application-specific. |
| Framework variables | Helpful once templates need request, validation, query, or storage context. |
| XIS theme and selectable highlighting | Presentation and UI behavior customization. |
| System errors | Operational/customization topic. |
| Client-side storage and client state | Convenience feature, not a core requirement. Applications can keep state on the server. |

## Rule For Advanced Documentation

Advanced documentation should still contain copyable examples. The difference is not quality; the difference is when a
reader needs the topic.

Core documentation should make a developer productive. Advanced documentation should make a developer complete.
