# AGENTS

## Purpose

`xis-distributed` adds optional distributed routing to XIS.

Its job is narrow:

- accept maps of remote page/frontlet hosts from user configuration
- derive whether a page or frontlet is remote from those cached maps
- provide the remote host for remote components through the internal resolver
- leave local components as same-origin

It does not implement load balancing, service discovery, or config-server behavior.

## Current contract

- `XisDistributedConfig` is the main contract
- local components return `null` host through the resolver path
- remote components must have an explicit host
- missing host for a remote component is an error
- there is no default host and no fallback host
- `XisDistributedConfig` should stay map-based; do not make users implement separate `isRemote...` methods
- `DistributedComponentHostResolver` copies the user-provided maps once at startup, because a config implementation may
  perform expensive discovery behind those map methods
- page host map keys are normalized page URLs such as `/product/*/details.html`, not fully qualified Java class names

## Important implementation notes

- `@ImportInstances` on `XisDistributedConfig` is internal framework plumbing
- do not document `@ImportInstances` as part of the user-facing API
- `PropertiesXisDistributedConfig` is only a `@DefaultComponent` implementation
- a normal `@Component` or runtime-provided bean may replace it

## JavaScript integration

Do not reintroduce a second distributed routing path in this module.

The active request-routing logic lives centrally in `xis-javascript` `HttpClient`.

If distributed behavior changes, prefer changing the shared routing logic there instead of adding another JS patch layer here.

## Testing guidance

Current repo tests here should stay focused on:

- config semantics
- resolver semantics
- strict remote-vs-local behavior

Do not overbuild multi-runtime distributed scenarios inside this module.

Larger end-to-end tests with different hosts, runtimes, or containers belong in a broader integration setup, not in the module-level unit tests.
