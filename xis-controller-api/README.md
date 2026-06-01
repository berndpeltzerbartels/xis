# XIS Controller API

`xis-controller-api` contains the public annotations and small helper types that application code uses to describe XIS
pages, frontlets, actions, model data, parameters, storage access, formatting, authorization, and refresh behavior.

This module is not one of the primary runtime choices, but it is part of the core programming model. Applications
usually receive it transitively through the selected runtime:

- `xis-boot`
- `xis-spring`

Most applications should depend on one runtime module and import the annotations from `one.xis`. Add
`xis-controller-api` directly only when you intentionally build a library that should compile against the XIS controller
API without selecting a runtime.

## Dependency Model

Use one runtime dependency in an application:

```groovy
dependencies {
    implementation("one.xis:xis-boot:<version>")
}
```

or:

```groovy
dependencies {
    implementation("one.xis:xis-spring:<version>")
}
```

Both runtime modules expose `xis-controller-api` transitively, so application classes can use annotations such as
`@Page`, `@ModelData`, `@Action`, and `@PathVariable` without declaring `xis-controller-api` separately.

## Minimal Example

`src/main/java/com/example/ProductPage.java`

```java
package com.example;

import one.xis.Action;
import one.xis.ModelData;
import one.xis.Page;
import one.xis.PathVariable;

@Page("/products/{id}")
public class ProductPage {

    @ModelData
    public Product product(@PathVariable("id") Long id) {
        return productService.findById(id);
    }

    @Action
    public Class<?> delete(@PathVariable("id") Long id) {
        productService.delete(id);
        return ProductListPage.class;
    }
}
```

`src/main/resources/ProductPage.html`

```html
<html xmlns:xis="https://xis.one/xsd" lang="en">
<head>
    <meta charset="UTF-8">
    <title>${product.name}</title>
</head>
<body>
    <h1>${product.name}</h1>
    <p>${product.description}</p>

    <a xis:action="delete">Delete product</a>
</body>
</html>
```

The selected runtime discovers the controller, binds the annotations at runtime, invokes the `delete` action from the
template, and navigates to `ProductListPage` from the action return value. Path variables and query parameters from the
current page URL are available to page actions.

## Page Controllers

### `@Page`

`@Page` marks a class as a page controller and maps it to a URL pattern. The pattern can be static, such as
`/products.html`, or contain path variables, such as `/products/{id}`. Methods inside the controller can then receive
those path variables with `@PathVariable`.

A page controller is also registered as a framework component for the supported dependency injection runtimes.

| Attribute | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `value` | `String` | Yes | none | Page URL pattern handled by this controller. |

### `@WelcomePage`

`@WelcomePage` marks the default page or router route used when no other URL mapping matches. An application should
have at most one welcome page. On routers it can be placed on one `@Route` method, or on a `@Router` class that declares
exactly one route.

| Attribute | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| none | - | - | - | Marker annotation. |

### `@Router` and `@Route`

`@Router` marks an optional route-only controller. A router has no HTML template. It matches incoming page URLs before a
page controller is selected and delegates them through methods annotated with `@Route`. Use it when several links or
actions share one business destination, but backend code must decide the final view.

```java
@Router("/products")
public class ProductRouter {

    @Route("/{id}.html")
    public PageResponse product(@PathVariable("id") long id) {
        return PageResponse.of(ProductDetailsPage.class, "id", id);
    }
}
```

Route methods must return a navigation value such as `String`, `PageResponse`, `PageUrlResponse`, `FrontletResponse`,
`ModalResponse`, or a page/frontlet class. `@Route` is only valid inside `@Router` controllers and must not be combined
with `@Action`.

| Annotation | Attribute | Type | Required | Default | Description |
| --- | --- | --- | --- | --- | --- |
| `@Router` | `value` | `String` | Yes | none | Base route path. |
| `@Route` | `value` | `String` | No | `""` | Method route path appended to the router base path. |

`@WelcomePage` may mark one route as the application entry point when a router should decide the final destination.

### `@HtmlFile`

`@HtmlFile` maps a controller to an HTML template when the template does not follow the default naming convention. If
the path starts with `/`, it is treated as an absolute classpath resource path. Otherwise it is resolved relative to the
controller package.

Use this when several controllers share a template or when the template filename differs from the controller class name.

| Attribute | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `value` | `String` | Yes | none | HTML template resource path. |

### `@DefaultHtmlFile`

`@DefaultHtmlFile` provides a default HTML template for library-provided controllers while still allowing applications to
override the template with `@HtmlFile` on a concrete controller class.

Like `@HtmlFile`, paths starting with `/` are absolute classpath resource paths; other paths are relative to the
controller package.

| Attribute | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `value` | `String` | Yes | none | Default HTML template resource path. |

## Frontlets And Includes

### `@Frontlet`

`@Frontlet` marks a class as a reusable UI frontlet controller. Frontlets can be embedded in pages or other frontlets and have
their own model data and actions.

If no explicit frontlet id is provided, XIS uses the Java class simple name. In larger applications, an explicit id avoids
collisions between frontlet classes with the same simple name in different packages.

| Attribute | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `value` | `String` | No | `""` | Frontlet id. If empty, the class simple name is used. |
| `id` | `String` | No | `""` | Alternative id attribute. Intended as an alias for `value`. |
| `url` | `String` | No | `""` | Optional frontlet URL metadata. |
| `title` | `String` | No | `""` | Optional frontlet title metadata. |
| `containerId` | `String` | No | `""` | Optional target container metadata. |

### `@FrontletParameter`

`@FrontletParameter` injects a stable parameter of the current frontlet instance. Frontlet parameters are supplied by
child `<xis:parameter>` tags, by `FrontletResponse`, or by query strings on frontlet targets such as
`/product-summary?productId=42`. These target query strings are still read with `@FrontletParameter`;
`@QueryParameter` is reserved for the query string of the current page URL. Nested frontlets can also read frontlet
parameters from their containing frontlet; parameters supplied directly to the nested frontlet win on name conflicts.

| Attribute | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `value` | `String` | No | `""` | Parameter name. Empty means all frontlet parameters when the Java parameter is a `Map<String, String>`. |

### `@ModalParameter`

`@ModalParameter` injects a stable parameter of the current modal instance. Modal parameters are supplied by child
`<xis:parameter>` tags on modal openers, by `ModalResponse`, or by query strings on modal targets such as
`/customers/edit?customerId=42`.

| Attribute | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `value` | `String` | No | `""` | Parameter name. Empty means all modal parameters when the Java parameter is a `Map<String, String>`. |

### `@Include`

`@Include` makes a reusable HTML include available to templates. If the annotation value is `navigation`, templates can
reference it as an include named `navigation`. The value is an include key, not a resource path. This is intentional:
templates can only use fragments that application code has explicitly exposed with `@Include`.

Classes annotated with `@Include` must be concrete classes. They are registered as framework components.

| Attribute | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `value` | `String` | Yes | none | Include key used from templates. |

## Model, Form, And Action Methods

### `@ModelData`

`@ModelData` marks a method as a source of data for the current page or frontlet. The return value is exposed to the
template and can be used in expressions such as `${product.name}`.

If `value` is empty, XIS derives the model name from the method name. Getter-style names are converted to property
names, for example `getUser()` becomes `user`.

| Attribute | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `value` | `String` | No | `""` | Template model name. Empty means derive it from the method name. |

### `@FormData`

`@FormData` binds a Java object to a form. On a method, the return value initializes or refreshes form data. On an action
method parameter, the submitted form data is deserialized and passed into the controller method. `@FormData` parameters
are only supported on `@Action` methods. `@FormData` methods must not return `null`.

In the current API, the `value` attribute is required by Java. Use the same binding key in the template and controller.

| Attribute | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `value` | `String` | Yes | none | Form binding key. |

### `@Action`

`@Action` marks a method as callable from the browser. Templates invoke actions with elements such as
`<a xis:action="delete">Delete</a>` or buttons/forms using `xis:action`.

If `value` is empty, the Java method name is used as the action name. Page actions receive path variables and query
parameters from the current page URL. Frontlet actions receive the frontlet state and frontlet parameters.

`updateEventKeys` can be used to publish refresh event keys after the action, allowing other pages or frontlets to reload
when relevant data changes.

| Attribute | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `value` | `String` | No | `""` | Action name used from templates. Empty means use the Java method name. |
| `updateEventKeys` | `String[]` | No | `{}` | Refresh event keys emitted after the action. |

### `@ActionParameter`

`@ActionParameter` injects a parameter supplied by an action link, button, form submitter, or drag-and-drop action.
In templates, named values are typically sent with `xis:parameter` elements or named drop arguments such as
`xis:drop="move(from, target='${field}')"`.

Action parameters are scoped to the exact element that triggered the action. They do not fall back to frontlet or modal
parameters with the same name; use `@FrontletParameter` or `@ModalParameter` when a method also needs that stable
context.

| Attribute | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `value` | `String` | No | `""` | Action parameter name. Either `value` or `index` must be set. |
| `index` | `int` | No | `-1` | Explicit 1-based positional action argument index. |

### `@PathVariable`

`@PathVariable` injects a value extracted from a dynamic page URL. The corresponding page must declare the variable in
its `@Page` pattern, for example `@Page("/products/{id}")`.

Path variables are available both when loading model data and when executing page actions from the current URL.

| Attribute | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `value` | `String` | Yes | none | Path variable name from the `@Page` URL pattern. |

### `@QueryParameter`

`@QueryParameter` injects a value from the query string of the current page URL, for example `?filter=active&page=7`.

Query parameters are available both when loading model data and when executing page actions from the current URL.

| Attribute | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `value` | `String` | Yes | none | Query parameter name. |

### `@NullAllowed`

`@NullAllowed` marks a controller method parameter as allowed to be `null`. Without it, missing or null input is usually
treated as invalid for required controller bindings.

| Attribute | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| none | - | - | - | Marker annotation. |

## Client And User Context

### `@ClientId`

`@ClientId` injects the anonymous browser/client identifier. It does not require authentication and is useful for
guest-state scenarios such as shopping carts, in-progress forms, or targeted refreshes for a specific browser.

The value is expected to be a `String`.

| Attribute | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| none | - | - | - | Marker annotation. |

### `@UserId`

`@UserId` injects the authenticated user id when XIS authentication is active. Use it when an action or model method
must be associated with a verified user rather than an anonymous client.

The value is expected to be a `String`. Without authentication, user-based targeting and user-id injection are not
available.

| Attribute | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| none | - | - | - | Marker annotation. |

### `@Authenticated`

`@Authenticated` marks a controller class, action method, or action DTO as requiring a logged-in user without requiring
a named role.

| Attribute | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| none | - | - | - | Marker annotation. |

### `@Roles`

`@Roles` marks a controller class, action method, or action DTO as requiring specific roles. A legacy empty `@Roles`
still requires an authenticated user but no named role; prefer `@Authenticated` for that case.

| Attribute | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `value` | `String[]` | No | `{}` | Required role names. Empty is kept as authenticated-only compatibility. |

### `@OwnedBy`

`@OwnedBy` connects a submitted object to an application-defined `OwnershipGuard`. XIS calls the guard after
deserialization and before the controller action is invoked. The guard receives the deserialized object and the trusted
`UserContext`, so application code can load the referenced resource and decide whether the current user may access it.

Ownership violations follow the same security response path as `@Roles`: the controller action is not called and the
frontend is redirected to login.

| Attribute | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `value` | `Class<? extends OwnershipGuard<?>>` | Yes | none | Guard used for the ownership decision. |

### `@RefreshOnUpdateEvents`

`@RefreshOnUpdateEvents` declares event keys that should refresh a page or frontlet when the server publishes matching
update events. This is the receiving side of the refresh-event model.

Use it on a page or frontlet controller that should reload when a related action emits one of the configured event keys.

| Attribute | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `value` | `String[]` | No | `{}` | Refresh event keys this controller reacts to. |

## Storage And Shared Values

### `@LocalStorage`

`@LocalStorage` binds a controller parameter or method return value to a value stored in the browser's `localStorage`.
For parameters, the client sends the configured key to the server, XIS deserializes the value, and changes made to the
object can be written back after the action. For methods, the return value is written to the configured key.

Local storage persists across browser sessions. Use `@NullAllowed` if a missing value should be passed as `null`
instead of initialized with a default value.

XIS discovers the used keys from controller parameters and storage methods and sends only those keys for the current
page or frontlet. After method invocation, parameter values and annotated method return values are written back to
browser storage. Returning `null` removes the value on the client.

| Attribute | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `value` | `String` | Yes | none | Browser `localStorage` key. |

### `@SessionStorage`

`@SessionStorage` binds a controller parameter or method return value to a value stored in the browser's
`sessionStorage`. The value is scoped to the current browser tab/session and is cleared by the browser when the session
ends.

Use it for workflows such as multi-step forms where state should survive reloads inside one tab but not persist as long
as `localStorage`.

Like `@LocalStorage`, XIS sends only configured keys for the current page or frontlet and writes parameter values or
annotated method return values back after method invocation. Returning `null` removes the value on the client.

| Attribute | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `value` | `String` | Yes | none | Browser `sessionStorage` key. |

### `@ClientState`

`@ClientState` binds a controller parameter or method return value to short-lived XIS client-side state. Unlike browser
`localStorage` and `sessionStorage`, this state is held by the JavaScript runtime and is not meant as durable browser
storage.

Use it for short-lived interaction state that should not be persisted across browser sessions. Good examples are
selected items, expanded panels, temporary form context, and small UI flags that would otherwise require extra
controller plumbing. Do not treat client state as the standard place for every variable. XIS is not a frontend-only
framework; model data, frontlet parameters, modal parameters, action parameters, shared values, and server-side flow
remain the normal tools when they describe the interaction directly.

Returning `null` from an annotated method, or writing back a nullable parameter value, removes the value from client
state.

| Attribute | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `value` | `String` | Yes | none | Client-state key. |

### `@SharedValue`

`@SharedValue` provides or injects a named value within one request/controller processing flow. Use it when several
controller methods need the same loaded object or intermediate context and you do not want to repeat the lookup logic.
On a method, the returned value is stored under the given key for the current processing flow. On a parameter, the value
with that key is injected. It is not persisted across requests.

| Attribute | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `value` | `String` | Yes | none | Shared value key. |

## Titles, Assets, And Extensions

### `@Title`

`@Title` marks a method or parameter that provides or receives the page title. A method annotated with `@Title` can
produce the title used by the browser after rendering or action processing.

| Attribute | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| none | - | - | - | Marker annotation. |

## Formatting And Validation Markers

### `@UseFormatter`

`@UseFormatter` applies a custom `Formatter` to a field, parameter, annotation, or record component. It controls both
display formatting and parsing where formatted input is deserialized.

| Attribute | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `value` | `Class<? extends Formatter<?>>` | Yes | none | Formatter implementation class. |

### `@Mandatory`

`@Mandatory` marks a field, parameter, or record component as required during deserialization/validation. For collection
values it requires at least one element. For other values it requires the value to be present and non-empty according to
the deserializer.

This annotation lives in `xis-controller-api` because core controller binding needs it. Additional validation
annotations live in `xis-validation`.

| Attribute | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| none | - | - | - | Marker annotation. |

### `@AllElementsMandatory`

`@AllElementsMandatory` marks a collection or array field, parameter, or record component so that all elements must be
non-null. Primitive element types are already non-null by nature.

| Attribute | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| none | - | - | - | Marker annotation. |

## Framework-Level Annotations

### `@ImportInstances`

`@ImportInstances` marks an interface whose implementations should be imported from the host framework, such as Spring,
into the private XIS context.

This annotation is primarily framework-level API. Application authors usually interact with it indirectly through
extension interfaces such as `Formatter`.

| Attribute | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| none | - | - | - | Marker annotation. |

## Public Helper Types

### `Formatter<T>`

`Formatter<T>` is the extension interface used together with `@UseFormatter`. Implement it when an application type
needs custom locale-aware display formatting and parsing.

```java
public interface Formatter<T> {
    String format(T value, Locale locale, ZoneId zoneId);

    T parse(String value, Locale locale, ZoneId zoneId);
}
```

Formatter implementations can be imported from the host framework into XIS because the interface is annotated with
`@ImportInstances`.

### `Response`

`Response` is the common contract implemented by response helper types that can target a controller class.

Most application code returns one of the concrete response types instead of implementing `Response` directly.

### `PageResponse`

`PageResponse` navigates to a page controller from an action result. It can carry path variables and query parameters
needed to build the destination URL.

Common usage:

```java
return new PageResponse(ProductPage.class)
        .pathVariable("id", productId)
        .queryParameter("tab", "details");
```

or:

```java
return PageResponse.of(ProductPage.class, "id", productId);
```

### `PageUrlResponse`

`PageUrlResponse` navigates to a concrete URL string. It can also append query parameters supplied through its
constructor.

Use `PageResponse` when the destination is a known XIS page controller. Use `PageUrlResponse` when the action already
has a URL.

### `FrontletResponse`

`FrontletResponse` targets frontlet updates from an action result. It can select a frontlet controller, pass frontlet
parameters, target a container, or request that a frontlet reload.

Common usage:

```java
return FrontletResponse.of(ProductFrontlet.class, "productId", productId);
```

or:

```java
return new FrontletResponse(ProductFrontlet.class)
        .frontletParameter("productId", productId)
        .targetContainer("details");
```

## Quick Reference

### Annotations

| Annotation | Target | Required Attributes | Purpose |
| --- | --- | --- | --- |
| `@Page` | class | `value` | Declares a page controller and maps it to a URL pattern. |
| `@WelcomePage` | class, method | none | Declares the fallback/default page or router route. |
| `@HtmlFile` | class | `value` | Binds a controller to an explicit HTML template. |
| `@DefaultHtmlFile` | class | `value` | Provides a default template for library controllers. |
| `@Frontlet` | class | none | Declares a reusable frontlet controller. |
| `@ActionParameter` | parameter | none | Injects a parameter from the triggering action element. |
| `@FrontletParameter` | parameter | none | Injects a stable parameter of the current frontlet. |
| `@ModalParameter` | parameter | none | Injects a stable parameter of the current modal. |
| `@Include` | class | `value` | Registers a reusable HTML include. |
| `@ModelData` | method | none | Exposes data to the template model. |
| `@FormData` | method, parameter | `value` | Binds form data. |
| `@Action` | method | none | Marks a browser-callable action. |
| `@PathVariable` | parameter | `value` | Injects a path variable from the current page URL. |
| `@QueryParameter` | parameter | `value` | Injects a query parameter from the current page URL. |
| `@NullAllowed` | parameter | none | Allows a nullable bound parameter. |
| `@ClientId` | parameter | none | Injects the anonymous client id. |
| `@UserId` | parameter | none | Injects the authenticated user id. |
| `@Authenticated` | class, method | none | Declares authenticated-only access. |
| `@Roles` | class, method | none | Declares required roles. |
| `@OwnedBy` | class, parameter, field, record component | `value` | Runs an ownership guard for a submitted object. |
| `@RefreshOnUpdateEvents` | class | none | Declares refresh event keys this controller reacts to. |
| `@LocalStorage` | method, parameter | `value` | Binds browser `localStorage`. |
| `@SessionStorage` | method, parameter | `value` | Binds browser `sessionStorage`. |
| `@ClientState` | method, parameter | `value` | Binds XIS client-side state. |
| `@SharedValue` | method, parameter | `value` | Provides or injects a named value inside one controller processing flow. |
| `@Title` | method, parameter | none | Provides or receives the page title. |
| `@UseFormatter` | field, parameter, annotation, record component | `value` | Applies a formatter. |
| `@Mandatory` | field, parameter, record component | none | Marks a value as required. |
| `@AllElementsMandatory` | field, parameter, record component | none | Requires all collection/array elements to be non-null. |
| `@ImportInstances` | class/interface | none | Imports host-framework implementations into the XIS context. |

### Helper Types

| Type | Purpose |
| --- | --- |
| `Formatter<T>` | Custom locale-aware formatting and parsing used with `@UseFormatter`. |
| `Response` | Common contract for controller-targeted response helper types. |
| `PageResponse` | Navigates to a page controller with optional path variables and query parameters. |
| `PageUrlResponse` | Navigates to a concrete URL string. |
| `FrontletResponse` | Targets frontlet updates, frontlet parameters, target containers, or frontlet reloads. |
