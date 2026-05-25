# Core Model

[Documentation map](../README.md)

XIS applications are built from pages, frontlets, includes, model data, form data, and actions.

For a complete map of class, method, field, record-component, and parameter annotations, see the
[Annotation reference](annotations.md).

## Pages

A page is a Java controller annotated with `@Page`. It maps to a browser URL and renders a complete HTML document.
The programming model deliberately resembles older request/page models where a controller provides data and a template
uses it directly. At runtime, however, XIS still runs as a SPA-style DOM application in the browser: navigation, action
calls, DOM updates, and refresh behavior are handled by XIS instead of by handwritten REST clients or frontend state
code.

```java
package example.products;

import one.xis.ModelData;
import one.xis.Page;
import one.xis.PathVariable;

@Page("/products/{id}.html")
class ProductPage {

    private final ProductService productService;

    ProductPage(ProductService productService) {
        this.productService = productService;
    }

    @ModelData
    Product product(@PathVariable("id") long id) {
        return productService.findById(id);
    }
}
```

```html
<!DOCTYPE html>
<html xmlns:xis="https://xis.one/xsd" lang="en">
<head>
    <meta charset="UTF-8">
    <title>${product.name}</title>
</head>
<body>
    <h1>${product.name}</h1>
    <p>${product.description}</p>
</body>
</html>
```

Rules:

- `@Page` paths start with `/`.
- User-facing page URLs should end with `.html`.
- Page templates are complete HTML documents.
- By convention, `ProductPage.java` uses `ProductPage.html` in the same package.
- Page classes are discovered as framework components by supported runtimes.

## Model Data

`@ModelData` exposes values to the template.

```java
@ModelData
List<Product> products() {
    return productService.findAll();
}
```

```html
<ul xis:foreach="product:${products}">
    <li>${product.name}</li>
</ul>
```

The model key can be chosen in three ways:

```java
@ModelData("customer")
Customer selectedCustomer() {
    return customerService.currentCustomer();
}

@ModelData(varName = "customer")
Customer selectedCustomer() {
    return customerService.currentCustomer();
}

@ModelData
Customer customer() {
    return customerService.currentCustomer();
}

@ModelData
Customer getCustomer() {
    return customerService.currentCustomer();
}
```

All four variants expose `${customer}`. `varName` is an explicit alias for `value` when you prefer a more readable
attribute name in examples or application code. Do not set both to different values.

With no explicit annotation value, XIS uses the method name as the key. If the
method name starts with `get` followed by an uppercase character, XIS uses the property name instead, so `getCustomer()`
becomes `customer`. This also applies when the controller method has XIS parameters such as `@SharedValue`,
`@PathVariable`, or `@QueryParameter`.

`@ModelData` and `@FormData` methods may return `Optional<T>` or `Stream<T>`. XIS unwraps `Optional` values and
materializes streams immediately as lists during controller processing; a stream is not transported lazily to the
browser.

`@ModelData` is loaded both when a page or frontlet is opened and after actions by default. Use `load` when a value is
only meaningful in one phase:

```java
@ModelData(value = "selectedStepId", load = ModelDataLoad.INITIAL)
Long selectedStepId() {
    return pipelineService.firstStepId().orElse(null);
}

@ModelData(value = "summary", load = ModelDataLoad.AFTER_ACTION)
Summary summary() {
    return pipelineService.currentSummary();
}
```

`INITIAL` is useful for default selections such as the first tab, first row, or first pipeline step. `AFTER_ACTION` is
useful for values that should only be calculated after a user action. `ALWAYS` is the default.

## Actions

`@Action` marks methods that can be called from the template.

```java
@Action
Class<?> delete(@PathVariable("id") long id) {
    productService.delete(id);
    return ProductListPage.class;
}
```

```html
<button xis:action="delete">Delete</button>
```

Common return behavior:

- `void`: stay on the current page or frontlet and refresh model data
- page class: navigate to that page
- frontlet class or frontlet response: update a frontlet container
- `ModalResponse`: open or close a modal dialog

See [Navigation and responses](navigation.md) for the complete navigation model.

## Parameters

Path variables from a page URL are available to model and action methods.

```java
@Page("/orders/{orderId}.html")
class OrderPage {

    @ModelData
    Order order(@PathVariable("orderId") long orderId) {
        return orderService.findById(orderId);
    }
}
```

Action-specific parameters can be passed from templates with `xis:parameter`.

```html
<button xis:action="deleteLine">
    <xis:parameter name="lineId" value="${line.id}"/>
    Delete line
</button>
```

## Frontlets

A frontlet is a reusable UI controller annotated with `@Frontlet`. Frontlets render HTML fragments and can be embedded inside
pages or other frontlets.

```java
package example.products;

import one.xis.ModelData;
import one.xis.Frontlet;

@Frontlet
class ProductSummaryFrontlet {

    @ModelData
    String headline() {
        return "Products";
    }
}
```

`ProductSummaryFrontlet.html`

```html
<xis:template xmlns:xis="https://xis.one/xsd">
    <h2>${headline}</h2>
</xis:template>
```

Embed it:

```html
<xis:frontlet name="ProductSummaryFrontlet"/>
```

or with a named container:

```html
<div xis:frontlet-container="summary" xis:default-frontlet="ProductSummaryFrontlet"></div>
```

Frontlet IDs default to the simple class name. Use an explicit ID if two frontlets would otherwise have the same name:

```java
@Frontlet("AdminProductSummary")
class ProductSummaryFrontlet {
}
```

## Frontlet Containers

Frontlet containers allow parts of a page to be replaced independently.

```html
<xis:frontlet-container container-id="main" default-frontlet="ProductSummaryFrontlet"/>
```

Attribute syntax:

```html
<div xis:frontlet-container="main" xis:default-frontlet="ProductSummaryFrontlet"></div>
```

An action can target a container:

```html
<button xis:action="showDetails" target-container="main">
    <xis:parameter name="productId" value="${product.id}"/>
    Show details
</button>
```

## Includes

Includes are reusable HTML fragments. Use them for shared markup such as headers, footers, and navigation when the
fragment does not need an independent frontlet controller.

```java
package example.layout;

import one.xis.Include;

@Include("header")
class Header {
}
```

`Header.html`

```html
<header>
    <a xis:page="/index.html">Home</a>
    <a xis:page="/products.html">Products</a>
</header>
```

Use it:

```html
<xis:include name="header"/>
```

Attribute syntax is equivalent:

```html
<header xis:include="header"></header>
```

The included markup is initialized inside the surrounding page or frontlet. It may therefore contain XIS links, action
buttons, parameters, and model expressions that belong to that surrounding controller. Choose frontlets when the fragment
needs its own controller logic, independent model data, or dynamic updates.
