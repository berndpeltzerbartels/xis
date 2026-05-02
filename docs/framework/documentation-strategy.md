# Documentation Strategy

This repository uses three documentation layers.

## User Layer

Files:

- `README.md`
- `docs/user/*`

Audience:

- application developers using XIS
- people evaluating the framework
- future users who have no forum, blog posts, or external examples available

Rules:

- Organize by public API behavior, not by repository module.
- Prefer copyable Java and HTML examples.
- Every stable public API should eventually have at least one executable example.
- Avoid internal implementation detail unless it changes how a user writes an application.

## Framework Developer Layer

Files:

- module README files
- `docs/framework/*`
- `docs/architecture.md`

Audience:

- maintainers of XIS itself
- contributors changing internals

Rules:

- Explain module responsibilities, design trade-offs, internal contracts, and extension points.
- It is acceptable for this layer to be module-oriented.
- Link to user docs when a public behavior is affected.

## Agent Layer

Files:

- `agents.mds`
- `docs/agent/*`

Audience:

- automated coding agents working in this repository

Rules:

- State where the canonical documentation lives.
- State how examples should be treated.
- Keep instructions concrete and operational.

## Website Policy

The documentation website should not become a second manually maintained truth.

Preferred model:

```text
Markdown documentation in this repository
        |
        +-- GitHub README/docs
        |
        +-- website rendering/importing the same content
```

The website can add navigation, design, examples, and interactive features such as a project wizard. It should not
replace the Markdown knowledge base.

## Example Policy

Documentation examples are part of the public API.

A good example should:

- compile or be close enough to compile with only obvious application-specific services missing
- name files and packages
- include both Java and template code when behavior crosses that boundary
- avoid pseudocode for public API mechanics
- become covered by tests when the API stabilizes

When a documented example cannot yet be tested, mark that as documentation debt rather than silently accepting it.

## Template Documentation Policy

Template documentation must be checked against runtime behavior:

- Use `DomNormalizer.js` to confirm tag and attribute spellings.
- Use `HandlerBuilder.js` to confirm which constructs actually receive behavior.
- Use `ELFunctions.js` to confirm built-in expression-language functions.

The old documentation app is useful for content and reader flow, but runtime files decide the current public surface.
