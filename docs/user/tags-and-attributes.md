# Tags and Attributes

XIS templates are HTML plus framework attributes, framework elements, and expression language. Most framework behavior
has an attribute form and an element form.

- Attribute syntax adds an `xis:*` attribute to ordinary HTML. It keeps templates close to browser-previewable HTML.
- Element syntax uses explicit `xis:*` framework elements. It is useful when you prefer XML-like structure or need a
  wrapper that does not become part of the final HTML.

Expression language also works in ordinary HTML attributes. For example, `data-id="${product.id}"` writes a normal
HTML `data-id` attribute. XIS does not interpret `data-*` attributes; they remain available for browser APIs, CSS, or
your own JavaScript.

## Conditions

Use `xis:if` to conditionally render an element.

Attribute syntax wraps the host element in the condition:

```html
<section xis:if="notEmpty(products)">
    <h2>Products</h2>
</section>
```

Element syntax renders the children only when the condition is true:

```html
<xis:if condition="notEmpty(products)">
    <section>
        <h2>Products</h2>
    </section>
</xis:if>
```

The condition is an EL expression. You can write `${...}`, but attributes that expect an expression usually do not need
the wrapper.

## Iteration

Use `xis:foreach` to repeat the content of an element.

```html
<ul xis:foreach="product:products">
    <li>${productIndex + 1}. ${product.name}</li>
</ul>
```

The host element is created once; only its content is repeated. XIS adds an index variable by appending `Index` to the
loop variable name. In the example above, the index is `productIndex`.

Element syntax:

```html
<ul>
    <xis:foreach var="product" array="products">
        <li data-id="${product.id}">${product.name}</li>
    </xis:foreach>
</ul>
```

Use `xis:repeat` when the repeated element itself needs item-specific attributes:

```html
<li xis:repeat="product:products" data-id="${product.id}">
    ${product.name}
</li>
```

## Page Navigation

Use `xis:page` for client-side XIS page navigation.

```html
<a xis:page="/products.html">Products</a>
<button xis:page="/products.html">Products</button>
```

Element syntax:

```html
<xis:a page="/products.html">Products</xis:a>
```

Plain page links and buttons navigate directly. They do not call a server-side action.

## Actions

Use `xis:action` to call an `@Action` method.

```html
<a xis:action="delete">Delete</a>
<button xis:action="save">Save</button>
```

Element syntax for an action link:

```html
<xis:action action="save">Save</xis:action>
```

`xis:action` is supported on action links and action buttons. Outside a form, the action is a page or frontlet action.
Inside a form, it submits the form with that action.

Pass action parameters with child tags:

```html
<button xis:action="delete">
    <xis:parameter name="productId" value="${product.id}"/>
    Delete
</button>
```

## Frontlets

Use frontlet containers to host replaceable frontlet content.

```html
<main xis:frontlet-container="main" xis:default-frontlet="ProductListFrontlet"></main>
```

Element syntax:

```html
<xis:frontlet-container container-id="main" default-frontlet="ProductListFrontlet"/>
```

Short frontlet include syntax creates a container whose default frontlet is the given frontlet:

```html
<xis:frontlet name="CartFrontlet"/>
```

Load another frontlet into a container with `xis:frontlet` and `xis:target-container`:

```html
<a xis:frontlet="ProductDetailsFrontlet" xis:target-container="main">
    Details
</a>

<button xis:frontlet="ProductDetailsFrontlet" xis:target-container="main">
    Details
</button>
```

Element syntax:

```html
<xis:a frontlet="ProductDetailsFrontlet" target-container="main">
    Details
</xis:a>
```

Pass frontlet parameters with `xis:parameter`. The target frontlet receives them with `@FrontletParameter`.

```html
<a xis:frontlet="ProductDetailsFrontlet" xis:target-container="main">
    <xis:parameter name="productId" value="${product.id}"/>
    Details
</a>
```

## Includes

Use includes for shared static markup fragments.

```html
<header xis:include="header"></header>
```

Element syntax:

```html
<xis:include name="header"/>
```

Includes are for markup reuse. Use frontlets when the fragment needs its own controller, model data, or actions.

## Forms

Use `xis:binding` on a form to load and submit form data.

```html
<form xis:binding="product">
    <input type="text" xis:binding="name"/>
    <textarea xis:binding="description"></textarea>
    <select xis:binding="category">
        <option value="${category.id}" xis:repeat="category:categories">${category.name}</option>
    </select>
    <button xis:action="save">Save</button>
</form>
```

Element syntax:

```html
<xis:form binding="product">
    <xis:input type="text" binding="name"/>
    <xis:textarea binding="description"/>
    <xis:select binding="category">
        <option value="${category.id}" xis:repeat="category:categories">${category.name}</option>
    </xis:select>
    <xis:submit action="save">Save</xis:submit>
</xis:form>
```

Checkbox and radio fields are ordinary form controls with `xis:binding`:

```html
<input type="checkbox" xis:binding="active"/>
<input type="radio" xis:binding="status" value="DRAFT"/>
```

Element syntax exists as a convenience:

```html
<xis:checkbox binding="active"/>
<xis:radio binding="status" value="DRAFT"/>
```

## Validation Messages

Use `xis:message-for` for field messages.

```html
<span xis:message-for="name"></span>
```

Element syntax:

```html
<xis:message message-for="name"/>
```

Use global messages for validation errors that are not attached to one field:

```html
<xis:global-messages/>
```

Use `xis:error-class` together with `xis:binding` when a field or label should receive a CSS class on validation error:

```html
<input xis:binding="name" xis:error-class="error"/>
<label for="name" xis:binding="name" xis:error-class="error">Name</label>
```

## Dynamic Classes

Use `xis:selection-class` and `xis:selection-group` for selection styling when XIS should manage the selected CSS class.

```html
<button xis:selection-group="tabs" xis:selection-class="active">
    Details
</button>
```

## Client State

Client-side storage binding is available, but it is not required for normal server-backed applications. Use it only when
state should intentionally live in the browser.

```html
<section xis:storage-binding="localStorage">
    <span>${localStorage.cart.count}</span>
</section>
```

Supported stores are `localStorage`, `sessionStorage`, and `clientStorage`.

## Raw Content

`xis:raw` inserts its content without normal XIS handling. Use it rarely, for content that is already trusted and already
prepared as HTML.

```html
<xis:raw>
    <strong>Trusted HTML</strong>
</xis:raw>
```

Use `text="true"` to insert the raw content as plain text:

```html
<xis:raw text="true"><strong>Shown as text</strong></xis:raw>
```

## Quick Reference

| Behavior | Attribute syntax | Element syntax |
| --- | --- | --- |
| Conditional rendering | `xis:if="condition"` | `<xis:if condition="condition">` |
| Iterate content | `xis:foreach="item:items"` | `<xis:foreach var="item" array="items">` |
| Repeat element | `xis:repeat="item:items"` | use `<xis:foreach>` around the element |
| Page link | `<a xis:page="/page.html">` | `<xis:a page="/page.html">` |
| Page button | `<button xis:page="/page.html">` | none needed |
| Action link | `<a xis:action="save">` | `<xis:action action="save">` |
| Action button | `<button xis:action="save">` | none needed |
| Parameter | `<xis:parameter name="id" value="${id}">` | same |
| Frontlet link | `xis:frontlet="DetailsFrontlet"` | `<xis:a frontlet="DetailsFrontlet">` |
| Frontlet container | `xis:frontlet-container="main"` | `<xis:frontlet-container container-id="main">` |
| Default frontlet | `xis:default-frontlet="ListFrontlet"` | `default-frontlet="ListFrontlet"` |
| Include | `xis:include="header"` | `<xis:include name="header">` |
| Form | `<form xis:binding="product">` | `<xis:form binding="product">` |
| Input | `<input xis:binding="name">` | `<xis:input binding="name">` |
| Textarea | `<textarea xis:binding="text">` | `<xis:textarea binding="text">` |
| Select | `<select xis:binding="category">` | `<xis:select binding="category">` |
| Checkbox | `<input type="checkbox" xis:binding="active">` | `<xis:checkbox binding="active">` |
| Radio | `<input type="radio" xis:binding="status">` | `<xis:radio binding="status">` |
| Submit | `<button xis:action="save">` | `<xis:submit action="save">` |
| Field message | `xis:message-for="name"` | `<xis:message message-for="name">` |
| Global messages | none | `<xis:global-messages>` |
| Error class | `xis:error-class="error"` | none |
| Storage binding | `xis:storage-binding="localStorage"` | `<xis:storage-binding store="localStorage">` |
| Raw content | none | `<xis:raw>` |
