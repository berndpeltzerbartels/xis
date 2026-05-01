# XIS Boot

`xis-boot` is the lightweight standalone runtime for XIS applications.

It is meant for applications that want to run XIS without Spring Boot. The goal is a small runtime model with very little magic:

- package scanning
- dependency injection
- bean factory methods
- property injection with `@Value`
- profile-aware `application.properties`
- embedded Netty-based HTTP runtime

`xis-boot` is intentionally much smaller than Spring Boot. It does not try to provide the same breadth of configuration formats, auto-configuration layers, or infrastructure abstractions.

## When to use it

Use `xis-boot` when you want:

- a standalone XIS application
- a small runtime with predictable behavior
- simple configuration without Spring Boot
- fast startup and low conceptual overhead

If you already use Spring Boot, prefer `xis-spring`.

## Entry Point

Mark your application class with `@XISBootApplication` and start it through `XISBootRunner`:

```java
package com.example.demo;

import one.xis.boot.XISBootApplication;
import one.xis.boot.XISBootRunner;

@XISBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        XISBootRunner.run(DemoApplication.class, args);
    }
}
```

The runtime scans the package of the application class and builds a lightweight XIS `AppContext`.

## Configuration

`xis-boot` supports configuration through Java properties files.

Supported files:

- `application.properties`
- `application-<profile>.properties`

These files are loaded from the classpath.

Later profile-specific files override values from `application.properties`.

### Profiles

Profiles are activated through:

- system property: `xis.profiles`
- environment variable: `XIS_PROFILES`

Examples:

```bash
java -Dxis.profiles=dev -jar app.jar
```

```bash
export XIS_PROFILES=dev,local
```

## `@Value`

Yes, `xis-boot` supports `@Value` through `xis-context`.

Example:

```java
package com.example.demo.config;

import one.xis.context.Component;
import one.xis.context.Value;

@Component
public class DemoConfig {

    @Value("app.title")
    private String title;

    @Value("${app.port}")
    private int port;

    public String getTitle() {
        return title;
    }

    public int getPort() {
        return port;
    }
}
```

Matching `application.properties`:

```properties
app.title=Demo Application
app.port=8080
```

Both forms are supported:

- `@Value("app.title")`
- `@Value("${app.title}")`

If a referenced property is missing, startup fails with a clear error.

## Bean Configuration

Besides component scanning, `xis-boot` also supports bean factory methods via `@Bean`.

```java
package com.example.demo.config;

import one.xis.context.Bean;
import one.xis.context.Component;

@Component
public class AppConfiguration {

    @Bean
    public Clock systemClock() {
        return Clock.systemUTC();
    }
}
```

## Spring Compatibility

`xis-boot` can recognize a small subset of familiar Spring annotations when they are present on the classpath, for example component stereotypes and `@Bean`.

This exists to make migration easier, not to turn `xis-boot` into a Spring clone.

## What `xis-boot` deliberately does not aim to be

`xis-boot` is intentionally conservative.

It does not aim to provide:

- YAML or TOML configuration as a core requirement
- large auto-configuration layers
- many alternative configuration models
- the full Spring ecosystem programming model

That is a design choice, not a missing feature.

The idea is:

- plain Java where possible
- small and explicit infrastructure
- only the minimum configuration mechanism needed by the runtime

## Port Configuration

The HTTP port can be passed as the first application argument:

```bash
java -jar app.jar 9090
```

If no argument is provided, the runtime uses the default server port configured by the Netty server component.

## Summary

`xis-boot` is the standalone, lightweight way to run XIS.

It gives you:

- standalone runtime
- package scanning
- dependency injection
- `@Bean`
- `@Value`
- `application.properties`
- profile-specific property files

without requiring the larger Spring Boot model.
