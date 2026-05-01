# XIS Distributed

`xis-distributed` adds distributed routing support to XIS applications.

It is intended for applications where pages and widgets may be served by different servers, while the browser still behaves like one coherent XIS application.

## What the module does

The module contributes a `ComponentHostResolver` implementation.

That resolver fills the optional `host` field in:

- page attributes
- widget attributes

The JavaScript client then uses that information to decide how to send requests:

- `host == null`: same-origin, normal relative XIS requests
- `host != null`: send the request to the configured remote host

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

The interface distinguishes between:

- components that are remote
- components that are local

and only remote components are expected to return a host.

## Default implementation

The module also contains `PropertiesXisDistributedConfig`.

This is a `@DefaultComponent`, which means:

- it acts as the fallback implementation shipped by the module
- a normal application `@Component` can replace it
- a Spring bean implementing `XisDistributedConfig` can replace it as well

So the architectural contract is the interface, not the properties-based implementation.

## Properties format

If you use the built-in properties-based implementation, it reads explicit remote mappings from:

- `xis.remote.widget.<widgetId>`
- `xis.remote.page.<normalizedPath>`

Example:

```properties
xis.remote.widget.ProductWidget=https://shop.example.com
xis.remote.page./product/*.html=https://shop.example.com
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
- explicit widget mappings
- local components
- missing host errors for declared remote components

Broader distributed end-to-end scenarios are expected to live in a larger integration setup later, likely with multiple runtimes and Docker-based composition.
