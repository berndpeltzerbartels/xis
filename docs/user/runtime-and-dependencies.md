# Runtime and Dependency Model

[Documentation map](../README.md)

XIS separates the public programming model from runtime integration.

## Application Runtime

Choose one application runtime:

`build.gradle` for Spring:

```groovy
plugins {
    id "java"
    id "org.springframework.boot" version "3.3.0"
    id "io.spring.dependency-management" version "1.1.5"
    id "one.xis.plugin" version "0.9.3"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation "org.springframework.boot:spring-boot-starter-web"
    implementation "one.xis:xis-spring"
}
```

`build.gradle` for XIS Boot:

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
}
```

When the XIS Gradle plugin is used, it aligns XIS dependency versions to the plugin version. Add explicit versions only
in builds that do not use the plugin.

Use `xis-spring` when the application already uses Spring Boot or needs Spring integration.

Use `xis-boot` when you want a small standalone runtime with XIS dependency injection, properties, package scanning, and
an embedded HTTP runtime.

## Controller API

The selected runtime brings the controller annotations into the application.

Important annotations include:

| Annotation | Use |
| --- | --- |
| `@Page` | Maps a controller to a page URL. |
| `@WelcomePage` | Marks the default entry page. |
| `@Frontlet` | Marks a reusable frontlet controller. |
| `@Modal` | Marks a modal dialog controller. |
| `@Include` | Registers a reusable HTML include. |
| `@ModelData` | Exposes data to a template. |
| `@FormData` | Provides or receives form-bound data. |
| `@Action` | Exposes a method to template-triggered interactions. |
| `@PathVariable` | Binds a value from the current page URL. |
| `@Parameter` | Binds a parameter passed to a frontlet. |

## Validation API

The selected runtime also brings validation support into the application.

## XIS Boot

`xis-boot` is the standalone runtime. A minimal application:

```java
package example;

import one.xis.boot.XISBootApplication;
import one.xis.boot.XISBootRunner;

@XISBootApplication
public class Application {

    public static void main(String[] args) {
        XISBootRunner.run(Application.class, args);
    }
}
```

`xis-boot` supports:

- package scanning
- dependency injection
- `@Component`, `@Bean`, `@Inject`, `@Value`
- `application.properties`
- `application-<profile>.properties`
- profiles through `xis.profiles` or `XIS_PROFILES`

## Static Resources

Put public assets here:

```text
src/main/resources/public/
```

Reference them from HTML without the `public` segment:

```html
<link rel="stylesheet" href="/css/app.css"/>
<img src="/images/logo.png" alt="Logo"/>
```

In XIS Boot development runs, public assets under `src/main/resources/public` are read from the source file when it is
available. CSS edits therefore become visible after a browser reload without rebuilding the application. Packaged jar
resources remain static.

## Distributed Mode

The default deployment mode is same-origin: pages, frontlets, static assets, and XIS endpoints are served from the same
host.

Distributed page and frontlet delivery is an advanced deployment mode. Use it when one browser application should be
composed from multiple XIS runtimes, for example a shell application plus independently deployed frontlets or pages.

Continue with [Microfrontend Architecture](advanced/microfrontend-architecture.md) when you need this setup.
