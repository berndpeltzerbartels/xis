# Tags and Attributes

XIS templates often support two equivalent ways to express the same framework behavior:

- Attribute syntax: add an `xis:*` attribute to ordinary HTML.
- Element syntax: use an explicit `xis:*` framework element.

Attribute syntax is usually better when you want the template to remain close to normal HTML and preview well in design
tools. Element syntax is useful when you prefer a more explicit XML-like structure or when the framework element reads
more clearly than an attribute on an existing element.

## Iteration

Attribute syntax repeats the content of the element:

```html
<ul xis:foreach="product:${products}">
    <li>${product.name}</li>
</ul>
```

Element syntax:

```html
<ul>
    <xis:foreach var="product" array="${products}">
        <li>${product.name}</li>
    </xis:foreach>
</ul>
```

`xis:repeat` repeats the element itself:

```html
<li xis:repeat="product:${products}">
    ${product.name}
</li>
```

## Conditions

Attribute syntax:

```html
<section xis:if="${notEmpty(products)}">
    <h2>Products</h2>
</section>
```

Element syntax:

```html
<xis:if condition="${notEmpty(products)}">
    <section>
        <h2>Products</h2>
    </section>
</xis:if>
```

## Page Links

Attribute syntax:

```html
<a xis:page="/products.html">Products</a>
```

Element syntax:

```html
<xis:a page="/products.html">Products</xis:a>
```

Buttons can use attribute syntax too:

```html
<button xis:page="/products.html">Products</button>
```

## Action Links and Buttons

Actionlink:

```html
<a xis:action="save">Save</a>
```

Action Button:

```html
<button xis:action="save">Save</button>
```

Element syntax:

```html
<xis:action action="save">Save</xis:action>
```

Parameters are expressed as child tags in all styles:

```html
<button xis:action="delete">
    <xis:parameter name="productId" value="${product.id}"/>
    Delete
</button>
```

## Frontlets

Short tag:

```html
<xis:frontlet name="CartFrontlet"/>
```

Attribute syntax:

```html
<div xis:frontlet="CartFrontlet"></div>
```

Container element syntax:

```html
<xis:frontlet-container container-id="main" default-frontlet="ProductListFrontlet"/>
```

Container attribute syntax:

```html
<main xis:frontlet-container="main" xis:default-frontlet="ProductListFrontlet"></main>
```

## Includes

Element syntax:

```html
<xis:include name="header"/>
```

Attribute syntax:

```html
<header xis:include="header"></header>
```

## Forms

Attribute syntax:

```html
<form xis:binding="user">
    <input type="text" xis:binding="firstName"/>
    <input type="email" xis:binding="email"/>
    <button type="submit" xis:action="saveUser">Save</button>
</form>
```

Element syntax:

```html
<xis:form binding="user">
    <xis:input type="text" binding="firstName"/>
    <xis:input type="email" binding="email"/>
    <xis:submit action="saveUser">Save</xis:submit>
</xis:form>
```

## Validation Messages

Attribute syntax:

```html
<div xis:message-for="email"></div>
```

Element syntax:

```html
<xis:message message-for="email"/>
```

Global messages:

```html
<xis:global-messages/>
```

## Quick Reference

| Behavior | Attribute syntax | Element syntax |
| --- | --- | --- |
| Iterate content | `xis:foreach="item:${items}"` | `<xis:foreach var="item" array="${items}">` |
| Repeat element | `xis:repeat="item:${items}"` | use `<xis:foreach>` around the element |
| Conditional rendering | `xis:if="${condition}"` | `<xis:if condition="${condition}">` |
| Page link | `<a xis:page="/page.html">` | `<xis:a page="/page.html">` |
| Actionlink | `<a xis:action="save">` | `<xis:action action="save">` |
| Action Button | `<button xis:action="save">` | none needed |
| Frontlet | `<div xis:frontlet="CartFrontlet">` | `<xis:frontlet name="CartFrontlet">` |
| Frontlet container | `xis:frontlet-container="main"` | `<xis:frontlet-container container-id="main">` |
| Include | `xis:include="header"` | `<xis:include name="header">` |
| Form | `<form xis:binding="user">` | `<xis:form binding="user">` |
| Input | `<input xis:binding="email">` | `<xis:input binding="email">` |
| Submit | `<button xis:action="save">` | `<xis:submit action="save">` |
| Field message | `xis:message-for="email"` | `<xis:message message-for="email">` |
