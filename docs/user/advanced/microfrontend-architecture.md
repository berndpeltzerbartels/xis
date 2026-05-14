# Microfrontend Architecture

[Documentation map](../../README.md)

Microfrontend Architecture means that a service does not only own business logic and data access; it can also own the
frontend fragment that presents that part of the domain. In XIS, those fragments are pages and frontlets. A browser
application can therefore be composed from more than one XIS runtime, for example a shell application that renders the
main page while selected pages or frontlets are served by another process.

Most applications do not need this. The normal mode is same-origin: the page, its frontlets, and all XIS endpoints come
from the same host.

## Dependency

Add `xis-distributed` to each participating XIS application.

`build.gradle`

```groovy
plugins {
    id "java"
    id "one.xis.plugin" version "0.9.3"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation "one.xis:xis-boot" // or xis-spring
    implementation "one.xis:xis-distributed"
}
```

## Routing Model

The browser receives the normal XIS client configuration. For every page and frontlet the configuration may contain a
host:

- no host means same-origin
- a host means requests for that page or frontlet go to that remote XIS runtime

This applies to rendering and actions. If a frontlet is remote, its action requests are sent to the remote frontlet host.
If a page is remote, page rendering and page actions are sent to the remote page host.

## Navigation By URL

In same-origin applications, returning a page class or `PageResponse` is often the most convenient navigation style.
In distributed applications, URLs and strings are usually the better boundary between applications. This applies to
pages and frontlets.

The reason is classpath ownership. If an action in the shell application returns a remote page class, the shell
application must have that remote application class on its classpath. That couples the applications at Java level. If the
action returns a URL string instead, the contract is only the public page URL. The client resolves that URL to the page
entry in its configuration and uses the configured host for that page.

```java
import one.xis.Action;
import one.xis.Page;

@Page("/cart.html")
public class CartPage {

    @Action
    public String checkout() {
        return "/checkout.html";
    }
}
```

With this configuration, `/checkout.html` is loaded from the checkout runtime:

```properties
xis.remote.page./checkout.html=https://checkout.example.com
xis.remote.origin.shell=https://shop.example.com
```

The action returns only the URL. XIS then resolves the URL to the configured page metadata in the browser. Because that
metadata contains `https://checkout.example.com` as host, the browser loads the page and its later page actions from the
checkout application.

Path variables stay part of the concrete URL returned by your action. Only the configuration key is normalized:

```java
@Page("/cart.html")
public class CartPage {

    @Action
    public String openProductDetails() {
        return "/product/42/details.html";
    }
}
```

```properties
xis.remote.page./product/*/details.html=https://catalog.example.com
```

The browser resolves `/product/42/details.html` against the page configuration entry for
`/product/*/details.html` and sends the request to the catalog runtime.

Use Java class returns or `PageResponse` when the target page belongs to the same application or when you deliberately
share the target page classes. Use strings or `PageUrlResponse` when the target page belongs to another distributed
application and the URL is the intended public contract.

The same idea applies to frontlets. If an action should replace a frontlet that lives in another application, return a
frontlet URL instead of a Java class:

```java
import one.xis.Action;
import one.xis.FrontletResponse;

@Action
public FrontletResponse openCartSummary() {
    return new FrontletResponse("/cart-summary?mode=compact")
            .targetContainer("header-cart");
}
```

The client resolves `/cart-summary` to the configured frontlet entry and then uses that entry's host. The shell
application therefore does not need the remote `CartSummaryFrontlet` class on its classpath.

## Properties Configuration

For simple deployments, configure explicit mappings in `application.properties`:

```properties
xis.remote.frontlet.CartSummaryFrontlet=https://shop-components.example.com
xis.remote.frontlet-url.CartSummaryFrontlet=/cart-summary
xis.remote.page./checkout.html=https://checkout.example.com
xis.remote.origin.shell=https://shop.example.com
```

`xis.remote.frontlet.*` maps a frontlet id to a remote host.

`xis.remote.frontlet-url.*` maps a frontlet id to its public frontlet URL.

`xis.remote.page.*` maps a normalized page path to a remote host.

For pages, the key is not the Java class name. It is the normalized page URL that also appears in the XIS client
configuration. Static URLs are used as-is:

```properties
xis.remote.page./checkout.html=https://checkout.example.com
```

Path variables are normalized to `*`. A page declared as:

```java
@Page("/product/{id}/details.html")
public class ProductDetailsPage {
}
```

is configured like this:

```properties
xis.remote.page./product/*/details.html=https://catalog.example.com
```

The user-facing URL still contains the real path value. For example, an action can return
`/product/42/details.html`; only the host map uses `/product/*/details.html`.

This is deliberate. In a distributed application, the stable contract between applications should be the public URL. A
fully qualified Java class name would make the shell application depend on another team's package names and refactorings.

`xis.remote.origin.*` declares browser origins that are allowed to call remote XIS endpoints. This is required because a
distributed application uses cross-origin `/xis/*` requests.

There is no default host. Unmapped pages and frontlets stay local.

XIS derives the remote/local decision from these mappings. You do not implement a second `isRemote...` decision in
normal application code. At startup, XIS copies the configured maps into its resolver and then uses that copy for
request routing and client configuration.

## Java Configuration

For dynamic deployments, implement `XisDistributedConfig` as a normal XIS component:

```java
import one.xis.context.Component;
import one.xis.distributed.XisDistributedConfig;

import java.util.Map;
import java.util.Set;

@Component
public class DistributedConfig implements XisDistributedConfig {

    @Override
    public Map<String, String> getFrontletHosts() {
        return Map.of("CartSummaryFrontlet", "https://shop-components.example.com");
    }

    @Override
    public Map<String, String> getFrontletUrls() {
        return Map.of("CartSummaryFrontlet", "/cart-summary");
    }

    @Override
    public Map<String, String> getPageHosts() {
        return Map.of(
                "/checkout.html", "https://checkout.example.com",
                "/product/*/details.html", "https://catalog.example.com"
        );
    }

    @Override
    public Set<String> getAllowedOrigins() {
        return Set.of("https://shop.example.com", "https://shop-components.example.com", "https://checkout.example.com");
    }
}
```

The method names still use the internal `Frontlet` wording for compatibility. In user-facing templates and documentation,
the public concept is a frontlet.

Return stable maps from this component. XIS reads them when the distributed resolver is created, validates that remote
hosts are not blank, and stores its own copy. That keeps routing predictable and prevents expensive lookup code from
running on every request.
