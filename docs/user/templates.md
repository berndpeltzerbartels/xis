# Template Syntax

XIS templates are plain HTML with XIS attributes, XIS tags, and expression language.

Pages use complete HTML documents. Frontlets use template fragments.

Many XIS template features can be written in two equivalent styles:

- Attribute syntax keeps the markup close to ordinary HTML and usually previews better in design tools.
- Element syntax uses explicit `xis:*` framework elements and can be preferable when you want a more XML-like template.

This documentation shows both styles where the choice matters. For a compact side-by-side reference, see
[Tags and attributes](tags-and-attributes.md).

## Expression Language

Expressions use `${...}`.

```html
<h1>${product.name}</h1>
<p>${product.description}</p>
<span>${count > 0 ? count + " items" : "No items"}</span>
```

Expressions can access model data, loop variables, action parameters, and selected framework context values.
They can be used in text content and in ordinary HTML attributes. XIS does not give HTML attributes such as `data-id`
special meaning; it only replaces the `${...}` expression in the attribute value.

```html
<li data-id="${product.id}">${product.name}</li>
```

The documented public EL surface is:

- literals: strings, numbers, booleans, `null`, and `undefined`
- model values, loop variables, action parameters, and selected framework context values
- property and index access with dot and bracket syntax
- arithmetic, comparison, boolean operators, parentheses, and ternary expressions
- built-in EL functions listed below, including nested function calls
- text interpolation and attribute interpolation with `${...}`

### Property and Index Access

Use dot notation for ordinary Java bean properties and bracket notation when the key or index is dynamic.

```html
<p>${user.name}</p>
<p>${user['name']}</p>
<p>${items[1]}</p>
<p>${items[index + 1]}</p>
<p>${lookup[selectedKey]}</p>
<p>${lookup[prefix + '_item'].label}</p>
```

Bracket expressions are useful when a map key is built from other model values. The result can be chained with normal
property access, as in `${lookup[prefix + '_item'].label}`.

### Operators and Conditions

Template expressions support arithmetic, comparison, boolean operators, parentheses, and ternary expressions.

```html
<p>${price * quantity}</p>
<p>${count > 0 ? count + " items" : "No items"}</p>
<section xis:if="items[1] == 'beta' && notEmpty(user.name)">
    Content
</section>
```

Useful examples:

```html
<p>${toUpperCase(user.name)}</p>
<p>${default(user.email, "No email")}</p>
<p>${round(order.total, 2)}</p>
<p>${formatDate(order.createdAt, "en-US")}</p>
```

Function parameters can be expressions, and function calls can be nested:

```html
<p>${defaultValue(user.displayName, toUpperCase(user.login))}</p>
<p>${join(split(tagsText, ","), " / ")}</p>
```

Keep complex business rules in Java `@ModelData` methods. Use template expressions for presentation logic.

## Built-in EL Functions

XIS includes a set of expression-language functions for common template work. These are built into the browser runtime.

String functions:

| Function | Use |
| --- | --- |
| `toUpperCase(str)` | Converts a string to uppercase. |
| `toLowerCase(str)` | Converts a string to lowercase. |
| `trim(str)` | Removes leading and trailing whitespace. |
| `substring(str, start, end)` | Returns part of a string. |
| `replace(str, search, replacement)` | Replaces the first matching string fragment. |
| `split(str, separator)` | Splits a string into an array. |
| `join(array, separator)` | Joins an array into a string. |

Collection and object functions:

| Function | Use |
| --- | --- |
| `length(value)` | Returns length or size for strings, arrays, maps, sets, or objects. |
| `size(value)` | Alias for `length(value)`. |
| `count(value)` | Alias for `length(value)`. |
| `empty(value)` | Returns `true` for `null`, empty strings, empty arrays, empty maps, or empty sets. |
| `isEmpty(value)` | Alias for `empty(value)`. |
| `notEmpty(value)` | Returns `true` for non-empty strings, arrays, maps, or sets. |
| `contains(container, value)` | Checks strings, arrays, or object values. |
| `keys(object)` | Returns object keys. |
| `values(object)` | Returns object values. |
| `hasKey(object, key)` | Checks whether an object owns a key. |
| `flatMap(value, path)` | Extracts nested values from one object or an array of objects. |
| `filter(array, property, value)` | Filters an array by property equality. |
| `arrayOf(first, last)` | Creates an integer array from `first` to `last`, inclusive. |

Number functions:

| Function | Use |
| --- | --- |
| `round(value)` | Rounds to the nearest integer. |
| `round(value, digits)` | Rounds to the given number of decimal places. |
| `floor(value)` | Rounds down. |
| `ceil(value)` | Rounds up. |
| `abs(value)` | Returns the absolute value. |
| `sum(...)` | Adds numeric values or numeric values inside arrays. |

Date and time functions:

| Function | Use |
| --- | --- |
| `formatDate(date, locale)` | Formats a date with medium date style. |
| `formatDateTime(date, locale)` | Formats date and time with medium date style and short time style. |
| `formatTime(date, locale)` | Formats time with short time style. |
| `year(date)` | Extracts the year. |
| `month(date)` | Extracts the month number, starting at `1`. |
| `day(date)` | Extracts the day of month. |
| `hour(date)` | Extracts the hour. |
| `minute(date)` | Extracts the minute. |

Fallback functions:

| Function | Use |
| --- | --- |
| `default(value, fallback)` | Returns `fallback` when `value` is `null`, an empty string, or an empty array. |
| `defaultValue(value, fallback)` | Alias for `default(value, fallback)`. |

Examples:

```html
<h2>${default(product.name, "Unnamed product")}</h2>
<p>${toUpperCase(user.lastName)}, ${user.firstName}</p>
<p>${count(products)} products</p>
<p xis:if="${notEmpty(products)}">Products are available.</p>
<p>Total: ${round(sum(order.lines), 2)}</p>
<p>Created: ${formatDateTime(order.createdAt, "en-US")}</p>
```

Custom EL functions are an advanced extension topic. See [Advanced topics](advanced/README.md) when you need
project-specific template functions.

## Iteration

Use `xis:foreach` to repeat the content of an element. Attribute syntax:

```html
<ul xis:foreach="product:${products}">
    <li>${productIndex + 1}. ${product.name}</li>
</ul>
```

In attribute syntax, the content of the host element is repeated. The host element itself is not repeated.

Element syntax:

```html
<ul>
    <xis:foreach var="product" array="${products}">
        <li data-id="${product.id}">${productIndex + 1}. ${product.name}</li>
    </xis:foreach>
</ul>
```

XIS adds an index variable by appending `Index` to the loop variable name. In the example above, the index is
`productIndex`.

Use element syntax or `xis:repeat` when attributes of the repeated element depend on the loop item:

```html
<li xis:repeat="product:${products}">
    ${product.name}
</li>
```

## Conditions

Attribute syntax:

```html
<p xis:if="${notEmpty(products)}">Products are available.</p>
<p xis:if="${empty(products)}">No products found.</p>
```

Element syntax:

```html
<xis:if condition="${notEmpty(products)}">
    <p>Products are available.</p>
</xis:if>
```

Expressions can often be written without `${...}` when the attribute expects an expression:

```html
<p xis:if="notEmpty(products)">Products are available.</p>
```

## Navigation

Use `xis:page` for XIS navigation. Attribute syntax:

```html
<a xis:page="/products.html">Products</a>
```

Element syntax:

```html
<xis:a page="/products.html">Products</xis:a>
```

Use `xis:action` for controller actions. Action Button:

```html
<button xis:action="save">Save</button>
```

Actionlink:

```html
<a xis:action="save">Save</a>
```

Framework action element:

```html
<xis:action action="save">Save</xis:action>
```

Pass parameters with `xis:parameter`:

```html
<button xis:action="delete">
    <xis:parameter name="productId" value="${product.id}"/>
    Delete
</button>
```

## Frontlets and Includes

Short frontlet tag:

```html
<xis:frontlet name="CartFrontlet"/>
```

Container attribute syntax:

```html
<div xis:frontlet-container="cart" xis:default-frontlet="CartFrontlet"></div>
```

Includes:

```html
<xis:include name="header"/>
```

Include attribute syntax:

```html
<header xis:include="header"></header>
```

Frontlet container element syntax:

```html
<xis:frontlet-container container-id="content" default-frontlet="DashboardFrontlet"/>
```

Attribute syntax:

```html
<main xis:frontlet-container="content" xis:default-frontlet="DashboardFrontlet"></main>
```

## Forms

Form fields use `xis:binding`.

Attribute syntax:

```html
<form xis:binding="user">
    <input type="text" xis:binding="firstName"/>
    <input type="email" xis:binding="email"/>
    <button type="submit" xis:action="saveUser">Save</button>
</form>
```

Framework element syntax:

```html
<xis:form binding="user">
    <xis:input type="text" binding="firstName"/>
    <xis:input type="email" binding="email"/>
    <xis:submit action="saveUser">Save</xis:submit>
</xis:form>
```

Validation messages:

```html
<div xis:message-for="email"></div>
<xis:global-messages/>
```

## Static Assets

Static resources such as CSS, images, and fonts belong under:

```text
src/main/resources/public/
```

Reference them without `/public`:

```html
<link rel="stylesheet" href="/css/app.css"/>
<img src="/images/logo.png" alt="Logo"/>
```

JavaScript needs special care in XIS because pages and frontlets are inserted dynamically in an SPA-style runtime. Avoid
adding ordinary `<script>` tags inside page and frontlet templates until the JavaScript integration for that use case is
documented.
