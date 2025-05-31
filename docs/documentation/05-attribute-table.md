## 5. XIS Attributes in HTML Templates

XIS extends standard HTML with a number of special attributes, all prefixed with `xis:`. These attributes allow HTML
templates to bind to controller data, react to user actions, show validation messages, conditionally render elements,
and more.

This table lists all `xis:` **attributes** that are valid inside standard HTML tags.

> **Note:** This table **excludes special `xis:` tags** like `<xis:message>` or `<xis:global-messages>`. Those are
> documented separately.

| Attribute              | Purpose                                                    | Explanation                                                                                                   | Example                                                               |
|------------------------|------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------|
| `xis:binding`          | Binds a form input or label to a field                     | Used in `<input>`, `<select>`, `<textarea>`, and also `<label>` to indicate the target field                  | `<input xis:binding="price" />`                                       |
|                        |                                                            | If used on a `<label>`, XIS links it to the field by matching the binding name                                | `<label xis:binding="price">Price</label>`                            |
| `xis:action`           | Triggers a controller action                               | The string value names a controller method                                                                    | `<button xis:action="save">Save</button>`                             |
| `xis:repeat`           | Loops over items in a list or array                        | Value must be of the form `item:items`, where `item` is the loop variable and `items` is a key                | `<li xis:repeat="item:products">...</li>`                             |
|                        |                                                            | Automatically provides `${itemIndex}` for current index                                                       | `<li xis:repeat="item:products">${itemIndex + 1}: ${item.label}</li>` |
| `xis:foreach`          | Iterates over an array, but does not repeat the tag itself | Best used on container elements like `<ul>` to wrap multiple children                                         | `<ul xis:foreach="item:products"> <li>${item.label}</li> </ul>`       |
| `xis:if`               | Conditional rendering                                      | Renders element only if the expression resolves truthy                                                        | `<div xis:if="${user.admin}">...</div>`                               |
| `xis:error-class`      | Adds a class if validation fails                           | Requires `xis:binding` on the same tag                                                                        | `<label xis:binding="price" xis:error-class="error">`                 |
| `xis:message-for`      | Renders a validation message inline                        | Optional alternative to using `<xis:message>`                                                                 | `<span xis:message-for="email"></span>`                               |
| `xis:default-widget`   | Declares which child is shown by default                   | Used within a widget container element                                                                        | `<div xis:default-widget="MyForm"></div>`                             |
| `xis:widget-container` | Marks an element that can contain widgets                  | Usually used with `xis:default-widget` or `container-id` to support dynamic placement                         | `<div xis:widget-container="mainArea"></div>`                         |
| `xis:page`             | Navigates to a page with a URL                             | Used in `<a>`; value may include EL variables and maps to a controller page                                   | `<a xis:page="/products/${id}">More</a>`                              |
| `xis:widget`           | Loads a widget (pagelet) by ID                             | Used in `<a>`; ID refers to a pagelet annotated with `@Widget("id")` or class name fallback (i.e. SimpleName) | `<a xis:widget="MyWidget">Open</a>`                                   |
| `xis:target-container` | Target container for pagelet updates                       | Used in `<a>` with `xis:widget`; names the container to update                                                | `<a xis:widget="MyWidget" xis:target-container="/test">Link</a>`      |

---

### Explanation of `xis:repeat` Syntax

The value for `xis:repeat` must always follow the pattern:

```text
variableName:dataKey
```

Example:

```html

<li xis:repeat="item:items">${itemIndex + 1}: ${item.name}</li>
```

This means: for each element in `items`, make it available as `item`. A variable `${itemIndex}` is also automatically
available inside the loop.

Note: `xis:foreach` follows the same syntax but is intended for use on a wrapping element (like `<ul>`) and does not
repeat the tag it is attached to.

---

### Notes

* `xis:error-class` must be used **in combination with** `xis:binding`.
* `xis:binding` supports all form elements **and** labels.
* `xis:message-for` is available both as a tag (`<xis:message>`) and an attribute.
* `xis:widget` and `xis:page` are used only inside `<a>` elements.
* `xis:target-container` is only meaningful in conjunction with `xis:widget`.
* `xis:repeat` and `xis:foreach` are syntactically similar but differ in behavior.
* Reactive contexts like `localStorage` or `clientState` can appear inside EL expressions used in attributes.

A separate table will describe **tags** like `<xis:message>`, `<xis:template>`, and others.

[Kapitel 04: Expression Language ←](04-expression-language.md) | [Kapitel 06: XIS Tags in HTML Templates →](06-xis-tags-table.md)