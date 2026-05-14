# XIS Distributed

`xis-distributed` adds distributed routing support to XIS applications.

It is intended for applications where pages and frontlets may be served by different servers, while the browser still behaves like one coherent XIS application.

## What the module does

The module contributes a `ComponentHostResolver` implementation.

That resolver fills the optional `host` field in:

- page attributes
- frontlet attributes

The JavaScript client then uses that information to decide how to send requests:

- `host == null`: same-origin, normal relative XIS requests
- `host != null`: send the request to the configured remote host

The module also contributes the distributed CORS policy. Cross-origin XIS calls are allowed only for origins that belong
to the configured distributed application.

## Important contract

`xis-distributed` is only about **remote** components.

That means:

- local components stay local
- local components do not need a host mapping
- local components return `null` as host

There is deliberately:

- no default host
- no implicit fallback
- no hidden routing behavior

If a component is declared as remote, it must have an explicit host mapping.

## Main API

The central contract is `XisDistributedConfig`.

Applications can provide their own implementation when they want full control over distributed routing.

Applications normally provide maps of remote frontlet and page hosts. XIS derives whether a component is remote from
those maps, copies them once when the distributed resolver starts, and uses that copy for routing. User implementations
should not perform expensive per-request checks in this API.

## Default implementation

The module also contains `PropertiesXisDistributedConfig`.

This is a `@DefaultComponent`, which means:

- it acts as the fallback implementation shipped by the module
- a normal application `@Component` can replace it
- a Spring bean implementing `XisDistributedConfig` can replace it as well

So the architectural contract is the interface, not the properties-based implementation.

## Properties format

If you use the built-in properties-based implementation, it reads explicit remote mappings from:

- `xis.remote.frontlet.<frontletId>`
- `xis.remote.frontlet-url.<frontletId>`
- `xis.remote.page.<normalizedPath>`
- `xis.remote.origin.<name>`

For page mappings, `<normalizedPath>` is the page URL normalized by XIS, not the Java class name. Static page URLs are
used as-is, for example `/checkout.html`. Path variables become `*`, so `@Page("/product/{id}/details.html")` is mapped
as `/product/*/details.html`. The concrete navigation URL still contains the real value: an action may return
`/product/42/details.html`, while the host mapping remains `/product/*/details.html`. This keeps distributed
applications coupled through public URLs instead of package and class names that another team may refactor.

Example:

```properties
xis.remote.frontlet.ProductFrontlet=https://shop.example.com
xis.remote.frontlet-url.ProductFrontlet=/product-summary
xis.remote.page./product/*.html=https://shop.example.com
xis.remote.page./product/*/details.html=https://catalog.example.com
xis.remote.origin.shell=https://app.example.com
```

There is no `xis.host` fallback.

Unmapped components are treated as local by that default implementation.

## Relationship to other modules

- `xis-server` provides the no-op fallback resolver used when `xis-distributed` is not present
- `xis-javascript` performs the actual request routing based on the `host` value in client config
- `xis-context` provides `@DefaultComponent`

## Testing status

The module is covered by unit tests for:

- explicit page mappings
- explicit frontlet mappings
- configured distributed CORS origins
- local components
- startup validation for blank remote host mappings

Browser-level end-to-end coverage lives in `xis-end-to-end-tests` and starts separate page and remote runtimes.
