# Annotation Reference

[Documentation map](../../README.md)

This page is the compact map of the public annotations a XIS user can meet while building an application. The workflow
chapters explain the details; this page makes sure class, method, field, record-component, and parameter annotations are
findable in one place.

Most applications start with `@Page`, `@ModelData`, `@Action`, `@FormData`, `@PathVariable`, and `@QueryParameter`.
Use [Core model](core-model.md), [Navigation and responses](navigation.md), [Routers](routers.md), [Forms and validation](forms-and-validation.md),
and [Security](security.md) for complete examples.

## Class Annotations

| Annotation | Use |
| --- | --- |
| `@Page("/path.html")` | Marks a class as a page controller and maps it to a browser URL. |
| `@Router("/path")` | Marks an optional route-only controller. A router has no template and delegates matching URLs to pages from `@Route` methods. See [Routers](routers.md). |
| `@WelcomePage` | Marks the page or single router route that should be used for `/`. |
| `@Frontlet` | Marks a reusable UI controller that renders a replaceable HTML fragment. |
| `@Modal` | Marks a modal dialog controller. See [Modals](modals.md). |
| `@Include("name")` | Registers a reusable HTML fragment that can be inserted with `xis:include` or `<xis:include>`. |
| `@HtmlFile` | Uses a template file whose name does not follow the default class-name convention. |
| `@DefaultHtmlFile` | Defines a default template file for a package or type. |
| `@Authenticated` | Protects a page, frontlet, action, or action DTO by login without requiring a named role. See [Security](security.md). |
| `@Roles` | Protects a page or frontlet by role. See [Security](security.md). |
| `@OwnedBy` | Runs an application-defined ownership guard for a submitted DTO. See [Security](security.md#ownership-checks). |
| `@RefreshOnUpdateEvents` | Refreshes a page or frontlet when one of the configured update events is fired. See [Events](events.md). |
| `@XISBootApplication` | Marks the application entry point for standalone XIS Boot applications. The main reason is build-time generation: the annotation processor uses this class to generate the `one.xis.boot.Runner` entry point needed by the Gradle plugin's `xisJar` task. |
| `@Component`, `@Service` | Registers ordinary application services in the XIS context. |
| `@DefaultComponent` | Registers a replaceable default component, mostly useful for libraries and framework extensions. |

## Method Annotations

| Annotation | Use |
| --- | --- |
| `@ModelData` | Exposes a value to the template. The default key is the method/property name. |
| `@FormData` | Loads form data for a named form binding. |
| `@Action` | Marks a method that can be called from an action link, action button, or form submit. May be combined with `@ModelData` or `@FormData` when the action return value should be rendered in the current response. The method still runs only when that action is triggered. |
| `@Route` | Marks a method on a `@Router`. Route methods run before a page controller is selected and must return a navigation value. |
| `@WelcomePage` | Can mark one `@Route` method as the application's welcome route. |
| `@Title` | Supplies a page or frontlet title from Java. |
| `@SharedValue` | Provides a named value for other controller methods during the same request/action processing flow. |
| `@Bean` | Creates a XIS context bean from a method. |
| `@Init` | Runs initialization code after dependency injection. |
| `@EventListener` | Handles XIS context events. |
| `@Scheduled` | Runs scheduled work in the XIS context. See [Scheduled jobs](scheduled-jobs.md). |

### Shared Values

Use `@SharedValue` when one controller processing flow needs the same object in several methods. A typical case is
loading an aggregate once and then using it for display and for an action without duplicating the lookup code.
The provider method must not be annotated with `@Action`: XIS calls shared-value providers when another method needs
their value, not because a user triggered them directly.

```java
@Page("/products/{id}.html")
class ProductPage {

    private final ProductService productService;

    ProductPage(ProductService productService) {
        this.productService = productService;
    }

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
                @ActionParameter("name") String name) {
        product.renameTo(name);
        productService.save(product);
    }
}
```

The shared value is only a controller execution context for the current request or action. It is not session state,
application state, or browser storage.

### Action Results As Model Data

Use `@Action` and `@ModelData` on the same method when a user action should produce a value that is rendered immediately
on the current page or frontlet.

```java
@Page("/quote.html")
class QuotePage {

    @Action
    @ModelData("calculationResult")
    String calculate(@ActionParameter("productId") long productId) {
        return quoteService.calculate(productId).displayText();
    }
}
```

The method is still an action: it is only called by an action link, action button, or form submit. `@ModelData` only says
where the return value should be placed in the response model.

For normal model methods, `@ModelData(varName = "customer")` is equivalent to `@ModelData("customer")`. The explicit
`varName` form can make examples easier to read when the value names a template variable.

## Parameter, Field, And Record Component Annotations

These annotations can appear on action/model/form parameters, DTO fields, or record components depending on their
purpose.

| Annotation | Use |
| --- | --- |
| `@PathVariable("name")` | Reads a value from a `@Page` path such as `/orders/{id}.html`. |
| `@QueryParameter("name")` | Reads a query parameter from the current page URL, such as `/orders.html?status=open`. |
| `@ActionParameter("name")` | Reads a one-shot parameter submitted by the action element that triggered the current action. |
| `@FrontletParameter("name")` | Reads a stable parameter of the current frontlet instance, supplied by a frontlet target URL, `<xis:parameter>` on a frontlet link/container, or `FrontletResponse`. |
| `@ModalParameter("name")` | Reads a stable parameter of the current modal instance, supplied by a modal target URL, `<xis:parameter>` on the opener, or `ModalResponse`. |
| `@FormData("name")` | Initializes form data on methods or injects submitted form data into `@Action` method parameters. Method usage supports `load`. |
| `@Upload` | Binds an uploaded multipart file to a form field or controller parameter. |
| `@SharedValue("name")` | Injects a value produced by another `@SharedValue` method in the same controller processing flow. |
| `@LocalStorage`, `@SessionStorage`, `@ClientState` | Injects browser-side state into an action method parameter. |
| `@ClientId` | Injects the browser client id. |
| `@UserId` | Injects the authenticated user id. |
| `@OwnedBy` | Runs an application-defined ownership guard for a submitted DTO, parameter, field, or record component. See [Security](security.md#ownership-checks). |
| `@UseFormatter` | Selects a formatter for converting form values between strings and Java values. |
| `@Mandatory` | Requires a value. |
| `@AllElementsMandatory` | Requires all elements of an array or collection value. |
| `@EMail` | Requires an email-like value. |
| `@MinLength` | Requires a minimum string length or collection size. |
| `@RegExpr` | Requires a value to match a regular expression. |
| `@LabelKey` | Supplies a context-specific label for validation messages, for example "total price" or "VAT". |
| `@Validate` | Builds a custom validation annotation by connecting it to a validator class. |
| `@NullAllowed` | Allows `null` for values that would otherwise be rejected during deserialization or validation. |
| `@Inject` | Injects a XIS context bean into a field or constructor. |
| `@Value` | Injects a property value into a field. |

Controller methods may also receive framework objects without an annotation: `UserContext` for the current browser,
authentication, and role state, plus `HttpRequest`, `HttpResponse`, and `RequestContext` for lower-level request access.

### UI Parameters

XIS keeps action, frontlet, and modal parameters separate because they have different lifetimes.
Use `@ActionParameter` for values submitted by the exact action element the user clicked. Use `@FrontletParameter` for
the stable context of the currently loaded frontlet. Use `@ModalParameter` for the stable context of the currently open
modal. These annotations are for simple values such as strings, numbers, booleans, dates, and enums. Use `@FormData`
for complex objects.

```html
<button xis:action="delete">
    <xis:parameter name="productId" value="${product.id}"/>
    Delete
</button>

<button xis:modal="/customers/edit?customerId=${customer.id}">Edit</button>
```

```java
@Action
void delete(@ActionParameter("productId") long productId) {
    productService.delete(productId);
}

@FormData("customer")
CustomerForm customer(@ModalParameter("customerId") long customerId) {
    return customerService.form(customerId);
}
```

Frontlet and modal target URLs may contain query strings. Those values are still frontlet or modal parameters and are
therefore read with `@FrontletParameter` or `@ModalParameter`. `@QueryParameter` is only for the query string of the current page URL. Use
`@PathVariable` for values embedded in a page, frontlet, or modal path. Use `@SharedValue`, `@LocalStorage`,
`@SessionStorage`, and `@ClientState` when the value comes from a different source than the current UI parameter set.

Action parameters never implicitly fall back to frontlet or modal parameters. If a method needs both values, name both
parameters explicitly. Positional action parameters are also supported:

```java
@Action
void move(@ActionParameter(index = 1) String from, @ActionParameter(index = 2) String to) {
}
```

Use indexes for positional action values, for example function-style template expressions such as drag-and-drop
arguments. The explicit index is 1-based and counts only action values, not Java method parameters. Every
`@ActionParameter` must set either `value` or `index`. An unnamed
`@FrontletParameter Map<String, Integer>` or `@ModalParameter Map<String, T>` receives all current frontlet or modal
parameters when `T` is a simple value type. Map keys must be `String`; values may be strings, numbers, booleans, dates,
or enums. `@ActionParameter` does not support an unnamed map because action parameters are scoped to one triggering
element.

### Configuration Values

Use `@Value` on a field when a component needs a property from `application.properties` or a profile-specific properties
file:

```java
@Component
class MailSettings {

    @Value("mail.host")
    String host;

    @Value(value = "mail.sender", mandatory = false)
    String sender;
}
```

By default, a missing property is an error. Set `mandatory = false` when the value is genuinely optional; the field then
keeps its Java default value when the property is absent.

### Browser Storage Parameters

`@LocalStorage`, `@SessionStorage`, and `@ClientState` do not send the whole browser state to the server. XIS scans the
controller methods, collects the storage keys used by annotated parameters, and writes those keys into the client
configuration for the page or frontlet. The browser then sends those configured keys with requests for that page or
frontlet.

Use storage parameters for values that intentionally live in the browser:

```java
class CartPage {

    @Action("addToCart")
    void addToCart(@LocalStorage("cart") Cart cart,
                   @ActionParameter("productId") String productId) {
        cart.add(productId);
    }
}
```

After the action finishes, XIS writes the storage parameter value back to the browser. This is most useful for mutable
DTO-like values. Do not use browser storage as the default place for application state; server-side state is usually
simpler and easier to control.

`@ClientState` is intentionally lighter than browser storage. It is useful for short-lived UI state such as selected
items, expanded panels, temporary form context, or other values that would otherwise force extra controller plumbing.
It is not XIS's default model for variables. Prefer model data, frontlet parameters, modal parameters, action
parameters, and shared values when those describe the flow directly. In particular, do not copy the usual pattern from
pure frontend frameworks and put every variable into client state. XIS is not a frontend-only framework; controller
model data and server-side flow are first-class parts of the programming model.

Template expressions can read the same storage values, usually inside a storage binding:

```html
<section xis:storage-binding="localStorage">
    <span>${defaultValue(localStorage.cart.count, '0')}</span>
</section>
```

## Plain HTTP Endpoint Annotations

**Normal XIS pages, frontlets, modals, forms, and actions do not need these annotations.** XIS already handles their
browser/server communication. Use these annotations only when you create plain HTTP endpoints for external non-XIS
clients, webhooks, scripts, or integration partners.

| Annotation | Use |
| --- | --- |
| `@Controller` | Marks a class as a plain HTTP controller. |
| `@Get`, `@Post`, `@Put`, `@Delete`, `@Head`, `@Options`, `@Trace` | Maps an HTTP method and path. |
| `@RequestBody` | Injects the request body. |
| `@RequestHeader`, `@ResponseHeader`, `@CookieValue`, `@BearerToken` | Reads or writes HTTP metadata. |
| `@UrlParameter`, `one.xis.http.PathVariable` | Reads URL/query data for plain HTTP controllers. |
| `@Upload` | Reads a multipart file from a plain HTTP controller request. |
| `@Produces` | Declares the response content type. |
| `@PublicResources` | Exposes public static resources. |

Plain HTTP controllers are still XIS components, so constructor injection works as usual. They do not render templates
and they do not participate in page/frontlet model loading. The method return value is written as the HTTP response body.

```java
package example.api;

import one.xis.http.Controller;
import one.xis.http.ContentType;
import one.xis.http.Get;
import one.xis.http.PathVariable;
import one.xis.http.Post;
import one.xis.http.Produces;
import one.xis.http.RequestBody;
import one.xis.http.RequestHeader;
import one.xis.http.ResponseEntity;

@Controller
class CustomerApi {
    private final CustomerService customers;

    CustomerApi(CustomerService customers) {
        this.customers = customers;
    }

    @Get("/api/customers/{id}")
    @Produces(ContentType.JSON)
    CustomerDto find(@PathVariable("id") long id,
                     @RequestHeader("Accept-Language") String language) {
        return customers.findDto(id, language);
    }

    @Post("/api/customers")
    @Produces(ContentType.JSON)
    ResponseEntity<CustomerDto> create(@RequestBody CustomerForm form) {
        var customer = customers.create(form);
        return ResponseEntity.created(customer)
                .addHeader("Location", "/api/customers/" + customer.id());
    }
}
```

Use `@UrlParameter("name")` for query parameters, `one.xis.http.PathVariable` for placeholders in the HTTP route, and
`@RequestBody` for JSON-like request bodies. `@CookieValue` and `@BearerToken` are convenience annotations for common
metadata. Use `ResponseEntity` when the endpoint needs a specific status or dynamic response headers. Use
`@ResponseHeader(name = "...", value = "...")` only for fixed headers declared on a method. The action/page
`one.xis.PathVariable` annotation is a different type and should not be imported for plain HTTP controllers.

## Persistence Annotation Index

SQL and MongoDB annotations live in their own modules. They are listed here so text search finds them from the central
annotation reference; the full behavior belongs in the persistence chapters.

### SQL

| Annotation | Use |
| --- | --- |
| `@Entity` | Maps a class or record to a database table. See [SQL entity mapping](sql.md#entity-mapping). |
| `@Column` | Maps a property to a differently named database column. |
| `@JsonColumn` | Stores a property as JSON in one SQL column. |
| `@NoColumn`, `@Ignore` | Excludes a property from SQL mapping. |
| `@OptionalColumn` | Maps a reusable property only when the column exists. |
| `@Repository` | Marks a SQL repository interface. See [SQL repositories](sql.md#repositories). |
| `@Select` | Executes a query and maps the result. |
| `@Insert`, `@Update`, `@Save`, `one.xis.sql.@Delete` | Execute or derive write operations. |
| `@Param` | Names a repository method parameter for SQL placeholders. |
| `@Transactional` | Opens a transaction through XIS interface advice. See [Transactions](sql.md#transactions). |
| `@Function`, `@StoredProcedure` | Calls database functions and stored procedures. |
| `one.xis.ddl.@ChangeSet` | Defines a stable, persisted DDL migration group and marks the class as a XIS/Spring component. See [DDL builder and change sets](sql.md#ddl-builder-and-change-sets). |
| `one.xis.ddl.@Change` | Marks one DDL migration method inside a change set. See [DDL builder and change sets](sql.md#ddl-builder-and-change-sets). |

### MongoDB

| Annotation | Use |
| --- | --- |
| `@MongoDocument` | Maps a class or record to a MongoDB collection. See [MongoDB documents](mongodb.md#documents). |
| `@MongoId` | Marks a property as the MongoDB `_id` when it is not named `id`. |
| `@MongoField` | Maps a property to a differently named document field. |
| `@MongoIgnore` | Excludes a property from MongoDB mapping. |
| `@MongoRepository` | Marks a MongoDB repository interface. See [MongoDB repositories](mongodb.md#repositories). |
| `@MongoQuery` | Defines an explicit MongoDB JSON query. |
| `@MongoWatch` | Registers a MongoDB change-stream listener. |

## Test Annotation Index

These annotations belong to XIS integration tests, not application runtime code. See [Examples and tests](examples-and-tests.md)
for complete setup examples.

| Annotation | Use |
| --- | --- |
| `@XisBootTest` | Creates a JUnit integration-test context for a XIS page test. |
| `one.xis.test.@InTestContext` | Registers a real field value in the XIS test context and injects it into the test. |
| `one.xis.test.@Mock` | Creates a Mockito mock and registers it in the XIS test context. |
| `one.xis.test.@Spy` | Creates a Mockito-style spy and registers it in the XIS test context. |
| `one.xis.test.@Captor` | Creates a Mockito argument captor for assertions. |

## Advanced And Rarely Needed Annotations

These are public because framework extensions or specialized applications may need them. They are not part of the
normal first application path.

| Annotation | Use |
| --- | --- |
| `@ImportInstances` | Imports component instances into the XIS runtime. |
| `@Proxy` | Marks an annotation as a context proxy annotation. See [Custom proxies](advanced/custom-proxies.md). |
| `@UseAdvice` | Marks an annotation as an interface advice annotation. See [Aspects and interface advice](advanced/aspects.md). |

## Include Syntax

An include always has an annotation and a template:

```java
@Include("header")
class Header {
}
```

```html
<xis:include name="header"/>
```

The attribute form is equivalent:

```html
<header xis:include="header"></header>
```

The string is an include key. It may contain path-like characters if you choose keys such as `layout/header`, but it is
not resolved as a resource path from the template. Only classes annotated with `@Include` are available. This keeps
includes explicit by design: the application decides which fragments may be reused, and templates cannot include
arbitrary files by walking through resource directories.

An include is for markup reuse, not for its own controller state. The included markup is still initialized as part of the
surrounding page or frontlet, so it may use model expressions, XIS links, action buttons, and parameters that belong to
that surrounding controller.
