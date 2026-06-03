# Routers

[Documentation map](../../README.md)

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

But the product state decides which page URL is useful:

- the product is available, so the normal product page should open
- the product is sold out, so a sold-out page should open
- the product needs age confirmation, so an age-confirmation page should open first

The link still points to one product destination. The router owns the decision about the concrete page.

```java
import one.xis.PageResponse;
import one.xis.PathVariable;
import one.xis.Route;
import one.xis.Router;

@Router("/products")
class ProductRouter {

    private final ProductService productService;

    ProductRouter(ProductService productService) {
        this.productService = productService;
    }

    @Route("/{id}.html")
    PageResponse product(@PathVariable("id") long id) {
        var product = productService.findById(id);
        if (product.isSoldOut()) {
            return PageResponse.of(SoldOutProductPage.class, "id", id);
        }
        if (product.requiresAgeConfirmation()) {
            return PageResponse.of(AgeConfirmationPage.class, "id", id);
        }
        return PageResponse.of(ProductPage.class, "id", id);
    }
}
```

The same pattern can also return a page:

```java
@Route("/{id}/edit.html")
PageResponse editProduct(@PathVariable("id") long id) {
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
class ProductRouter {

    @Route("/{id}.html")
    PageResponse product(@PathVariable("id") long id) {
        return PageResponse.of(ProductPage.class, "id", id);
    }
}
```

This matches `/products/42.html`. If the combined route does not end with `.html`, XIS adds `.html`.

Route methods may use the same injected request data as ordinary controller methods, such as `@PathVariable`,
`@QueryParameter`, `@ClientId`, `UserContext`, and storage parameters.

## Welcome Routes

A router can provide the application's welcome entry when the first request should be decided by backend logic.
Annotate the selected `@Route` method with `@WelcomePage`:

```java
@Router("/entry")
class EntryRouter {

    @WelcomePage
    @Route("/start.html")
    PageResponse start(@ClientId String clientId) {
        return PageResponse.of(HomePage.class);
    }
}
```

If a router has exactly one route, `@WelcomePage` may also be placed on the router class. XIS fails during startup if
that would be ambiguous, for example when a class-level welcome router declares more than one `@Route`.

## Return Values

Route methods must return a page navigation value:

- `String` or `PageUrlResponse` for URL-based page navigation
- a page class or `PageResponse` for type-oriented page navigation

Use `String` or URL responses when you intentionally want to decouple modules. For example, in a distributed application
a router can return a URL without requiring the target page class on the classpath.

Routers are matched from page URLs and should not be used to select frontlets directly. If a page URL should decide
which frontlet is mounted, model that through the target page, query/path parameters, or URL-mounted frontlets.

## Validation

`@Route` is only valid on `@Router` controllers. XIS fails during application startup if a route method is placed on a
page or frontlet controller, if it is also annotated with `@Action`, or if its return type is not a supported navigation
type.

## When Not to Use a Router

Do not introduce a router for ordinary page links. If a link always opens the same page, use `xis:page`. If an action
always opens the same target, return the navigation value directly from the action. Router is useful when the same
business destination is reached from multiple places and the final view depends on state that belongs in backend code.
