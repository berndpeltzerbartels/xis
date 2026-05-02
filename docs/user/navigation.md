# Navigation and Responses

Navigation is core XIS behavior. Pages, frontlets, links, forms, and actions all participate in the same navigation
model.

XIS supports deep links like a server-rendered application and smooth client-side transitions like an SPA. A user can
open `/products/42.html` directly in the browser, but links, buttons, and actions inside the application do not need to
reload the whole page.

Template features often exist in two syntaxes:

- Attribute syntax, such as `<a xis:page="/products.html">`. This keeps the template close to normal HTML and usually
  gives better previews in web design tools.
- Element syntax, such as `<xis:a page="/products.html">`. This is useful when you prefer explicit framework tags or a
  more XML-like style.

## Page Links

Use `xis:page` for direct navigation between XIS pages. This does not call a server-side action method.

```html
<a xis:page="/products.html">Products</a>
```

This is the recommended syntax for internal page navigation. XIS updates browser history and loads the destination
through its own client-side navigation flow.

A normal HTML link still works:

```html
<a href="/products.html">Products</a>
```

Use normal `href` links when you intentionally want ordinary browser behavior or when linking outside the XIS
application. Use `xis:page` for XIS page-to-page navigation.

Element syntax is also possible and normalizes to the same page-link behavior:

```html
<xis:a page="/products.html">Products</xis:a>
```

Prefer `xis:page` on normal anchors unless the element syntax is clearly more readable in a specific template.

Buttons can link to pages too:

```html
<button xis:page="/products.html">Products</button>
```

## Page URLs and Path Variables

A page declares its URL with `@Page`.

```java
package example.products;

import one.xis.ModelData;
import one.xis.Page;
import one.xis.PathVariable;

@Page("/products/{id}.html")
public class ProductPage {

    @ModelData
    public Product product(@PathVariable("id") long id) {
        return productService.findById(id);
    }
}
```

The URL `/products/42.html` loads `ProductPage` and passes `42` as `id`.

## Action Links and Buttons

Server-side action methods are triggered with `xis:action`. Outside forms there are two common action controls:

- Actionlink: an `<a>` with `xis:action`
- Action Button: a `<button>` with `xis:action`

Actionlink:

```html
<a xis:action="deleteProduct">
    <xis:parameter name="productId" value="${product.id}"/>
    Delete
</a>
```

Element syntax:

```html
<xis:action action="deleteProduct">
    <xis:parameter name="productId" value="${product.id}"/>
    Delete
</xis:action>
```

Action Button:

```html
<button xis:action="deleteProduct">
    <xis:parameter name="productId" value="${product.id}"/>
    Delete
</button>
```

```java
import one.xis.Action;
import one.xis.ActionParameter;

@Action
public void deleteProduct(@ActionParameter("productId") long productId) {
    productService.delete(productId);
}
```

`void` means: stay on the current page or frontlet and refresh the relevant model data.

Return types such as `PageResponse` and `FrontletResponse` are only evaluated after a server-side action invocation.
They are not used by plain page links or frontlet links.

## Form Actions

Forms use `xis:binding` for their data object and `xis:action` on the submit control.

```html
<form xis:binding="product">
    <input type="text" xis:binding="name"/>
    <button type="submit" xis:action="save">Save</button>
</form>
```

Framework element syntax is also available for form controls. It normalizes to normal HTML controls:

```html
<xis:form binding="product">
    <xis:input type="text" binding="name"/>
    <xis:submit action="save">Save</xis:submit>
</xis:form>
```

The action method can then return `void`, a page class, `PageResponse`, a frontlet class, or `FrontletResponse`.

## Return a Page Class

An action can return a page controller class.

```java
@Action
public Class<?> save(@FormData("product") ProductForm product) {
    productService.save(product);
    return ProductListPage.class;
}
```

Use this when the destination page does not need path variables or query parameters.

## Return `PageResponse`

Use `PageResponse` when the target page has path variables or query parameters.

```java
import one.xis.PageResponse;

@Action
public PageResponse openProduct(@ActionParameter("productId") long productId) {
    return PageResponse.of(ProductPage.class, "id", productId);
}
```

With query parameters:

```java
@Action
public PageResponse search(@FormData("search") SearchForm search) {
    return new PageResponse(ProductSearchPage.class)
            .queryParameter("q", search.query())
            .queryParameter("page", 1);
}
```

`PageResponse` is the explicit, type-oriented way to navigate to a known XIS page.

## Return a Page URL

For page actions, returning a string that resolves to a known XIS page URL can navigate to that page.

```java
@Action
public String openProductUrl(@ActionParameter("productId") long productId) {
    return "/products/" + productId + ".html";
}
```

Prefer `PageResponse` when the destination is a known page controller. It is easier to refactor and makes path variables
visible in Java.

## Return `PageUrlResponse`

Use `PageUrlResponse` when the action already has a concrete URL string.

```java
import one.xis.PageUrlResponse;

@Action
public PageUrlResponse openReport(@ActionParameter("reportId") String reportId) {
    return new PageUrlResponse("/reports/" + reportId + ".html");
}
```

Use this sparingly for application navigation. Prefer `PageResponse` for known pages.

## Frontlet Navigation

Frontlets are loaded into frontlet containers. A direct frontlet link or button changes the frontlet in a container
without calling an action method.

```html
<main xis:frontlet-container="main" xis:default-frontlet="ProductListFrontlet"></main>
```

A link can load another frontlet into a target container:

```html
<a xis:frontlet="ProductDetailsFrontlet" xis:target-container="main">
    <xis:parameter name="productId" value="${product.id}"/>
    Details
</a>
```

A button can do the same:

```html
<button xis:frontlet="ProductDetailsFrontlet" xis:target-container="main">
    <xis:parameter name="productId" value="${product.id}"/>
    Details
</button>
```

Framework element syntax:

```html
<xis:a frontlet="ProductDetailsFrontlet" target-container="main">
    <xis:parameter name="productId" value="${product.id}"/>
    Details
</xis:a>
```

The frontlet receives the parameter with `@FrontletParameter`.

```java
import one.xis.Frontlet;
import one.xis.FrontletParameter;
import one.xis.ModelData;

@Frontlet
public class ProductDetailsFrontlet {

    @ModelData
    public Product product(@FrontletParameter("productId") long productId) {
        return productService.findById(productId);
    }
}
```

## Return a Frontlet Class

An action can return a frontlet controller class.

```java
@Action
public Class<?> showCreateForm() {
    return ProductFormFrontlet.class;
}
```

This updates the current target container. If the frontlet declares `containerId`, XIS can use that metadata to select
the container.

## Return `FrontletResponse`

Use `FrontletResponse` when you need to pass parameters, choose a container, or reload frontlets.

```java
import one.xis.FrontletResponse;

@Action
public FrontletResponse showProduct(@ActionParameter("productId") long productId) {
    return new FrontletResponse(ProductDetailsFrontlet.class)
            .frontletParameter("productId", productId)
            .targetContainer("main");
}
```

Shortcut:

```java
@Action
public FrontletResponse showProduct(@ActionParameter("productId") long productId) {
    return FrontletResponse.of(ProductDetailsFrontlet.class, "productId", productId)
            .targetContainer("main");
}
```

Reload another frontlet by ID:

```java
@Action
public FrontletResponse save(@FormData("product") ProductForm product) {
    productService.save(product);
    return new FrontletResponse()
            .reloadFrontlet("ProductListFrontlet");
}
```

## Deep Linking

Every `@Page` URL is a deep link. Users can bookmark it, open it directly, and use browser back/forward navigation.

Frontlet state is not automatically part of the URL. If a specific frontlet state must be shareable, model it through
page path variables, query parameters, or frontlet URL metadata.

Example: a product page controls which frontlet loads and passes the product ID to it.

```java
@Page("/products/{id}.html")
public class ProductPage {

    @ModelData
    public String detailsFrontlet() {
        return "ProductDetailsFrontlet";
    }

    @ModelData
    public long productId(@PathVariable("id") long id) {
        return id;
    }
}
```

```html
<main xis:frontlet-container="main" xis:default-frontlet="${detailsFrontlet}">
    <xis:parameter name="productId" value="${productId}"/>
</main>
```

## Choosing the Right Tool

| Need | Use |
| --- | --- |
| Link to another XIS page in HTML | `xis:page` or `<xis:a page="...">` |
| Link to another XIS page from a button | `<button xis:page="...">` |
| Trigger server logic from a link | `<a xis:action="...">` or `<xis:action action="...">` |
| Trigger server logic from a button | `<button xis:action="...">` |
| Trigger server logic from a form | submit control with `xis:action` |
| Stay on current page/frontlet | action returns `void` |
| Navigate to a known page without parameters | return `ProductListPage.class` |
| Navigate to a known page with parameters | return `PageResponse` |
| Navigate to an already-built URL | return `PageUrlResponse` |
| Load a frontlet into a container from HTML | `xis:frontlet` with `xis:target-container` |
| Load a frontlet from Java | return frontlet class |
| Load a frontlet with parameters/container control | return `FrontletResponse` |

Internal authentication or authorization flows may use lower-level redirect responses. Application code should normally
use the navigation types above.
