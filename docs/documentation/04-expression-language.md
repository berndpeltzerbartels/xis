## 4. Expression Language (EL) in Templates

XIS templates support a simple but powerful **expression language (EL)** syntax for dynamic rendering. Expressions are
wrapped in `${...}` and can be used in both:

* **Text content**
* **Attribute values**

This allows templates to be **data-driven** and **declarative**. Some data sources are also **reactive**, meaning
changes propagate to the DOM automatically (see below).

---

### Basic Syntax

Expressions follow a dotted path notation:

```html
<p>Hello, ${user.name}!</p>
```

The value `user.name` is resolved from the current model data, client state, or other exposed context variables.

---

### Supported Contexts

You can access variables from the following contexts:

* `@ModelData` methods (provided by the controller)
* `@FormData` objects
* `@ClientState`, `@LocalStorage`, `@QueryParameter`, etc.
* Built-in values like `validation.messages`, `validation.globalMessages`

Among these, **`clientState` and `localStorage` are reactive**: if their values change, the affected parts of the DOM
are re-rendered automatically.

---

### Example: Text Interpolation

```html
<h1>Welcome, ${user.firstName}!</h1>
```

If `@ModelData` provides:

```java

@ModelData
public User user() {
    return new User("Alice", "Smith");
}
```

Then the rendered HTML becomes:

```html
<h1>Welcome, Alice!</h1>
```

---

### Example: Attribute Binding

```html
<input type="text" value="${user.email}"/>
```

You can also bind custom attributes like:

```html

<button xis:action="send" xis:if="${user.active}">Send</button>
```

---

### EL in Repeats

You can iterate over arrays or lists using `xis:repeat`, which takes the form `item:items`.

```html

<ul>
    <li xis:repeat="item:items">${item.label}</li>
</ul>
```

With:

```java

@ModelData
public List<Item> items() {
    return List.of(new Item("A"), new Item("B"));
}
```

Renders:

```html

<ul>
    <li>A</li>
    <li>B</li>
</ul>
```

You can also access a specific index of an array directly:

```html
<p>${items[0].label}</p>
```

To access the index inside a loop, XIS automatically provides a variable named `<item>Index`. In the example above, this
would be `itemIndex`:

```html

<ul>
    <li xis:repeat="item:items">${itemIndex + 1}: ${item.label}</li>
</ul>
```

This is useful for styling or numbering elements.

---

### Conditional Rendering with EL

```html

<div xis:if="${user.admin}">Admin Area</div>
```

You can also use the ternary operator:

```html
<p>${user.active ? 'Online' : 'Offline'}</p>
```

---

### Property Access and Operators

You can access properties using either dot or bracket syntax:

```html
<p>${user.name}</p>
<p>${user['name']}</p>
```

XIS EL supports standard JavaScript-style operators:

* Arithmetic: `+`, `-`, `*`, `/`, `%`
* Comparison: `==`, `!=`, `<`, `>`, `<=`, `>=`
* Logical: `&&`, `||`, `!`
* Conditional (ternary): `condition ? a : b`
* Parentheses for grouping: `a * (b + c)`

You do **not** need to learn a new DSL — just follow standard JavaScript/EL rules.

---

### LocalStorage and ClientState Examples

```html
<p>Last viewed product: ${localStorage.lastProduct}</p>
<p>Selected tab: ${clientState.selectedTab}</p>
```

These values are reactive — if they change, the DOM updates automatically.

> Note: Setting these values programmatically is described in a later chapter.

---

### Validation Messages

You can display field-specific or global validation messages using:

```html
<p>${validation.messages.firstName}</p>
<p>${validation.globalMessages[0]}</p>
```

This outputs server-side validation messages set by the backend.

Alternatively, you can use specialized tags for rendering:

```html

<xis:message for="firstName"/>
<xis:global-messages/>
```

You can also use the attribute version, which is useful inside standard tags:

```html
<span xis:message-for="firstName"></span>
```

> TODO: Check correct runtime behavior and add missing integration tests.

---

### Custom Functions

You can define your own functions in a JavaScript file named after your controller, e.g.:

```text
HelloPage.java → HelloPage.js
```

Those functions can then be used in templates:

```html
<p>${formatDate(user.birthdate)}</p>
```

The EL parser will evaluate `formatDate(...)` by calling the user-defined function if available.

---

### Technical Note: Evaluation Timing

Expressions are evaluated during rendering on the server and may be updated dynamically in the browser for reactive
state.

* Model and form data are static unless re-rendered
* `localStorage` and `clientState` are fully reactive and tracked by the framework

---

### Summary

* `${...}` expressions allow embedding dynamic values in HTML
* Used in both text and attributes
* Contexts include controller-provided data and client-side state
* `localStorage` and `clientState` trigger re-rendering automatically
* Ternary operator and array indexes are supported
* Custom functions can extend the syntax
* Inside repeats, a variable like `itemIndex` gives the current index
* Validation messages can be rendered directly or with tags

We recommend starting simple with `@ModelData` and progressing to advanced patterns.

---

### TODO

* Link to detailed documentation on custom JavaScript functions
* Move chapter earlier in the guide
* Clarify expression usage in styling and `xis:if`, `xis:action`, etc.
* Add examples for using validation messages with specialized tags and attributes

[Kapitel 03: HTML Integration Overview ←](03-html-integration-overview.md) | [Kapitel 05: Microfrontend Support →](05-microfrontend-support.md)