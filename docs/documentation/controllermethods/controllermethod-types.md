# Controller Method Types

XIS controllers can contain different kinds of annotated methods, each serving a specific purpose. This page gives a
structured overview of all supported method types and their roles in a page controller.

---

## `@ModelData`

Declares data that is passed to the HTML template. Multiple methods with this annotation may exist in a controller. The
returned data objects are merged into the rendering context.

```java

@ModelData
public Product product(@RequestScope Product product) {
    return product;
}
```

If the annotation has a value, the return value is stored under that key:

```java

@ModelData("state.title")
public String pageTitle() {
    return "Hello World";
}
```

---

## `@FormData`

Used to populate form fields in the initial GET request. The returned data can be a form bean or an object from the
database. Called only on GET, not on form submission.

```java

@FormData
public Product product(@RequestScope Product product) {
    return product;
}
```

This is typically used to preload forms when editing existing entities.

---

## `@Action`

Declares a method that handles a user-triggered action, either from a form or a link. The method name or annotation
value must match the `xis:action` in the HTML.

```java

@Action("save")
public WidgetResponse save(ProductForm form) {
    // process and return a widget response
}
```

`@Action` methods can:

- Be triggered by forms or action links
- Accept parameters (e.g. form beans, request values)
- Return `void`, `String`, `Redirect`, or `WidgetResponse`

---

## `@RequestScope`

Marks a method that provides a value to other methods during the same HTTP request. The value is evaluated once and
reused.

```java

@RequestScope
public Product product(@UrlParameter("id") long id) {
    return productService.load(id);
}
```

This is useful for avoiding redundant database access.  
Values from `@RequestScope` can be injected into other controller methods.

---

## Client-Side State Methods

In addition to server-side state, XIS also supports reading reactive client-side state into controller methods.

### `@LocalStorage`

User for methods, the return value will get stored in the browser's `localStorage`. It can be used standalone or
additional to other method annotations.
This is a reactive variable on the
client side. The value is updated on any component when this method was called.

Used as a parameter, it declares that the value should be read from the browser's `localStorage`. This works both on
parameters and methods.
Useful for persisting settings, filters, or cached UI state across sessions.

```java

@ModelData("counter")
@LocalStorage("counter")
public Integer nameFromClient(@LocalStorage("counter") Integer counter) {
    int c = counter == null ? 0 : counter;
    return ++counter;
}
```

### `@ClientState`

Similar to @LocalStorage, but the value is stored in the browser's `sessionStorage`. It can be used standalone or
additional to other method annotations.This is a reactive variable on the
client. The value is updated on any component when this method was called.

```java

@ModelData("state.filter")
public String currentFilter(@ClientState("filter") String filter) {
    return filter;
}
```

Both annotations are compatible with reactivity in HTML templates:

- `${state.xyz}` for `@ClientState`
- `${localStorage.xyz}` for `@LocalStorage`

These annotations can be used on their own or in combination with others such as `@ModelData`.
---

## Notes on Invocation Order

- All `@RequestScope` methods are evaluated first (sorted by dependencies)
- Then, `@ModelData`, `@FormData`, and other model-relevant methods
- If the request contains an action, the matching `@Action` method is invoked last

---

## Plain Methods (without annotations)

Methods without any annotations are **ignored** by the framework. They can still be called from within annotated
methods, but have no effect by themselves.

---

## Multiple Roles

A single method can have **more than one role** by combining multiple annotations. For example, a method can be both
`@RequestScope` and `@ModelData` to compute a value once per request and also expose it to the template. This helps
reduce redundancy and improves clarity.

---

## Further Reading

- [Actions (for pages and widgets)](../widgets/actions.md)

