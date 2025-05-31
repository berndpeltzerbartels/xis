## 6. XIS Tags in HTML Templates

This section lists all custom `xis:` **tags** that extend the HTML language in XIS templates. These tags go beyond
simple attributes and define structural or semantic behaviors within the template.

Each tag has specific attributes that may be required or optional, and many of them support the use of Expression
Language (EL) within attribute values.

| Tag                      | Purpose                                          | Required Attributes           | Explanation                                                                                             | Example                                                        |
|--------------------------|--------------------------------------------------|-------------------------------|---------------------------------------------------------------------------------------------------------|----------------------------------------------------------------|
| `<xis:message>`          | Displays a validation message for a field        | `for`                         | Shows message if the field has an error. Alternative to `xis:message-for` attribute.                    | `<xis:message for="email"/>`                                   |
| `<xis:global-messages>`  | Displays global validation messages              | —                             | Typically used to render array-style error messages not bound to a specific field                       | `<xis:global-messages/>`                                       |
| `<xis:a>`                | Enhanced anchor tag supporting XIS actions/pages | Depends on use case           | Has attributes `action`, `page`, or `widget`. Also supports `target-container` when used with `widget`. | `<xis:a action="submit">Send</xis:a>`                          |
| `<xis:template>`         | Declares a pagelet (not a full HTML document)    | —                             | Ensures exactly one root element. Must **not** include `<html>`, `<head>`, or `<body>` tags.            | `<xis:template><div>...</div></xis:template>`                  |
| `<xis:foreach>`          | Iterates over items, like `<ul xis:foreach>`     | `value`                       | Similar to `xis:repeat`, but the tag itself is not repeated. Use format `var:array`.                    | `<ul xis:foreach="item:items">...</ul>`                        |
| `<xis:input>`            | Input field with binding                         | `binding`                     | Same as `<input xis:binding="..." />`, but in tag form. Useful for consistency or style.                | `<xis:input binding="price"/>`                                 |
| `<xis:form>`             | Form wrapper                                     | —                             | Must not use `action`. Use buttons or links for actions.                                                | `<xis:form>...</xis:form>`                                     |
| `<xis:submit>`           | Submit button inside a form                      | —                             | Equivalent to `<button type="submit">`. Often used with an `action` attribute.                          | `<xis:submit action="save">Save</xis:submit>`                  |
| `<xis:button>`           | Button with optional action                      | —                             | Can define a client or server-side action. EL-supported.                                                | `<xis:button action="reset">Reset</xis:button>`                |
| `<xis:widget-container>` | Placeholder for dynamically loaded widgets       | `id`                          | Declares a named slot that widgets can be rendered into. Also supports `default-widget`.                | `<xis:widget-container id="main" default-widget="StartPage"/>` |
| `<xis:parameter>`        | Declares a parameter inside links or widgets     | `name`, `value` or inner text | Used inside `<xis:a>` or similar tags to pass additional data.                                          | `<xis:parameter name="q" value="${value}"/>`                   |
| `<xis:if>`               | Conditional rendering                            | `condition`                   | Renders content only if the expression evaluates to true.                                               | `<xis:if condition="${user.loggedIn}">...</xis:if>`            |

---

### Notes

* All tags support the use of EL expressions inside their attribute values.
* The `<xis:a>` tag is context-sensitive:

    * If `action` is present, it triggers a controller method.
    * If `page` is present, it navigates to a new page.
    * If `widget` is present, it loads a pagelet (widget), and may include `target-container`.
* The `widget` attribute refers to the widget's ID, which is either defined via `@Widget("xyz")` or falls back to the
  Java class's simple name (i.e., the class name without package).
* When using `<xis:template>`, the template content must have a single root element and must **not** include `<html>`,
  `<head>`, or `<body>` tags. This ensures the fragment is well-formed and embeddable.
* The `xis:foreach` tag works similarly to `xis:repeat`, but the tag itself is not duplicated. Ideal use case is inside
  list containers like `<ul>`.
* `<xis:input>`, `<xis:form>` and others are structurally equivalent to their HTML counterparts using `xis:` attributes.
  For example: `<xis:input binding="price"/>` is equivalent to `<input xis:binding="price"/>`.
* `<xis:message>` and `<span xis:message-for="..."/>` are interchangeable in function but serve different
  structural/stylistic needs.
* `<xis:parameter>` can be used in three distinct contexts:

    1. Inside an action link: `<xis:a action="xyz"><xis:parameter name="id" value="${id}"/></xis:a>` →
       `@ActionParameter` in the controller method

       ```java
       @Action("xyz")
       public void handle(@ActionParameter("id") String id) { ... }
       ```
    2. Inside a page link: `<xis:a page="details"><xis:parameter name="tab" value="info"/></xis:a>` → `@QueryParameter`
       in a `@ModelData` or `@FormData` method of a PageController

       ```java
       @ModelData
       public PageModel data(@QueryParameter("tab") String tab) { ... }
       ```
    3. Inside a widget link: `<xis:a widget="InfoBox"><xis:parameter name="key" value="val"/></xis:a>` →
       `@WidgetParameter` in a `@ModelData` or `@FormData` method of a Pagelet/WidgetController

       ```java
       @ModelData
       public InfoModel info(@WidgetParameter("key") String key) { ... }
       ```
* The tag `<xis:action>` was intentionally omitted, as its behavior is not intuitive enough for typical usage.
* The attribute `xis:error-class` requires the presence of `xis:binding` on the same element.

We recommend that integration tests be written for cases involving `xis:message`, `xis:global-messages`, and `<xis:a>`
with `target-container`. These serve as critical runtime features.

[← XIS Attributes](05-attribute-table.md) | [Controller Methods →](07-controller-methods.md)