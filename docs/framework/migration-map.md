# Documentation Migration Map

The old documentation app in `/Users/bernd/projects/xis-docs` contains useful source material. This map records how the
Markdown documentation should absorb it.

## Migrated Into User Docs

| Old source area | Markdown target |
| --- | --- |
| Quickstart installation, main class, welcome page, hello world | `docs/user/quickstart.md` |
| Introduction and Why XIS | `README.md`, `docs/user/quickstart.md` |
| Pages, frontlets, includes | `docs/user/core-model.md` |
| Page links, deep linking, action return types | `docs/user/navigation.md` |
| Expression language | `docs/user/templates.md` |
| Framework attributes and framework tags | `docs/user/tags-and-attributes.md` |
| Form binding, form actions, validation | `docs/user/forms-and-validation.md` |
| Runtime selection, XIS Boot overview | `docs/user/runtime-and-dependencies.md` |
| Testing philosophy | `docs/user/examples-and-tests.md` |
| Documentation layering for advanced topics | `docs/user/advanced/README.md` |

## Core Topics Still To Deepen

These topics belong in the normal user flow because a developer may need them for ordinary applications:

- formatters
- template locations
- Gradle plugin details
- full public API reference tables
- lifecycle and request/update flow
- parameter annotations
- method annotations
- class annotations

## Advanced Topics Still To Migrate Or Deepen

These areas exist in the old app but need a careful Markdown pass before becoming canonical user documentation:

- security, local authentication, roles, SSO, external identity providers
- distributed applications and micro-frontend architecture
- custom JavaScript
- selectable highlighting
- system errors
- advanced frontlet containers, frontlet parameters, frontlet update events
- custom expression language functions
- framework variables
- XIS theme
- client-side storage and local/session storage helpers

## Migration Rule

Do not copy pages one-to-one just to increase coverage. Each migrated page should become either:

- user documentation with copyable examples
- framework documentation with implementation context
- agent documentation with operational rules
- a deliberate backlog item if the source is outdated or the behavior is not stable

For template-related pages, check the runtime files before migrating:

- `xis-javascript/src/main/js/classes/init/DomNormalizer.js`
- `xis-javascript/src/main/js/classes/init/HandlerBuilder.js`
- `xis-javascript/src/main/js/classes/parse/ELFunctions.js`
