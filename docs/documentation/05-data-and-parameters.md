# 5. Data & Parameters

In XIS, controller methods are annotated to provide data to the frontend, handle user input, or define dependencies. All
data flow is explicit and handled through method-level annotations—there are no annotated fields.

This chapter introduces the core annotations used to bind data between the backend and your templates.

---

## 1. Controller Methods

XIS supports three main types of controller methods:

### 📦 @ModelData

Used to provide data for rendering a page or pagelet.

```java

@ModelData
public Product product() {
    return productService.load(id);
}
```

---

### 📝 @FormData

Handles form submissions by extracting and binding posted values.

```java

@FormData
public ProductForm submittedForm() {
    return FormParser.parse(ProductForm.class);
}
```

---

### 🔄 @RequestScope

Provides transient data, dependencies, or helper values needed during rendering.

```java

@RequestScope("today")
public LocalDate today() {
    return LocalDate.now();
}
```

`@RequestScope` methods can be injected into `@ModelData` or `@FormData` methods via parameters.

```java

@FormData
public SomeData processForm(@RequestScope("today") LocalDate today) {
    ...
}
```

---

## 2. Method Parameters

All parameters must be passed through annotated method parameters—no field injection is used.

### 🌐 @URLParameter

Used for values defined in the page path (e.g., `/product/42`).

```java

@ModelData
public Product loadProduct(@URLParameter("id") long id) {
    return productService.load(id);
}
```

### ❓ @QueryParameter

Used for values passed via query strings (e.g., `?lang=en`).

```java

@RequestScope
public String language(@QueryParameter("lang") String lang) {
    return lang != null ? lang : "en";
}
```

### 📩 @PageletParameter (formerly @Parameter)

Used for values passed from links or pagelet switches via `param-*` attributes.

```java

@ModelData
public Product loadProduct(@PageletParameter("id") long id) {
    return productService.load(id);
}
```

In templates:

```html

<xis:link to="EditPagelet" param-id="${product.id}"/>
```

> Pagelets can use both `@PageletParameter` and `@URLParameter`, depending on where the data comes from.

---

### ⚙️ @ActionParameter

Used to pass dynamic parameters into actions triggered by user interaction.

```java
public void updateName(@ActionParameter("name") String name) {
    ...
}
```

This is especially useful for buttons or other UI elements triggering backend methods with custom input.

---

### 🙋 @UserId

Injects the current user's ID, typically extracted from a JWT or session context.

```java

@ModelData
public List<Product> myProducts(@UserId String userId) {
    return productService.loadForOwner(userId);
}
```

### 🧭 @ClientId

Injects the ID of the current client instance (browser session).

```java

@RequestScope
public String clientSpecificState(@ClientId String clientId) {
    return "...";
}
```

### 👤 UserContext (Full Object)

Alternatively, inject the full context:

```java

@ModelData
public String greeting(UserContext ctx) {
    return ctx.getLocale().getLanguage().equals("de") ? "Hallo" : "Hello";
}
```

Structure:

```java
public interface UserContext {
    Locale getLocale();

    ZoneId getZoneId();

    String getUserId();

    String getClientId();
}
```

---

## Summary of Annotations

| Purpose               | Annotation          | Scope           |
|-----------------------|---------------------|-----------------|
| Provide data          | `@ModelData`        | Pages, Pagelets |
| Form handling         | `@FormData`         | Pages, Pagelets |
| Request dependencies  | `@RequestScope`     | Pages, Pagelets |
| URL path parameter    | `@URLParameter`     | Pages, Pagelets |
| URL query parameter   | `@QueryParameter`   | Pages, Pagelets |
| Link parameter        | `@PageletParameter` | Pagelets        |
| Action-specific input | `@ActionParameter`  | Pages, Pagelets |
| User ID               | `@UserId`           | Pages, Pagelets |
| Client ID             | `@ClientId`         | Pages, Pagelets |
| Context (full)        | `UserContext`       | Pages, Pagelets |

---

→ [Continue to Chapter 6: Client State & Local Storage](06-clientstate.md)
