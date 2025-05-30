# 7. Response Types Of Action-Methods

In XIS, controller methods can return structured responses to update parts of the page or trigger navigation. This
chapter outlines the available response types and explains when and how to use them.

---

## ⚙️ Typical Use Cases

- **@Page controllers** typically return:
    - `PageResponse` or a page class (`Class<?>`) to trigger a full-page transition.
    - A fully-qualified class name as `String` (used for cloud-based microfrontend resolution – not yet implemented).
    - `void`, to keep the current page unchanged.

- **@Pagelet controllers** typically return:
    - `WidgetResponse` (alias PageletResponse) to update themselves or another container.
    - A page class to trigger a full page transition.
    - `void`, to keep the current content unchanged.

---

## 🔁 PageResponse

Used to transition to a new page.

```java
return new PageResponse(MyPage .class);
```

Optional parameters:

```java
return new PageResponse(MyPage .class)
    .pathVariable("id",42)
    .urlParameter("ref","search");
```

> ✅ When a `@Pagelet` controller returns a page, the container to insert it into is **clear and implied**.

> ❗ When a `@Page` controller returns a pagelet, **you must specify the container**, even if there’s only one. This
> simplifies the implementation and avoids ambiguous behavior.

---

## 🔄 WidgetResponse (PageletResponse)

Used to replace a pagelet or update parameters dynamically.

```java
return MyPagelet .class;
```

With parameters:

```java
return new WidgetResponse(MyPagelet.class)
    .widgetParameter("productId",42);
```

Replace content in another container:

```java
return new WidgetResponse(MyPagelet.class)
    .targetContainer("sidebar");
```

Reload a pagelet explicitly:

```java
return new WidgetResponse(MyPagelet.class)
    .reloadWidget(AnotherWidget.class);
```

> ✅ A `@Pagelet` returning another pagelet automatically replaces itself.
> ❗ To target a **different** container, use `.targetContainer(...)`.

---

## 🔄 Cross-Replacement: Rare Cases

Though rare, it's technically possible to:

- Have a `@Pagelet` controller return a page (navigation).
- Have a `@Page` controller return a pagelet (partial update) – this **requires** a container ID.

The container id must match the container-id-attribute of the widget-container.

```xml
    <xis:widget-container container-id="123"/>
```

---

## 🧭 String Class Name Returns (for Microfrontends)

For cloud-based microfrontend architectures, you might not have access to the class at compile time. In such cases,
you can return the fully qualified class name as a `String`:

```java
return"com.example.pages.MyPage";
```

---

## ✅ Summary

| Return Type             | From Controller     | Description                                |
|-------------------------|---------------------|--------------------------------------------|
| `PageResponse`          | `@Page`, `@Pagelet` | Triggers full-page navigation              |
| `WidgetResponse`        | `@Pagelet`          | Updates one or more pagelet containers     |
| `Class<?>`              | `@Pagelet`          | Triggers page transition                   |
| `String` (FQCN)         | `@Pagelet`          | Cross-project microfrontend loading (TODO) |
| `void`                  | Any                 | Retains current content                    |

---

→ [Continue to Chapter 8: Client State & Local Storage](08-client-state.md)
