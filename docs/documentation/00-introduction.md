# The Origins & Philosophy of XIS

## The Origins of XIS

Before creating XIS, I had worked on many web applications and kept noticing the same repetitive patterns. Whether in
frontend or backend development, I often found myself writing nearly identical code for different entities—defining
actions, reducers, controllers, and service logic that followed the same structure over and over again.

In the backend, every new feature required a new controller. These controllers usually just wrapped standard CRUD
operations, differing only in the data types they managed. Meanwhile, collaboration between frontend and backend teams
often led to friction: APIs had to be discussed, documented, implemented, updated—and the cycle repeated with every
change.

At the same time, I was fascinated by the idea of **microfrontend architecture**. It felt like a natural extension of
microservices, enabling a true vertical division of work across teams. But I found few practical implementations that
made this concept work cleanly in the real world.

All of this became the driving force behind XIS: a framework that simplifies common tasks, reduces team friction, and
enables clear, scalable frontend-backend structures—without sacrificing control or performance.

---

## Why XIS?

Most Java frameworks are either overly complex or rely heavily on conventions that are difficult to trace and debug. XIS
was created as a practical alternative. It focuses on:

- Minimal "magic"
- Transparent communication between components
- A declarative, annotation-driven model
- Seamless integration with modern frontend architectures
- An intuitive API with a low learning curve

It’s designed to be simple, explicit, and robust—ideal for developers who want to **understand**, not guess.

---

## Core Principles

Here are the principles that define XIS:

### 🧩 1. Simple and Lightweight

XIS avoids bloated abstractions and excessive dependencies. Its core is small and focused, so your app stays fast and
maintainable.

### 🧠 2. Declarative, Annotation-Based Programming

Controller logic is expressed through clear, Java-based annotations—no need for XML, YAML, or complex runtime
introspection.

### 🔄 3. Fully Managed Communication

The framework handles data flow between frontend and backend automatically, without requiring REST APIs or manual
wiring.

### 🌐 4. Single Page Architecture by Default

Each page is defined by a controller and a corresponding HTML file. Navigation happens without full reloads, enabling
SPA-like experiences.

### 🧱 5. Microfrontend Support

XIS is built with vertical modularity in mind. It encourages separation into independently developed, testable
units—perfect for microfrontend projects.

### 🧪 6. Easy to Test, TDD-Friendly

Since controller logic is plain Java, it’s easy to unit test. You can use your favorite testing frameworks—no custom
bootstrapping required.

### 🔗 7. Clear View–Controller Mapping

Every HTML file has exactly one Java controller. This 1:1 relationship makes navigation, debugging, and reasoning about
your code effortless.

### 🧭 8. Intuitive API with Low Learning Curve

You don’t need to learn an internal DSL. If you know Java and HTML, you’re ready to use XIS productively.

### 🔧 9. Easy Interoperability

XIS plays well with Spring, Micronaut, and other Java stacks. It doesn't lock you in—and can run standalone or embedded.

### 🔌 10. No REST, No JavaScript Calls Required

XIS eliminates the need to write custom REST endpoints or client-side JavaScript for calling your backend. Actions are
declared once—in the HTML—and automatically linked to your Java controller methods.  
You don’t need to worry about AJAX, fetch calls, or serialization: it all just works.

---

## In short

> "XIS is what you get when you stop trying to outsmart the developer and instead work with them."

---

→ [Chapter 1: Module Overview](01-overview.md)
