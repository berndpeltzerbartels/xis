# XIS – A Declarative Java Web Framework

**XIS** is a lightweight, fast, and structured web framework for building **single-page Java applications** with fully *
*declarative HTML templates**. It empowers developers to structure their UIs into manageable fragments, each with its
own **Java controller**, supporting **microfrontend architectures** and modular development workflows.

XIS covers the entire **client-server communication layer**, providing seamless data binding and routing — and includes
built-in support for **push communication via Socket.IO**, enabling real-time updates out of the box.

XIS combines a minimal footprint with strong typing, full IDE support, and a clear separation of concerns. It avoids
boilerplate through powerful conventions while remaining highly explicit and testable. The result is a streamlined
developer experience — without giving up control.

---

## 🔗 Quick Links

- [🚀 Quickstart Guide](#quickstart-guide)
- [📐 Architecture & Design](#architecture--design)
- [🤔 Why XIS?](#why-xis)
- [📚 Documentation](https://xis.one/docs/)
- [📘 Javadoc](https://javadoc.io/doc/one.xis/xis-core)

---

## 🚀 Quickstart Guide

_Read the full guide in [docs/Quickstart.md](docs/quickstart-spring/Quickstart-Setup.md)_

XIS applications are built from simple HTML templates, each backed by a Java controller class. You define your UI in
HTML and bind it directly to Java methods, keeping logic and markup close but cleanly separated.

The project structure encourages clear responsibilities:

- Each **HTML fragment** corresponds to one controller.
- Controllers are written in plain Java, with zero framework-specific annotations.
- All routing, data binding and lifecycle control is **declarative and explicit**.

Build your first page in minutes with the Quickstart guide.

---

## 📐 Architecture & Design

_Read the full architecture overview in [docs/Architecture.md](docs/Architecture.md)_

XIS promotes a component-based, modular architecture that makes it easy to:

- Split applications into self-contained fragments or pages
- Assign responsibility to teams per domain or feature
- Compose applications from reusable UI modules

This structure naturally supports **microfrontend patterns**, while maintaining a consistent development model based
entirely on Java and HTML.

---

## 🤔 Why XIS?

Other Java frameworks often force you into:

- heavyweight abstractions
- reflection-based dependency injection
- magic annotations and XML config
- template engines with little IDE support

**XIS takes a different approach.**

### What makes it different:

- ✅ **Declarative HTML-first approach** with Java-backed controllers
- 🧩 **Composable UI fragments**, each with its own logic
- 🚀 **Fast and lightweight**, no runtime overhead or proxies
- 📦 **Boilerplate-free**, sensible defaults and strong typing
- 🛠️ **Microfrontend-ready**: Natural support for distributed UIs and independent ownership
- 👥 **Team-friendly**: Developers can work independently on isolated features and provide their HTML and logic in one
  place

If you’re tired of bloated, abstracted frameworks and want something clean, fast, and transparent — **XIS is built for
you.**

