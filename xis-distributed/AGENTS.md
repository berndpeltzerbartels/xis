# AGENTS

## Purpose

`xis-distributed` adds optional distributed routing to XIS.

Its job is narrow:

- accept a list of remote hosts from user configuration
- expose that host list to the browser
- let the browser load and merge each host's normal XIS client config
- leave local components as same-origin

It does not implement load balancing, service discovery, or config-server behavior.

## Current contract

- `XisDistributedConfig` is the main contract
- it exposes only remote hosts
- missing hosts are an error when the module is on the classpath
- there is no default host and no fallback host
- page/frontlet routing is derived from the remote `/xis/config` responses, not from server-side maps

## Important implementation notes

- `@ImportInstances` on `XisDistributedConfig` is internal framework plumbing
- do not document `@ImportInstances` as part of the user-facing API
- `PropertiesXisDistributedConfig` is only a `@DefaultComponent` implementation
- a normal `@Component` or runtime-provided bean may replace it

## JavaScript integration

The active request-routing logic lives centrally in `xis-javascript` `HttpClient`.

If distributed behavior changes, prefer changing the shared routing logic there instead of adding another JS patch layer here.

## Testing guidance

Current repo tests here should stay focused on:

- config semantics
- host-list endpoint semantics
- distributed CORS behavior

Do not overbuild multi-runtime distributed scenarios inside this module.

Larger end-to-end tests with different hosts, runtimes, or containers belong in a broader integration setup, not in the module-level unit tests.
