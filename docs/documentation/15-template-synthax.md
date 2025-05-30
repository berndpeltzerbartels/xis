## 15. Template Syntax and Normalization

In XIS, you can choose between two syntactic styles when using custom attributes and tags in your HTML templates. Both
styles are functionally equivalent and are normalized internally to a common representation before processing.

### Attribute Style vs. Tag Style

You can write:

```html

<xis:form binding="myForm">
```

… or:

```html

<form xis:binding="myForm">
```

This is true for most XIS tags: any custom element like `<xis:xyz>` can usually be expressed as a standard HTML tag with
`xis:xyz` as an attribute.

* **Element style** is XML-compliant, which may be useful in tools that validate well-formed XML.
* **Attribute style** is better supported in WYSIWYG HTML editors.

The XIS `DomNormalizer` is responsible for rewriting everything into a unified form. As a result, internal logic (like
action handling, parameter binding etc.) only needs to support one version.

### Special Attributes

Some attributes do not correspond to standard HTML but are used to provide behavior or configuration to handlers:

* `xis:parameter`
* `xis:repeat`
* `xis:default-widget`
* `xis:widget-container`
* `xis:if`
* … and more

These attributes often accept values containing variables like `${value}` or expressions like `true`, `false`, or
literal strings.

### Examples

#### Form Binding

```html
<!-- Attribute style -->
<form xis:binding="myForm">
```

```html
<!-- Tag style -->
<xis:form binding="myForm">
```

#### Action Binding

```html

<button xis:action="saveData">Save</button>
```

#### Repeat Example

```html

<ul>
    <li xis:repeat="item:items">${item}</li>
</ul>
```

#### Conditional Display

```html
<!-- Attribute style -->
<div xis:if="${user.loggedIn}">
    Welcome back!
</div>

<!-- Tag style -->
<xis:if test="${user.loggedIn}">
    <div>Welcome back!</div>
</xis:if>
```

#### Validation Error Display

```html

<div>
    <input type="text" xis:binding="price" id="price"/>
    <label xis:for="price" xis:error-class="error">Price</label>
    <div>${validation.messages.price}</div>
</div>
```

#### Parameter Binding

```html
<!-- Form action parameter -->
<xis:parameter name="fixed" value="true"/>

<!-- Widget link -->
<xis:widget name="MyWidget">
    <xis:parameter name="query" value="${query}"/>
</xis:widget>

<!-- Action link with query -->
<a xis:action="doSomething">
    <xis:parameter name="search" value="${searchTerm}"/>
</a>
```

##### Example Controller

```java

@Action
public void doSomething(@Parameter("search") String term) {
    // term contains the value of the parameter passed via xis:parameter
}
```

### Supported Tags and Attributes (work in progress)

This list will be expanded based on the handlers defined in the codebase (see `handler.zip`, `NodeDecorator.js`).

#### Pure Attributes

| Attribute              | Description                                              |
|------------------------|----------------------------------------------------------|
| `xis:action`           | Binds an action method on user interaction               |
| `xis:parameter`        | Adds a fixed/query/widget parameter depending on context |
| `xis:repeat`           | Repeats element for each item in a collection            |
| `xis:if`               | Conditionally renders element                            |
| `xis:widget-container` | Declares a container for dynamically inserted widgets    |
| `xis:default-widget`   | Marks the default widget inside a container              |
| `xis:for`              | Used with `label`, equivalent to HTML `for`              |
| `xis:error-class`      | Adds class if associated field has validation errors     |

#### Tag + Attribute Equivalents

| Tag Style         | Equivalent Attribute Style         |
|-------------------|------------------------------------|
| `<xis:form>`      | `<form xis:binding="...">`         |
| `<xis:if>`        | `<div xis:if="...">`               |
| `<xis:parameter>` | `<div xis:parameter="...">` or tag |

#### Pure Tags

| Tag             | Description                             |
|-----------------|-----------------------------------------|
| `xis:foreach`   | Internal normalized loop representation |
| `xis:message`   | Shows single validation message         |
| `xis:messages`  | Shows global validation messages        |
| `xis:parameter` | Explicit parameter declaration          |

This table will be expanded to include \~20+ handlers once fully extracted from the JavaScript logic.

---

### TODOs

* Add all handler-based attribute/tag mappings from `handler.zip`
* Document edge cases and syntax exceptions
* Include more examples, especially with nested structures or advanced parameters
* Add `xis:if` handler as dual tag/attribute
* Explain the dual role of `xis:parameter` depending on context
* Add more controller-side Java examples
