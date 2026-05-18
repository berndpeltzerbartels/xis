# Request Lifecycle

[Documentation map](../README.md)

This chapter gives you the mental model for when XIS calls controller methods and how a browser interaction becomes a
new rendered page or frontlet.

## Initial Page Load

When the browser opens a page URL, XIS matches the URL to a `@Page` controller.

```java
@Page("/products/{id}.html")
class ProductPage {

    @ModelData("product")
    Product product(@PathVariable("id") long id) {
        return productService.findById(id);
    }
}
```

The initial load runs the page data methods, evaluates template expressions, processes XIS tags and attributes, and
renders the HTML document. If the page contains frontlet containers, the configured frontlets are loaded as part of that
rendering flow.

## Navigation Without Actions

Normal page and frontlet links do not call an action method. They only tell XIS what should be displayed next.

```html
<a xis:page="/products/42.html">Open product</a>

<xis:frontlet-container container-id="details"
                        default-frontlet="ProductDetails"/>
```

Use navigation links when no server-side business method needs to run.

## Actions

An action is a user-triggered call to a Java method annotated with `@Action`.

```html
<button xis:action="delete">
    <xis:parameter name="productId" value="${product.id}"/>
    Delete
</button>
```

```java
@Action
PageResponse delete(@Parameter("productId") long productId) {
    productService.delete(productId);
    return new PageResponse(ProductListPage.class);
}
```

The action return type decides what happens next:

| Return type | Result |
| --- | --- |
| `void` | Stay on the current page or frontlet and refresh its data. |
| `Class<?>` | Navigate to that page, or replace the current frontlet when the class is a frontlet. |
| `String` | Navigate to the URL string. |
| `PageResponse` / `PageUrlResponse` | Navigate to a page with explicit path variables, query parameters, or URL details. |
| `FrontletResponse` | Replace a frontlet, optionally in a specific container. |
| `ModalResponse` | Open or close a modal dialog. |

For the full matrix, including container rules, frontlet-to-frontlet replacement, and modal responses, see
[Navigation and responses](navigation.md).

An action method may also be annotated with `@ModelData`. In that case the action still runs because the user triggered
it, and its return value is also written into the model data of the current response. This is useful for small UI
results that should appear immediately without adding another model method.

```html
<button xis:action="calculateDiscount">
    <xis:parameter name="productId" value="${product.id}"/>
    Calculate discount
</button>

<p>${discountMessage}</p>
```

```java
@Action
@ModelData("discountMessage")
String calculateDiscount(@Parameter("productId") long productId) {
    return discountService.discountMessageFor(productId);
}
```

## Forms

A form action adds binding and validation before the action method runs.

```html
<form xis:binding="product">
    <input xis:binding="name">
    <button xis:action="save">Save</button>
</form>
```

```java
@Action
PageResponse save(@FormData("product") ProductForm product) {
    productService.save(product);
    return new PageResponse(ProductDetailsPage.class)
            .pathVariable("id", product.id());
}
```

XIS deserializes submitted values into the `@FormData` object and validates its annotations. If validation fails, the
action method is not called. The page or frontlet is rendered again with the submitted values and validation messages.

See [Forms and validation](forms-and-validation.md) for message rendering, custom validators, records, and formatters.

## Shared Values

Use `@SharedValue` when several methods in the same processing flow need the same object.

```java
@SharedValue("product")
Product product(@PathVariable("id") long id) {
    return productService.findById(id);
}

@ModelData("product")
Product productModel(@SharedValue("product") Product product) {
    return product;
}

@Action
void rename(@SharedValue("product") Product product,
            @Parameter("name") String name) {
    product.rename(name);
}
```

This avoids repeating database or service lookups and lets an action work with the same contextual object that was
loaded for rendering.

## Client State

`@LocalStorage`, `@SessionStorage`, and `@ClientStorage` read named values that XIS knows the controller may need. XIS
does not send the whole browser store. It scans controller annotations and only transfers keys that may be used by the
current controller.

```java
@Action
void addToCart(@LocalStorage("cart") Cart cart,
               @Parameter("productId") String productId) {
    cart.add(productId);
}
```

Prefer server-side state for normal application data. Client state is a convenience feature for browser-local state such
as a cart draft, wizard state, or UI preferences.

## Server-Triggered Refresh Events

The normal action response updates the current page or frontlet. Refresh events are for other already open pages or
frontlets that should reload because shared state changed.

To publish one, inject `RefreshEventPublisher` and call `publishToAll(...)`, `publishToClient(...)`,
`publishToUser(...)`, or `publishToAllUsers(...)`.

```java
@Action
void addItem(@ClientId String clientId) {
    cartService.add(clientId);
    refreshEventPublisher.publishToClient("cart-updated", clientId);
}
```

See [Events](events.md) for the full publishing examples, target rules, and
frontlet reload details.
