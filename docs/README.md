# XIS Documentation

This directory is the canonical documentation source for users, framework developers, and coding agents.

The old documentation application in `docs-app` / `/Users/bernd/projects/xis-docs` is source material, not the target
structure. The Markdown documentation in this repository should become the stable knowledge base.

## User Documentation

Start here if you want quick results:

- [Quickstart](user/quickstart.md)

Read these to build normal XIS applications:

- [Runtime and dependency model](user/runtime-and-dependencies.md)
- [Template location and mapping](user/template-location-and-mapping.md)
- [Core model](user/core-model.md)
- [Annotation reference](user/annotations.md)
- [Template syntax](user/templates.md)
- [Tags and attributes](user/tags-and-attributes.md)
- [Navigation and responses](user/navigation.md)
- [Forms and validation](user/forms-and-validation.md)
- [Security](user/security.md)
- [Examples and tests](user/examples-and-tests.md)

Optional or specialized capabilities live under:

- [Advanced topics](user/advanced/README.md)

User documentation should be organized around public API behavior and copyable examples, not around repository modules.

## Framework Developer Documentation

Use this layer when working on XIS itself:

- [Documentation strategy](framework/documentation-strategy.md)
- [Migration map](framework/migration-map.md)
- [Architecture](architecture.md)

Module README files belong to this layer. They may explain internals, design trade-offs, and implementation details.

## Agent Documentation

Use this layer when an automated coding agent works in the repository:

- [Agent documentation rules](agent/documentation-rules.md)

The root [agents.mds](../agents.mds) file summarizes repository-wide working rules.
