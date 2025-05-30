# 09. Controller Methods

This chapter describes how you can structure your controller logic in XIS. XIS follows a declarative, annotation-based
approach, where the method signatures and annotations determine the behavior, lifecycle, and binding of controller
methods.

---

## 🧠 Conceptual Overview

In XIS, controller methods can serve different purposes:

- Provide data for rendering (via `@ModelData`)
- Provide form data (via `@FormData`)
- Handle form input (via `@FormData`)
- React to UI events (via `@Action`)
- Provide values for template variables (`@RequestScope`, etc.)

---

## 🚀 Action Methods

Annotated with `@Action`, these methods execute logic when a user interacts with an element in the template.

```java

@Action
public void delete(@ActionParameter("id") String id) {
    productService.deleteById(id);
}
```

### ✅ Template Syntax

Action methods require all needed parameters to be explicitly passed using `<xis:parameter>`:

```html
<a xis:action="deleteProduct" id="delete-link">
    Delete
    <xis:parameter name="id" value="${product.id}"/>
</a>
```

---

## 📦 Providing Data: @ModelData

Methods annotated with `@ModelData` provide data that will be available for rendering the template.

```java

@ModelData
public List<Product> products() {
    return productService.findAll();
}
```

### 🔍 Template usage:

```html

<xis:foreach array="products" var="product">
    <div>${product.name}</div>
</xis:foreach>
```

---

## 📝 Forms with @FormData and @Action

Use `@FormData` to bind input values to a model object, and `@Action` to handle the form submission.

```java

@FormData
public Product productForm() {
    return new Product();
}

@Action
public void save(@FormData Product product) {
    productService.save(product);
}
```

### 💡 Template

```html

<form xis:binding="product" method="save">
    <input type="text" xis:binding="name"/>
    <input type="text" xis:binding="price"/>
    <button xis:action="save">Save</button>
</form>
```

---

## 🧩 Parameter Binding Overview

| Annotation          | Description                                        |
|---------------------|----------------------------------------------------|
| `@ActionParameter`  | For action-triggered parameter passing             |
| `@FormData`         | For automatic model binding from form inputs       |
| `@ModelData`        | For populating the template before rendering       |
| `@RequestScope`     | For request-scoped helper methods                  |
| `@PageletParameter` | For declarative parameters passed between Pagelets |
| `@URLParameter`     | Bound to values from URL path (`/foo/{id}`)        |
| `@QueryParameter`   | Bound to values from query string (`?id=42`)       |
| `@ClientId`         | Injects current client ID                          |
| `@UserId`           | Injects current user ID from JWT token             |

`UserContext` does not require annotation, as it is automatically injected. It contains local user information like zone
and locale.

---

These examples demonstrate the most common interaction patterns between templates and controller logic.
