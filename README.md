# XIS - Java Web Framework

**Server-side rendered HTML meets Single Page Application**

XIS is a Java web framework for building interactive web applications with plain Java and plain HTML. It keeps the
development model close to server-side rendering while providing smooth SPA-style navigation and UI updates.

Instead of designing a separate communication layer between browser and server, you describe pages, actions, and data
declaratively. XIS handles the transport and page update flow for you.

## Why XIS?

Many web stacks force you into one of two extremes:

- server-side rendering with full page reloads
- REST APIs plus a JavaScript frontend with extra coordination and boilerplate

XIS is designed to avoid that split. You write Java controllers and HTML templates, while XIS handles navigation,
actions, and UI refresh behavior.

**No REST endpoints. No REST clients. No manual request/response wiring.**

## What XIS Takes Off Your Plate

With XIS, developers do not need to implement the browser-server communication path manually.

You describe interaction declaratively, primarily through annotations and conventions. XIS then takes care of
transporting user interactions, invoking server-side actions, and updating the UI.

That means less work spent on:

- REST endpoint boilerplate
- REST client boilerplate
- custom frontend-backend transport contracts
- manual coordination between UI events and backend handlers

## Hello World

```java
package com.example;

import one.xis.Action;
import one.xis.ModelData;
import one.xis.Page;

@Page("/hello.html")
public class HelloPage {

    private int counter = 0;

    @ModelData
    public String message() {
        return "Hello World!";
    }

    @ModelData
    public int count() {
        return counter;
    }

    @Action
    public void increment() {
        counter++;
    }
}
```

```html
<html xmlns:xis="https://xis.one/xsd" lang="en">
<head>
    <meta charset="UTF-8">
    <title>Hello</title>
</head>
<body>
    <h1>${message}</h1>
    <p>Counter: ${count}</p>
    <button xis:action="increment">Click me</button>
</body>
</html>
```

The button invokes a Java method without a full page reload. Navigation and UI updates remain smooth without manual
JavaScript wiring.

## How It Works

XIS automatically:

- maps URLs to page controllers
- renders HTML templates with model data
- handles button clicks and form submissions
- updates relevant parts of the page
- manages browser history and deep linking

## Key Features

- Plain Java and plain HTML
- Declarative interaction via annotations
- No REST endpoints and no REST clients
- Automatic handling of client-server communication
- Smooth SPA-style navigation without manual frontend wiring
- Extendable expression language for templates
- Convention over configuration
- Testable controller-oriented programming model

## Runtime Support

The first major XIS release focuses on two actively supported runtime models:

- Spring
- XIS Boot (`xis-boot`) for standalone applications

A Micronaut module still exists in the repository, but it is currently not actively maintained.

## Controller API

The public controller annotations live in [`xis-controller-api`](xis-controller-api/README.md).

This module is not a primary runtime choice, but it is part of the core programming model. Applications normally receive
it transitively through `xis-boot` or `xis-spring`, so users can import annotations such as `@Page`, `@ModelData`,
`@Action`, and `@PathVariable` without adding `xis-controller-api` separately.

## Validation API

The public validation annotations live in [`xis-validation`](xis-validation/README.md).

This module follows the same dependency model: it is not a primary runtime choice, but users normally receive it through
`xis-boot` or `xis-spring` and can import annotations such as `@EMail`, `@MinLength`, `@RegExpr`, and `@LabelKey`.

## Refresh Events

XIS can refresh pages and widgets when something changes on the server.

These refresh events are intentionally lightweight:

- they only send event keys
- they do not send application data payloads
- the browser reloads the affected page or widget data through the normal XIS flow

This keeps the push channel simple. The transport is used as a notification mechanism, not as a second data API.

Typical examples are:

- a score changed in a game
- a stock price changed
- a dashboard widget should reload

On the server, controllers and widgets declare which event keys they react to. When such an event is published, XIS
refreshes the affected UI parts.

### Targeting

Refresh events can be targeted in different ways:

- all connected clients
- specific client IDs
- specific user IDs
- all authenticated users

`clientId` is always available. It identifies a browser/client instance and does not require authentication.

`userId` is only available when XIS authentication is active. It identifies an authenticated user and can map to
multiple connected clients.

Without authentication, use client-based or global refresh targeting. User-based targeting requires the
`xis-authentication` module.

## Real-World Example

```java
@Page("/products/{id}")
public class ProductDetailPage {

    @Inject
    ProductService productService;

    @ModelData
    public Product product(@PathVariable("id") Long id) {
        return productService.findById(id);
    }

    @Action
    public Class<?> deleteProduct(@PathVariable("id") Long id) {
        productService.delete(id);
        return ProductListPage.class;
    }
}
```

```html
<html xmlns:xis="https://xis.one/xsd" lang="en">
<head>
    <meta charset="UTF-8">
    <title>Product Detail</title>
</head>
<body>
    <h1>${product.name}</h1>
    <button xis:action="deleteProduct">Delete Product</button>
    <a xis:page="ProductListPage">Back to list</a>
</body>
</html>
```

## Learn More

- [Controller API annotations](xis-controller-api/README.md)
- [Validation annotations](xis-validation/README.md)
- [Documentation](https://xis.one/docs/introduction.html)
- [Quickstart](https://xis.one/quickstart/installation.html)

## License

Apache License 2.0
