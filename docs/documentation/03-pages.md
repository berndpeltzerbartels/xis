# 3. Pages (`@Page`)

Pages are the primary building blocks of a XIS application. Each page represents a full-screen view in your application
and is backed by a Java class annotated with `@Page`.

Despite their name, Pages in XIS do **not** trigger a full browser reload when navigating between them. They behave like
views in a single-page application (SPA), enabling fast, seamless transitions and state preservation.

---

## Basic Structure

Each page consists of:

- A Java class annotated with `@Page`
- A matching HTML file
- Optional methods to populate data, define actions, or handle state

Example:

```java

@Page
public class HelloPage {

    @ModelData
    public String name() {
        return "World";
    }
}
```

```html
<!-- HelloPage.html -->
<h1>Hello, ${name}!</h1>
```

---

## File Matching

- The HTML file must match the Java class name and package.
- `HelloPage.java` ↔ `HelloPage.html`
- Files are matched automatically unless overridden with `@HtmlFile(...)`.

---

## Navigation

XIS handles all navigation internally. When a user clicks a link to a new page:

1. The current view is unloaded
2. The new `@Page` controller is instantiated
3. Lifecycle methods are executed (e.g. `@ModelData`)
4. The matching HTML is rendered and inserted into the DOM

You don’t need routing libraries or JavaScript—navigation happens declaratively.

To link to a page:

```html
<a xis:page="/HelloPage.html">Go to Hello Page</a>
```

Have in mind that this link might look like a full page reload, but it is actually handled by XIS as a single-page
application transition. The default behavior is to replace the current view without reloading the entire page.

## Summary

- Pages are single-view controllers backed by HTML
- They behave like SPA views (no full reloads)
- Lifecycle methods populate variables and dependencies
- Navigation and linking are declarative and type-safe
- Parameters are handled automatically

---

→ [Continue to Chapter 4: Pagelets (`@Pagelet`)](04-pagelets.md)
