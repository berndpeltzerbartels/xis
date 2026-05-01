# AGENTS

## Purpose

`xis-distributed` adds optional distributed routing to XIS.

Its job is narrow:

- decide whether a page or widget is remote
- provide the remote host for remote components
- leave local components as same-origin

It does not implement load balancing, service discovery, or config-server behavior.

## Current contract

- `XisDistributedConfig` is the main contract
- local components return `null` host through the resolver path
- remote components must have an explicit host
- missing host for a remote component is an error
- there is no default host and no fallback host

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
