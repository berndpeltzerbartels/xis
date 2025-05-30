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

```text
src/
└── main/
    ├── java/com/example/pages/HelloPage.java
    └── resources/com/example/pages/HelloPage.html
