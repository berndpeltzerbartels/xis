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
    id "one.xis.plugin" version "0.12.1"
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

Each participating XIS runtime still exposes its normal `/xis/config`. The shell additionally asks its own runtime for
the distributed host list and then loads `/xis/config` from every listed host. The browser merges those configs.

After merging:

- local pages and frontlets still use same-origin requests
- pages and frontlets from a remote config use that remote host
- page and frontlet ownership comes from the runtime that exposes the config

There are no page or frontlet mappings in `application.properties`.

## Properties Configuration

For simple XIS Boot deployments, configure the remote host list in `application.properties`:

```properties
xis.distributed.hosts=https://components.example.com,https://checkout.example.com
```

Hosts must include protocol and host, for example `http://localhost:9000` or `https://checkout.example.com`.

The list is interpreted from the point of view of the current runtime. In a shell application it lists the remote
runtimes whose `/xis/config` should be loaded. In a remote runtime the same contract is used for CORS, so that runtime
normally lists the shell host that may call it.

When `xis-distributed` is on the classpath, this property is required unless the application provides its own
`XisDistributedConfig` implementation.

## Java Configuration

For dynamic deployments, implement `XisDistributedConfig` as a normal XIS component:

```java
import one.xis.context.Component;
import one.xis.distributed.XisDistributedConfig;

import java.util.List;

@Component
class DistributedConfig implements XisDistributedConfig {

    @Override
    public List<String> getHosts() {
        return List.of("https://components.example.com", "https://checkout.example.com");
    }
}
```

Return stable host values. Future versions may use the same contract for host selection or availability checks.

## Navigation By URL

In same-origin applications, returning a page class or `PageResponse` is often the most convenient navigation style.
In distributed applications, URLs and strings are usually the better boundary between applications. This applies to
pages and frontlets.

The reason is classpath ownership. If an action in the shell application returns a remote page class, the shell
application must have that remote application class on its classpath. That couples the applications at Java level. If the
action returns a URL string instead, the contract is only the public page URL. The browser resolves that URL to the page
entry in the merged client config and uses the host that provided that page.

```java
import one.xis.Action;
import one.xis.Page;

@Page("/cart.html")
class CartPage {

    @Action
    String checkout() {
        return "/checkout.html";
    }
}
```

If `/checkout.html` belongs to the config loaded from `https://checkout.example.com`, rendering and later page actions
for that page go to `https://checkout.example.com`.

Path variables stay part of the concrete URL returned by your action. The remote runtime exposes the normalized page
path in its own config:

```java
@Page("/cart.html")
class CartPage {

    @Action
    String openProductDetails() {
        return "/product/42/details.html";
    }
}
```

The browser resolves `/product/42/details.html` against the merged page config entry for `/product/*/details.html`.

Use Java class returns or `PageResponse` when the target page belongs to the same application or when you deliberately
share the target page classes. Use strings or `PageUrlResponse` when the target page belongs to another distributed
application and the URL is the intended public contract.

The same idea applies to frontlets. If an action should replace a frontlet that lives in another application, return a
frontlet URL instead of a Java class:

```java
import one.xis.Action;
import one.xis.FrontletResponse;

@Action
FrontletResponse openCartSummary() {
    return new FrontletResponse("/cart-summary?mode=compact")
            .targetContainer("header-cart");
}
```

The client resolves `/cart-summary` to the configured frontlet entry from the merged remote config and then uses that
entry's host. The shell application therefore does not need the remote `CartSummaryFrontlet` class on its classpath.
