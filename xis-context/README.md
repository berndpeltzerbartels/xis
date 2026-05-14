# XIS Context

`xis-context` is the lightweight dependency injection and bean container used by XIS.

It provides the core application context model that is used directly by XIS modules and indirectly by runtimes such as `xis-boot`.

## Core Concepts

The main annotations and concepts are:

- `@Component`
- `@Inject`
- `@Bean`
- `@Init`
- `@Value`
- `@DefaultComponent`

Together they define how singletons are discovered, created, wired, and optionally replaced.

## `@DefaultComponent`

`@DefaultComponent` marks a component as a fallback implementation.

The rule is:

- if only a `@DefaultComponent` exists for a type, it is used
- if a normal `@Component` exists for the same type, the normal component wins

This replacement behavior applies both to:

- single-value injection
- collection injection

That means a default implementation does not stay in the injected collection once non-default implementations for the same type exist.

`@Bean` methods declared on a replaced default component are also removed together with that component.

This makes `@DefaultComponent` useful for framework modules that want to provide replaceable defaults while allowing applications or higher-level modules to override them cleanly.

## Configuration

`xis-context` supports simple property-based configuration through:

- `application.properties`
- `application-<profile>.properties`

Profiles are activated via:

- system property `xis.profiles`
- environment variable `XIS_PROFILES`

`@Value` supports both forms:

- `@Value("app.name")`
- `@Value("${app.name}")`

## Typical Use

You usually do not use `xis-context` alone as an application runtime.

Instead:

- `xis-boot` uses it as the standalone runtime container
- other XIS modules build on it for dependency injection and component replacement

If you want to understand bean wiring, fallback components, or property injection semantics, `xis-context` is the right place to look.
