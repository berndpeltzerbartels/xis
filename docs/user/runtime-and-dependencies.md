# Runtime and Dependency Model

XIS separates the public programming model from runtime integration.

## Application Runtime

Choose one application runtime:

```groovy
dependencies {
    implementation "one.xis:xis-spring"
}
```

or:

```groovy
dependencies {
    implementation "one.xis:xis-boot"
}
```

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
| `@Include` | Registers a reusable HTML include. |
| `@ModelData` | Exposes data to a template. |
| `@FormData` | Provides or receives form-bound data. |
| `@Action` | Exposes a method to template-triggered interactions. |
| `@PathVariable` | Binds a value from the current page URL. |
| `@FrontletParameter` | Binds a parameter passed to a frontlet. |

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

## Distributed Mode

The default deployment mode is same-origin: pages, frontlets, static assets, and XIS endpoints are served from the same
host.

Distributed page and frontlet delivery is an architectural goal. Treat distributed and micro-frontend features as
advanced unless the user documentation or tests for the target release explicitly document them as supported.
