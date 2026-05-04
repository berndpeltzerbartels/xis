# Agent Documentation Rules

When working on XIS documentation, treat Markdown in this repository as canonical.

## Source Material

The old XIS documentation application may be available through `docs-app` or `/Users/bernd/projects/xis-docs`.
Its HTML files are useful source material, but do not preserve its structure blindly.

## Template Documentation Sources

For template-related documentation, verify behavior against the runtime files:

- `xis-javascript/src/main/js/classes/init/DomNormalizer.js` for supported framework tags, attributes, and normalization.
- `xis-javascript/src/main/js/classes/init/HandlerBuilder.js` for handlers and runtime behavior.
- `xis-javascript/src/main/js/classes/parse/ELFunctions.js` for built-in EL functions.

Do not document a tag, attribute, or EL function as stable only because it appears in the old documentation.

## Target Structure

- Root `README.md`: public user entry point.
- `docs/user/*`: user documentation organized by public API and workflows.
- Module `README.md` files: framework-developer notes and module-specific details.
- `docs/framework/*`: cross-module architecture and maintainer documentation.
- `docs/agent/*` and `agents.mds`: instructions for coding agents.

## Editing Rules

- Prefer adding copyable examples over adding prose.
- Keep examples realistic and small.
- Include both Java and HTML when documenting page, frontlet, action, form, or validation behavior.
- For template features with attribute and element syntax, document both and test both when the behavior is public.
- For form controls, tests should check the object received by the Java `@Action` method through `@FormData`.
- Do not make the website a separate manual documentation source.
- If behavior is uncertain, inspect source and tests before documenting it as stable.
- If no test exists for a public example, leave the example useful anyway and consider adding a test next.

## Documentation Definition of Done

For public API changes, aim for:

- implementation updated
- test updated or added
- user documentation updated
- copyable example updated or added
- module README updated only when internals or module contracts changed
