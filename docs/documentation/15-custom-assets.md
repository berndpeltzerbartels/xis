## 17. Custom JavaScript Functions and CSS

XIS allows each component or page to include its own JavaScript and CSS resources. This modular design ensures that
logic and styling stay **close to their context**, simplifying maintenance and improving separation of concerns.

---

### Recommended Folder Layout (inside Java package)

We recommend placing your static resources directly alongside your Java controllers and templates, like this:

```
com/example/shop/      # Java package
├── ShoppingPage.java   # @Page controller
├── ShoppingPage.html   # Template
├── ShoppingPage.js     # JavaScript (optional)
└── ShoppingPage.css    # CSS (optional)
```

Benefits:

* Modular and self-contained
* Easy to locate and manage
* Resources follow the naming conventions of the associated controller
* XIS plugin automatically configures Gradle to include these files as classpath resources

> ✅ This layout supports clean code structure and mirrors the HTML+Java+JS/CSS as one cohesive unit.

---

### Alternative: Placing Files in `resources/`

If you prefer to keep static resources in the `resources` folder, you can do so by mimicking the Java package structure:

```
src/main/resources/com/example/shop/
├── ShoppingPage.html
├── ShoppingPage.js
└── ShoppingPage.css
```

This works **without any annotations**, as long as the names and folder structure match the controller’s package.

> ⚠️ Using this layout may reduce IDE support for Java-centric workflows. It is supported but less modular.

---

### Explicit File Linking via Annotations

As a fallback or for advanced scenarios, you can explicitly annotate your controller:

```java

@JavaScriptFile("/com/example/shop/ShoppingPage.js")
@CSSFile("/com/example/shop/ShoppingPage.css")
public class ShoppingPage {
    // ...
}
```

* The values in the annotations are classpath-relative paths.
* These annotations override the naming convention.

---

### Purpose of the JavaScript File

The JS file associated with a page provides:

* **Expression Language (EL) functions** usable in HTML bindings

  ```html
  <div class="${calculateDiscount(price)}">
  ```
* Optionally, other custom client-side behavior (e.g. analytics, UI toggles)

> ✨ These functions are automatically available in the template's expression context.

> 🔒 The JS file should **not** register global listeners or override the DOM globally unless required. It is intended as
> a logic layer, not a monolithic script.

---

### Summary

| Option                   | Recommended | Needs Annotation | Modular  | Works Without Plugin |
|--------------------------|-------------|------------------|----------|----------------------|
| Java package co-location | ✅ Yes       | ❌ No             | ✅ Yes    | ⚠️ No                |
| Resource folder layout   | ⚠️ Optional | ❌ No             | ⚠️ Less  | ✅ Yes                |
| Explicit annotation      | ⚠️ Optional | ✅ Yes            | ⚠️ Mixed | ✅ Yes                |

> ✅ Recommended: keep everything together in the Java package.

---

### TODO (Framework Internals)

* ✅ Gradle plugin ensures `src/main/java` is also scanned for resources.
* 🛠️ A Maven plugin is planned but not yet implemented.
* 🔍 Make sure Expression Language engine only scans the local JS file for EL-visible functions.

[Kapitel 14: Client State and Reactive Values ←](14-client-state.md) | [Kapitel 16: Security →](16-security.md)