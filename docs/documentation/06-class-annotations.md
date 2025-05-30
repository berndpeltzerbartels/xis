# 6. Class-Level Annotations

XIS uses class-level annotations to connect Java classes with HTML/CSS/JS templates and to define their roles in the application. This chapter covers key annotations such as `@Page`, `@Pagelet`, `@HtmlFile`, `@JavascriptFile`, and `@CSSFile`.

---

## 🧩 @Pagelet

Marks a class as a pagelet controller.

```java
@Pagelet
public class ProductListPagelet { ... }
```

### ID Behavior:
- The `value` attribute is **optional**.
- If omitted, the class's **simple name** (e.g., `ProductListPagelet`) becomes the ID.
- This ID must be **unique** within the application.
- To override or avoid conflicts, set the value explicitly:

```java
@Pagelet("product-list")
public class ProductListPagelet { ... }
```

---

## 📄 @Page

Marks a controller as the entry point for a full HTML page.

```java
@Page("products.html")
public class ProductPage { ... }
```

### Rules for `@Page`:
- `value` is **mandatory**.
- Must end in `.html`.
- Can include wildcards (`*`) to define parameterized URLs:

```java
@Page("product-*.html") // Matches product-42.html, product-abc.html, etc.
```

---

## 🧬 @HtmlFile

Overrides the default HTML file that XIS expects to find for a page or pagelet.

```java
@HtmlFile("custom-product-view.html")
public class ProductViewPagelet { ... }
```

- **Optional**.
- File must end with `.html`.
- If omitted, XIS defaults to a file matching the class name (e.g., `ProductViewPagelet.html`).

---

## 🧪 @JavascriptFile

Specifies a JavaScript file to be loaded with this component.

```java
@JavascriptFile("product-list.js")
public class ProductListPagelet { ... }
```

- **Optional**.
- File must end with `.js`.
- The file is only loaded when the component is rendered.

---

## 🎨 @CSSFile (planned)

This annotation will allow you to specify CSS that only applies to a specific page.

```java
@CSSFile("product-style.css")
public class ProductPage { ... }
```

- Applies to `@Page` only.
- Not yet implemented but reserved for future versions.


---

## 🏠 @WelcomePage

Specifies the default page of the application if no other `@Page` mapping matches.

```java
@WelcomePage
@Page("home.html")
public class HomePage { ... }
```

- Only **one** class in the application should be annotated with `@WelcomePage`.
- Works only on classes also annotated with `@Page`.

---

## Summary

| Annotation        | Target         | value required | Notes |
|-------------------|----------------|----------------|-------|
| `@Pagelet`        | Pagelets       | ❌ (optional)  | Defaults to simple class name as ID |
| `@Page`           | Pages          | ✅ (required)  | Must end with `.html`, allows wildcards |
| `@HtmlFile`       | Pages/Pagelets | ❌ (optional)  | Defaults to class name + `.html` |
| `@JavascriptFile` | Pages/Pagelets | ❌ (optional)  | JS loaded when component rendered |
| `@CSSFile`        | Pages only     | ❌ (planned)   | Reserved for future use |

---

→ [Continue to Chapter 7: Response Types](07-responses.md)
