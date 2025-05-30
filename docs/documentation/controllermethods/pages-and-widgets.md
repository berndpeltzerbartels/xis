# Pages and Widgets

In XIS, the building blocks of a web application are **pages** and **widgets**. Both are implemented as annotated Java
classes and backed by corresponding HTML files. While they follow a similar structure, they serve different purposes in
the overall application architecture.

---

## Pages

A page represents a full browser view and is directly addressable via a URL. Each page class must have a matching HTML
file with the same name (e.g., `HelloPage.java` ↔ `HelloPage.html`).

Pages:

- Represent navigable top-level views
- Are loaded via direct browser navigation or redirects
- Contain layout, routing targets, and widgets

Although pages change the displayed content, they do **not** break the concept of a single-page application (SPA). Only
the content area is replaced; the overall HTML structure, layout, and JavaScript context remain intact.

### Example: Page Class

```java

@Page
public class HelloPage {
    @ModelData("state.message")
    public String message() {
        return "Hello from XIS";
    }
}
```

### Example: Page Template (`HelloPage.html`)

```html
<h1>${state.message}</h1>
```

This example defines a simple page that provides data to the template via `@ModelData`.

---

## Widgets

Widgets are components that are rendered inside a page or other widgets. Each widget class is also linked to
an HTML file of the same name.

Widgets:

- Represent **embedded components** within a page
- Can be placed into widget containers defined in the page's HTML
- Can appear multiple times in the same page (e.g. in a loop)
- Have their own controller class and template

While widgets are implemented as classes and behave like singletons on the controller level, they **can be rendered
multiple times** in the same page – for example, when iterating over a list. The framework manages the correct data
separation and ensures each instance behaves correctly.

Widgets are useful for:

- Separating code into smaller, testable units
- Structuring complex pages
- Building microfrontend-style architectures

They do **not** represent shared or static HTML fragments – their logic is fully dynamic and reactive, just like pages.

### Example: Widget Class

```java

@Widget
public class HelloWidget {

    @ModelData("state.message")
    public String message() {
        return "Hello from a widget";
    }
}
```

### Example: Widget Template (`HelloWidget.html`)

```html
<p>${state.message}</p>
```

---

### Example: Page with Widget Container (Widget Host Page)

This example shows how a page can act purely as a platform to host a widget. It does not provide any controller logic of
its own:

#### Java (`HelloWidgetPage.java`)

```java

@Page
public class HelloWidgetPage {
    // Empty controller – renders a widget only
}
```

To include a widget in a page, the HTML must define a widget container. In most cases, the container specifies a
`default-widget`:

#### HTML (`HelloWidgetPage.html`)

```html
<h1>Hello from XIS</h1>
<xis:widget-container id="hello" xis:default-widget="HelloWidget"></xis:widget-container>
```

Note: Widgets can themselves contain `widget-container` elements, enabling nested compositions.

---

## Summary

- Pages define full screens and URLs
- Widgets define embedded UI blocks
- Both use HTML + Java together
- Widgets are singletons in logic, but support multiple renderings per page
- This separation helps organize code, especially in larger or modular applications
- Page transitions do not reload the app – only the content is replaced, maintaining SPA behavior

