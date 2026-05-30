# Reusable Web Artifacts

[Documentation map](../../../README.md)

XIS libraries can ship usable web functionality, not only Java classes. A reusable artifact can contain controllers,
default templates, static resources, CSS, and JavaScript extensions. The application gets a working default UI from the
library and can still replace the parts that should follow its own product design.

This matters because reuse in web applications often fails at the template boundary. A backend component may be reusable,
but the HTML around it usually has to fit the consuming application. XIS keeps that boundary explicit.

## Replaceable Templates

Use `@HtmlFile` together with `@DefaultHtmlFile` when a controller should first look for an application template and then
fall back to a library-provided default.

```java
package company.account;

import one.xis.DefaultHtmlFile;
import one.xis.HtmlFile;
import one.xis.Page;

@Page("/account/login.html")
@HtmlFile("/account/login.html")
@DefaultHtmlFile("/company-default-login.html")
class CompanyLoginController {
}
```

With this setup XIS resolves templates in this order:

1. `/account/login.html`, usually supplied by the application.
2. `/company-default-login.html`, supplied by the library artifact.
3. The normal controller-name convention, if neither annotated resource exists.

The controller, model methods, form data, actions, validation, events, and routing behavior stay in the library. The
application only replaces the HTML template when it needs a different layout or design.

The built-in `LoginFormController` follows this pattern. It is mapped to `/login.html`, looks for `/login.html` first,
and falls back to `/default-login.html` from `xis-authentication`.

## Default Functionality First

A reusable artifact should usually provide a default template even when teams are expected to replace it. That gives
users an immediately working feature and a living example of the required bindings, actions, message areas, and model
data.

For example, a reusable CRM artifact could provide:

- a customer search controller
- a default `customer-search.html`
- CSS that makes the default screen usable
- actions for opening a customer detail modal
- refresh events after updates

The consuming application can then replace only `customer-search.html` and keep the controller behavior.

## JavaScript Extensions

Reusable artifacts can also ship browser-side extensions. Put extension declarations in:

```text
src/main/resources/META-INF/xis/js/extensions
```

and list JavaScript resource paths in that file. XIS reads every extension declaration on the classpath and bundles the
listed scripts into the browser runtime.

This makes company-specific or product-specific browser behavior easy to ship as a dependency:

```groovy
dependencies {
    implementation "com.example:company-browser-extensions:1.0.0"
}
```

Inside that artifact, the extension registry can point to ordinary classpath resources:

```text
# src/main/resources/META-INF/xis/js/extensions
company/browser/company-ui.js
company/browser/usage-tracking.js
```

The listed scripts may also come from transitive dependencies. XIS' `xis-javascript-jquery` module uses that variant: it
depends on the jQuery WebJar and registers the WebJar's `jquery.min.js` resource.

This allows a library to deliver several layers together:

- Java controllers and services
- default templates
- CSS and static resources
- custom EL functions
- browser behavior that belongs to the feature

Use this sparingly. Normal application behavior should stay in Java controllers and XIS templates. JavaScript extensions
are useful when the reusable feature really needs browser-specific behavior, custom expression-language functions, or an
integration with a browser library.

See also [Template location and mapping](../template-location-and-mapping.md) and
[Custom JavaScript and custom EL functions](custom-javascript.md).
