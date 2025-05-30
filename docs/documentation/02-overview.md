# 2. How XIS Applications Work – An Overview

XIS applications are built around a small set of clear concepts. Each one will be explained in detail in later chapters,
but this section gives you a high-level map of how everything fits together.

---

## The Building Blocks

A typical XIS application consists of:

- **Pages**  
  Represent full-screen views, defined by a Java controller (`@Page`) and a matching HTML file. Pages are part of a *
  *single-page application** architecture: even though they resemble traditional full-page views, switching between them
  does **not** cause a full browser reload.

  Each page may also provide **page-specific CSS or JavaScript**, which is automatically included when the page is
  loaded. This allows you to modularize styles and scripts per view, without global pollution.

- **Pagelets**  
  Represent logic-bound fragments of HTML, each backed by a controller (`@Pagelet`). While they can be used to build
  reusable UI components, their main purpose is to define **server-side logic for a small, well-scoped portion of a page
  **.

  A Pagelet is typically a **singleton** within the page and **not** designed for instantiating multiple times with
  separate states. Think of it as a logical subcontroller for one section of your layout.

- **Actions**  
  Declarative buttons, links, or triggers that invoke Java methods in your controllers—without using REST or JavaScript.

- **Model Binding**  
  Data is passed between controllers and views using simple annotations like `@ModelData` or `@FormData`. The framework
  handles population and synchronization.

- **Templates and Includes**  
  HTML files can use `<xis:include>` to embed shared fragments. You keep control over layout and avoid custom templating
  languages.

- **Navigation**  
  Pages load dynamically, enabling single-page-application behavior. All routing is handled by XIS automatically—no need
  to configure endpoints.

---

## A Typical Flow

Here’s what happens when a user navigates to a new page in a XIS app:

1. The URL triggers the matching `@Page` controller.
2. XIS calls lifecycle methods to populate data (`@ModelData`, `@RequestScope`, etc.).
3. The associated HTML file is loaded and combined with server-side values.
4. Actions on the page trigger further controller methods.
5. Pagelets and includes are loaded dynamically as needed.
6. The browser stays on the same physical page—the experience is seamless.

---

## What You’ll Learn Next

In the upcoming chapters, we’ll look at each of these pieces in detail:

- [Chapter 3](03-pages.md): How pages and their controllers work
- [Chapter 4](04-pagelets.md): How to build and nest reusable components
- [Chapter 5](05-data.md): How model binding and lifecycle work
- [Chapter 6](06-actions.md): How to define buttons, links, and logic triggers
- [Chapter 7](07-template-reference.md): How templates and includes work
- [Chapter 8](08-forms.md): How to build forms and validate user input

Each chapter builds on the last, but you can also jump directly to the topic you need.

---

> “XIS is built to make web development understandable again. This chapter is your map. The next ones are your journey.”

→ [Continue to Chapter 3: Pages (`@Page`)](03-pages.md)
