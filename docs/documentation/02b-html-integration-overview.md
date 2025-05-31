# 2b. HTML Integration Overview

At its core, XIS uses plain HTML files to define the structure of your views. Unlike template engines that mix logic and
markup, XIS separates concerns cleanly: Java controllers provide the logic, HTML files define the layout.

---

## File Mapping: Java ↔ HTML

Each controller corresponds to exactly one HTML file:

- A controller named `HelloPage.java` will automatically use `HelloPage.html`.
- Both files must be placed in the same package/folder structure.
- If needed, you can override the default by using `@HtmlFile("CustomName.html")`.

**Example:**

```bash
src/
└── main/
    └── java/
          └──com/
              └──example/
                  ├── HelloPage.java
                  └── HelloPage.html
```

Alternatively, you can use the `resources` folder, but it must mirror the Java package structure.

```bash
---

## Folder Structure & Page Resolution

- All HTML files live in the **resources folder**, using the same package-style paths as your Java classes.
- Subfolders can be used to group pages logically (e.g. `admin/UserPage.java` → `admin/UserPage.html`).
- The framework auto-detects the correct HTML file based on class name unless overridden.

---

## Layout Composition: Includes

XIS supports a simple include mechanism to reuse HTML fragments:

```html

<xis:include file="/shared/header.html"/>
<xis:include file="/shared/footer.html"/>
```

These files can contain layout fragments, menus, or any reusable markup. There is no template language—just raw HTML and
includes.

---

## CSS & JavaScript

Each page can bring its own styles and scripts:

- `HelloPage.html` may require specific css or js files.

```bash
resources/com/example/pages/
├── HelloPage.java
├── HelloPage.html
├── HelloPage.css
└── HelloPage.js
```

These files will be **automatically included** if they match the name of the HTML file.

---

## Summary

- HTML is treated as a **first-class citizen**
- No custom DSL or template engine required
- Clean 1:1 mapping between logic and layout
- Supports modular, page-specific styling and scripting
- Encourages reuse through includes

---

→ [Continue to Chapter 3: Pages (`@Page`)](03-pages.md)
