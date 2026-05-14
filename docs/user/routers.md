# Routers

[Documentation map](../README.md)

Routers are an optional navigation tool. They are not a core concept that every XIS application has to use. Unlike
frameworks where every route has to be declared in a router, XIS normally maps pages directly through `@Page`. Most
pages can and should be linked directly with `xis:page`, ordinary links, forms, or action responses.

Use a router only when several links or several actions point to the same business destination, but server-side business
logic must decide what should actually be shown. Without a router, that decision often ends up duplicated in several
actions or hidden in a service. Duplicating the navigation code is noisy, while putting view navigation into a domain
service usually mixes responsibilities. A router keeps that decision close to the web layer without forcing every link
or action to know the final target.

## Typical Case

A shop links to a product detail route. The URL is stable:

```html
<a xis:page="/products/${product.id}.html">${product.name}</a>
```

But the product state decides which view is useful:

- the product is available, so the page should show an order frontlet
- the product is sold out, so the page should show a notification frontlet
- the product needs age confirmation, so the page should show a confirmation frontlet first

The link still points to one product destination. The router owns the decision about the concrete page or frontlet.

```java
import one.xis.FrontletResponse;
import one.xis.PathVariable;
import one.xis.Route;
import one.xis.Router;

@Router("/products")
public class ProductRouter {

    private final ProductService productService;

    public ProductRouter(ProductService productService) {
        this.productService = productService;
    }

    @Route("/{id}.html")
    public FrontletResponse product(@PathVariable("id") long id) {
        var product = productService.findById(id);
        if (product.isSoldOut()) {
            return FrontletResponse.of(SoldOutNotificationFrontlet.class, "id", id)
                    .targetContainer("product-area");
        }
        return FrontletResponse.of(OrderProductFrontlet.class, "id", id)
                .targetContainer("product-area");
    }
}
```

The same pattern can also return a page:

```java
@Route("/{id}/edit.html")
public PageResponse editProduct(@PathVariable("id") long id) {
    if (productService.requiresApproval(id)) {
        return PageResponse.of(ProductApprovalPage.class, "id", id);
    }
    return PageResponse.of(EditProductPage.class, "id", id);
}
```

## How Routing Works

A router is a backend-only controller. It has no HTML template. XIS matches the router URL, calls the matching `@Route`
method, and then processes the returned navigation target through the normal response model.

The class-level `@Router` path and method-level `@Route` path are joined:

```java
@Router("/products")
public class ProductRouter {

    @Route("/{id}.html")
    public PageResponse product(@PathVariable("id") long id) {
        return PageResponse.of(ProductPage.class, "id", id);
    }
}
```

This matches `/products/42.html`. If the combined route does not end with `.html`, XIS adds `.html`.

Route methods may use the same injected request data as ordinary controller methods, such as `@PathVariable`,
`@QueryParameter`, `@ClientId`, `UserContext`, and storage parameters.

## Return Values

Route methods must return a navigation value:

- `String` or `PageUrlResponse` for URL-based page navigation
- a page class or `PageResponse` for type-oriented page navigation
- a frontlet class or `FrontletResponse` for frontlet navigation
- `ModalResponse` for modal navigation

Use `String` or URL responses when you intentionally want to decouple modules. For example, in a distributed application
a router can return a URL without requiring the target page class on the classpath.

## Validation

`@Route` is only valid on `@Router` controllers. XIS fails during application startup if a route method is placed on a
page or frontlet controller, if it is also annotated with `@Action`, or if its return type is not a supported navigation
type.

## When Not to Use a Router

Do not introduce a router for ordinary page links. If a link always opens the same page, use `xis:page`. If an action
always opens the same target, return the navigation value directly from the action. Router is useful when the same
business destination is reached from multiple places and the final view depends on state that belongs in backend code.
