# XIS Documentation

Welcome to the official documentation of **XIS**, a lightweight and transparent Java web framework designed to keep
things simple, explicit, and robust.

## What is XIS?

XIS offers a modern approach to building server-side rendered HTML applications in Java. It combines plain HTML and
plain Java in a predictable and testable way. Instead of hiding behavior behind configuration and annotations, XIS
promotes clarity and directness.

## Why XIS?

Most Java frameworks are either overly complex or rely heavily on conventions that are hard to trace and debug. XIS was
built as an alternative to these approaches, aiming for:

- Minimal "magic"
- Simple and intuitive API, easy to learn
- Fast and lightweight
- Works with Spring and Micronaut, or standalone
- Supports java 17+ and Groovy
- Transparent view-controller coupling
- Strict separation of responsibilities
- IDE-friendly structures
- Optional but clean support for modern patterns like microfrontends

With XIS, you define views as standard HTML files and back them with Java controller classes that are easy to test,
navigate, and evolve. The result is a framework that's accessible for beginners and powerful enough for advanced use
cases.

## Structure of this documentation

This documentation is divided into clearly structured chapters. You can start with an overview or jump directly to a
specific topic:

- [1. Introduction & Philosophy](docs/documentation/01-introduction.md)
- [2. Module Overview](docs/documentation/02-overview)
- [2b. HTML Integration Overview](docs/documentation/02b-html-integration-overview.md)
- [3. Pages (`@Page`)](docs/documentation/03-pages.md)
- [4. Pagelets (`@Pagelet`)](docs/documentation/04-pagelets.md)
- [5. Data Lifecycle & Model Binding](docs/documentation/05-data.md)
- [6. Actions & Navigation](docs/documentation/06-actions.md)
- [7. Template Language (Full Reference)](docs/documentation/07-template-reference.md)
- [8. Forms & Validation](docs/documentation/08-forms.md)
- [9. Extension Points](docs/documentation/09-extension.md)
- [10. Deployment & Build](docs/documentation/10-deployment.md)
- [11. Appendix & Reference](docs/documentation/11-appendix.md)

If you're new to XIS, we recommend starting with the [Quickstart Guide](quickstart/index.md).

---
→ [Start with Chapter 1: Introduction & Philosophy](docs/01-introduction.md)
