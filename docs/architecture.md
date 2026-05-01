# XIS Architecture

## Purpose

This document describes the current target architecture of XIS with a focus on:

- normal same-origin deployments
- optional distributed applications
- multi-server delivery of pages and widgets
- page and widget host resolution
- SSE refresh events
- practical deployment guidance for cloud environments

It is written for humans and AI agents. It prefers explicit rules over prose.

## Core Principle

XIS is a server-driven UI framework.

The browser does not own application routing or backend contracts. Instead:

- Java page and widget controllers define behavior
- HTML templates define structure
- XIS JavaScript performs transport, rendering, navigation, and refresh handling

The browser may contact different servers for different pages or widgets, but this remains an implementation detail of XIS, not of application code.

## Distributed Goal

The distributed-application goal is:

- pages may be delivered by different servers
- widgets may be delivered by different servers
- the browser still presents one coherent XIS application
- XIS JavaScript decides which server to call for each page or widget
- application code should not manually orchestrate cross-server transport

In other words:

- a page may come from server A
- a widget inside that page may come from server B
- another page may come from server C
- the browser must still assemble these parts into one running UI

This is the architectural reason why XIS needs optional host metadata for pages and widgets.

## Deployment Modes

### Same-Origin Mode

This is the default mode.

In same-origin mode:

- pages and widgets are served from the same host as the main application
- XIS JavaScript uses relative `/xis/...` URLs
- no host metadata is required
- no distributed module is required

This mode should remain the simplest and safest default.

### Distributed Mode

This mode is optional.

In distributed mode:

- different pages may live on different hosts
- different widgets may live on different hosts
- the browser still runs one XIS application
- XIS JavaScript chooses the correct target host per page or widget

Distributed mode must not make same-origin mode harder.

## Architectural Decision

### Decision

Host information is optional.

- If distributed deployment support is not active, `host` must be omitted or `null`.
- If distributed deployment support is active, `host` may be set per page and per widget.

### Reason

Leaving `host` unset in same-origin mode is better than always filling it.

Benefits:

- no unnecessary coupling to deployment topology
- fewer chances for wrong absolute URLs
- fewer cloud-specific surprises
- simpler local development
- same-origin remains the default mental model

This is the right default and should be preserved.

## Page and Widget Addressing

XIS uses two main identifiers:

- `pageId`
  Usually the normalized page path, for example `/product/*.html`
- `widgetId`
  The widget identifier used by the client and server

In distributed mode, resolution works like this:

- `pageId -> host`
- `widgetId -> host`

The host must contain scheme and authority, for example:

- `https://shop.example.com`
- `https://widgets.example.com`

It should not be only a path fragment.

## Recommended Design For Distributed Applications

### Chosen Direction

The authoritative mapping should remain server-side.

That means:

- each participating XIS server instance knows the page/widget-to-host mapping
- the server exposes resolved host metadata to the browser as part of normal XIS configuration
- the browser uses this metadata when it must call another host

### Why this is better than a client-only config file

A pure client-side host file sounds simple, but it does not actually solve the server concerns:

- CORS headers are still decided by servers, not by the browser
- every participating server still needs to know which browser origins it accepts
- authentication and cookie behavior remain server-side concerns
- a client-only file creates a second topology source that can drift from server reality

So the better split is:

- topology mapping: resolved on the server
- execution of cross-host requests: done in the browser

### Practical Interpretation

Each XIS instance that serves pages or widgets in a distributed deployment should have access to the same logical mapping.

That mapping can come from:

- a local file
- environment variables
- mounted config
- cloud configuration system

But from the browser perspective, it should simply arrive through normal XIS config.

## Configuration Strategy

### Recommended For First Usable Release

Prefer Java-based configuration through an extension interface that is imported from the host framework.

This is the recommended first version because it keeps the distributed module framework-neutral while still allowing
framework-specific configuration styles at the application edge.

The distributed module should define an interface such as:

```java
@ImportInstances
public interface DistributedComponentMapping {

    String getPageHost(String normalizedPageId);

    String getWidgetHost(String widgetId);
}
```

The exact interface name can differ, but the idea is:

- the distributed module owns the abstraction
- host frameworks provide implementations
- XIS imports these implementations through `@ImportInstances`

This configuration must express:

- default local or public host
- page host mappings
- widget host mappings

### Why Java configuration is the preferred default

Pros:

- framework-neutral distributed core
- no mandatory extra config file format
- good fit for Spring, because users can still bind from `application.yml` into beans
- good fit for other runtimes, because they can produce the same interface differently
- supports computed or environment-dependent mappings

Cons:

- more code than a plain property file
- less convenient for very static setups
- needs one explicit extension contract

Conclusion:

This is the best default for now.

### Role of framework-specific modules

The distributed core module should not assume:

- Spring
- YAML
- any specific property binding mechanism

If users want file-based configuration, that belongs at the runtime integration layer, for example:

- Spring reads `application.yml`
- Spring binds it into a bean implementing the distributed mapping interface
- XIS imports that bean through `@ImportInstances`

This keeps the core architecture clean.

### Optional file-based configuration

File-based configuration is still possible, but it should be implemented by specialized runtime modules, not forced by
the distributed core.

For example, a Spring integration may support a shape like:

```yaml
xis:
  distributed:
    pageHosts:
      "/products/*.html": "https://shop.example.com"
      "/orders/*.html": "https://orders.example.com"
    widgetHosts:
      "StockTickerWidget": "https://widgets.example.com"
      "CartSummaryWidget": "https://shop.example.com"
```

This YAML is not the architecture itself. It is only one possible runtime-level way to create the Java mapping bean.

### Why not require a dedicated standalone properties file

That would be lightweight technically, but it is not the best user experience.

Problems:

- one more special XIS-only config file
- worse integration with framework-native config systems
- less convenient for profile-specific cloud setups
- users of Spring usually expect everything relevant to fit into `application.yml`

Conclusion:

Do not make a dedicated XIS-specific properties file the primary required model.

### Important Rule

This configuration should use public or browser-reachable base URLs.

Do not use:

- pod names
- internal JVM hostnames
- container-local addresses
- private service names that the browser cannot resolve

In cloud environments, the mapping should usually point to:

- ingress URLs
- gateway URLs
- externally resolvable DNS names

## Alternatives Considered

### 1. Client-side file containing all server names

Possible, but not preferred as the primary model.

Pros:

- simple mental model
- no extra backend-to-backend communication

Cons:

- duplicates topology knowledge on client and server side
- does not remove the need for server-side CORS policy
- adds another boot-time artifact to keep in sync
- less controlled than normal `/xis/config` handling

Conclusion:

Acceptable only as a secondary implementation detail. Not recommended as the primary architecture.

### 2. One server pulls config from other servers

Not recommended for now.

Pros:

- automatic aggregation is possible

Cons:

- adds coupling between servers
- harder startup behavior
- harder failure behavior
- more moving parts
- not required to reach a useful first release

Conclusion:

Avoid for now.

### 3. Dedicated configuration server

Architecturally possible, but not recommended for the first finished version.

Pros:

- single central source
- familiar pattern in some cloud stacks

Cons:

- additional infrastructure
- startup dependency
- operational complexity
- soft single point of failure during boot

Conclusion:

Reasonable future option, but too much weight for the current goal.

## Cloud Guidance

Cloud deployments require special care because hostnames are not always transparent.

### User Guidance

Users should configure stable browser-visible base URLs, not internal node addresses.

Good examples:

- `https://shop.company.com`
- `https://widgets.company.com`
- `https://orders.company.com`

Bad examples:

- `http://pod-17:8080`
- `http://orders-service.default.svc.cluster.local`
- `http://localhost:8080` in production

### What XIS Should Provide

XIS should give users:

- an explicit host resolver SPI
- an explicit Java extension interface for distributed host mappings
- a documented configuration format
- clear distinction between same-origin and distributed mode
- examples for ingress-based deployments

XIS should not require users to understand hidden reverse-proxy internals unless their deployment already depends on them.

## CORS

Distributed mode implies cross-origin browser requests.

That means every server that may receive cross-origin XIS requests must set appropriate CORS headers.

Important:

- CORS is a server-side policy
- client-side mapping does not replace CORS handling

Minimum expectations:

- allow the requesting origin
- allow credentials if authentication requires them
- allow required methods and headers

If authentication uses cookies across sites, deployment may also need:

- `SameSite=None`
- `Secure`

These are deployment concerns and should be documented clearly for users.

## JavaScript Client Responsibilities

The XIS browser client should:

- load the normal XIS configuration
- keep the same-origin fast path when no host is present
- prepend scheme + host only when a page or widget has an explicit remote host
- continue using relative URLs for same-origin components

This keeps the distributed feature additive instead of invasive.

## SSE In Distributed Deployments

SSE remains a notification channel.

Refresh events:

- send only event keys
- do not transport business payloads
- trigger reload of affected pages or widgets through normal XIS requests

In distributed deployments this means:

- the event channel is still lightweight
- refreshed data is always fetched from the component's owning host

This fits the distributed model well.

## Load Balancing

XIS should not try to become a general-purpose load balancer.

For the first usable distributed version:

- XIS resolves component ownership
- the configured host may itself be a load-balanced public endpoint
- real balancing remains the responsibility of ingress, gateway, proxy, or platform tooling

A list of hosts per component may be possible later, but it is not needed for the first finished release.

## Rules Summary

- Same-origin mode is the default.
- In same-origin mode, `host` is omitted or `null`.
- Distributed mode is optional.
- Distributed mode resolves `pageId -> host` and `widgetId -> host`.
- Host values must be browser-reachable base URLs.
- The server remains the source of truth for host resolution.
- The browser performs the actual cross-host requests.
- CORS must be configured on every participating server.
- XIS should not add config-server or server-to-server aggregation complexity in the first finished version.

## Recommended Next Step

The next implementation step should be:

1. Finalize the distributed host resolver and Java extension contract.
2. Keep `host` absent in same-origin mode.
3. Expose resolved page/widget hosts through normal client configuration.
4. Extend the JavaScript transport path so requests use `host + /xis/...` when a remote host is defined.
5. Add runtime-specific adapters, for example Spring bean binding from `application.yml`.
