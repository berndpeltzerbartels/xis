# Core Model

XIS applications are built from pages, frontlets, includes, model data, form data, and actions.

## Pages

A page is a Java controller annotated with `@Page`. It maps to a browser URL and renders a complete HTML document.

```java
package example.products;

import one.xis.ModelData;
import one.xis.Page;
import one.xis.PathVariable;

@Page("/products/{id}.html")
public class ProductPage {

    private final ProductService productService;

    public ProductPage(ProductService productService) {
        this.productService = productService;
    }

    @ModelData
    public Product product(@PathVariable("id") long id) {
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
public List<Product> products() {
    return productService.findAll();
}
```

```html
<ul xis:foreach="product:${products}">
    <li>${product.name}</li>
</ul>
```

The method name is the default model key. If a method is named `products`, the template can read `${products}`.

## Actions

`@Action` marks methods that can be called from the template.

```java
@Action
public Class<?> delete(@PathVariable("id") long id) {
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

See [Navigation and responses](navigation.md) for the complete navigation model.

## Parameters

Path variables from a page URL are available to model and action methods.

```java
@Page("/orders/{orderId}.html")
public class OrderPage {

    @ModelData
    public Order order(@PathVariable("orderId") long orderId) {
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
public class ProductSummaryFrontlet {

    @ModelData
    public String headline() {
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

or:

```html
<div xis:frontlet="ProductSummaryFrontlet"></div>
```

Frontlet IDs default to the simple class name. Use an explicit ID if two frontlets would otherwise have the same name:

```java
@Frontlet("AdminProductSummary")
public class ProductSummaryFrontlet {
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

Includes are reusable HTML fragments. Use them for static or mostly-presentational shared markup such as headers,
footers, and navigation.

```java
package example.layout;

import one.xis.Include;

@Include("header")
public class Header {
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

Choose includes when you need shared markup. Choose frontlets when you need controller logic, independent model data, or
dynamic updates.
