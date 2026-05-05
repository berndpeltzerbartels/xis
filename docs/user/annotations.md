# Annotation Reference

This page is the compact map of the public annotations a XIS user can meet while building an application. The workflow
chapters explain the details; this page makes sure class, method, field, record-component, and parameter annotations are
findable in one place.

Most applications start with `@Page`, `@ModelData`, `@Action`, `@FormData`, `@PathVariable`, and `@QueryParameter`.
Use [Core model](core-model.md), [Navigation and responses](navigation.md), [Forms and validation](forms-and-validation.md),
and [Security](security.md) for complete examples.

## Class Annotations

| Annotation | Use |
| --- | --- |
| `@Page("/path.html")` | Marks a class as a page controller and maps it to a browser URL. |
| `@WelcomePage` | Marks the page that should be used for `/`. |
| `@Frontlet` | Marks a reusable UI controller that renders a replaceable HTML fragment. |
| `@Include("name")` | Registers a reusable HTML fragment that can be inserted with `xis:include` or `<xis:include>`. |
| `@HtmlFile` | Uses a template file whose name does not follow the default class-name convention. |
| `@DefaultHtmlFile` | Defines a default template file for a package or type. |
| `@Roles` | Protects a page or frontlet by role. See [Security](security.md). |
| `@RefreshOnUpdateEvents` | Refreshes a page or frontlet when one of the configured update events is fired. See [Refresh events](advanced/refresh-events.md). |
| `@JavascriptExtension` | Adds a JavaScript extension class for advanced client behavior. |
| `@CssFile` | Adds a CSS file for a page or component. |
| `@XISBootApplication` | Marks the application entry point for standalone XIS Boot applications. |
| `@Component`, `@Service` | Registers ordinary application services in the XIS context. |
| `@DefaultComponent` | Registers a replaceable default component, mostly useful for libraries and framework extensions. |

## Method Annotations

| Annotation | Use |
| --- | --- |
| `@ModelData` | Exposes a value to the template. The default key is the method/property name. |
| `@FormData` | Loads form data for a named form binding. |
| `@Action` | Marks a method that can be called from an action link, action button, or form submit. |
| `@Title` | Supplies a page or frontlet title from Java. |
| `@SharedValue` | Provides a named value for other controller methods during the same request/action processing flow. |
| `@LocalStorage`, `@SessionStorage`, `@ClientStorage` | Reads or writes browser-side state. Use only when client state is actually useful. |
| `@LocalDatabase` | Reads or writes browser-side database state. This is an advanced client-state feature. |
| `@Bean` | Creates a XIS context bean from a method. |
| `@Init` | Runs initialization code after dependency injection. |
| `@EventListener` | Handles XIS context events. |
| `@Scheduled` | Runs scheduled work in the XIS context. This is not needed for normal page development. |

### Shared Values

Use `@SharedValue` when one controller processing flow needs the same object in several methods. A typical case is
loading an aggregate once and then using it for display and for an action without duplicating the lookup code.

```java
@Page("/products/{id}.html")
public class ProductPage {

    private final ProductService productService;

    public ProductPage(ProductService productService) {
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

## Parameter, Field, And Record Component Annotations

These annotations can appear on action/model/form parameters, DTO fields, or record components depending on their
purpose.

| Annotation | Use |
| --- | --- |
| `@PathVariable("name")` | Reads a value from a `@Page` path such as `/orders/{id}.html`. |
| `@QueryParameter("name")` | Reads a query parameter such as `/orders.html?status=open`. |
| `@ActionParameter("name")` | Reads a value supplied by `<xis:parameter>`. |
| `@FrontletParameter("name")` | Reads a parameter supplied when a frontlet is loaded. |
| `@FormData("name")` | Injects submitted form data into an action method. |
| `@SharedValue("name")` | Injects a value produced by another `@SharedValue` method in the same controller processing flow. |
| `@LocalStorage`, `@SessionStorage`, `@ClientStorage` | Injects browser-side state into an action method parameter. |
| `@LocalDatabase` | Injects browser-side database state. This is an advanced client-state feature. |
| `@ClientId` | Injects the browser client id. |
| `@UserId` | Injects the authenticated user id. |
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

### Browser Storage Parameters

`@LocalStorage`, `@SessionStorage`, and `@ClientStorage` do not send the whole browser store to the server. XIS scans the
controller methods, collects the storage keys used by annotated parameters, and writes those keys into the client
configuration for the page or frontlet. The browser then sends those configured keys with requests for that page or
frontlet.

Use storage parameters for values that intentionally live in the browser:

```java
public class CartPage {

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

Template expressions can read the same storage values, usually inside a storage binding:

```html
<section xis:storage-binding="localStorage">
    <span>${defaultValue(localStorage.cart.count, '0')}</span>
</section>
```

## Plain HTTP Endpoint Annotations

Normal XIS pages do not need these annotations. Use them only when you create plain HTTP endpoints next to the XIS UI.

| Annotation | Use |
| --- | --- |
| `@Controller` | Marks a class as a plain HTTP controller. |
| `@Get`, `@Post`, `@Put`, `@Delete`, `@Head`, `@Options`, `@Trace` | Maps an HTTP method and path. |
| `@RequestBody` | Injects the request body. |
| `@RequestHeader`, `@ResponseHeader`, `@CookieValue`, `@BearerToken` | Reads or writes HTTP metadata. |
| `@UrlParameter`, `one.xis.http.PathVariable` | Reads URL/query data for plain HTTP controllers. |
| `@Produces` | Declares the response content type. |
| `@PublicResources` | Exposes public static resources. |

## Advanced And Rarely Needed Annotations

These are public because framework extensions or specialized applications may need them. They are not part of the
normal first application path.

| Annotation | Use |
| --- | --- |
| `@Address` | Identifies an addressable component in distributed or messaging-oriented scenarios. |
| `@PushRecipients` | Selects recipients for push/update delivery. |
| `@ImportInstances` | Imports component instances into the XIS runtime. |
| `@MainClass` | Legacy application metadata; normal applications should use the runtime setup instead. |
| `@Proxy` | Marks a context proxy type. |

## Include Syntax

An include always has an annotation and a template:

```java
@Include("header")
public class Header {
}
```

```html
<xis:include name="header"/>
```

The attribute form is equivalent:

```html
<header xis:include="header"></header>
```

An include is for markup reuse, not for its own controller state. The included markup is still initialized as part of the
surrounding page or frontlet, so it may use model expressions, XIS links, action buttons, and parameters that belong to
that surrounding controller.
