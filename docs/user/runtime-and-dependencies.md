# Runtime And Dependency Model

[Documentation map](../../README.md)

XIS separates the public programming model from runtime integration. Most controller code and templates look the same
whether the application runs inside Spring, standalone on the JVM, or as a GraalVM native image.

Controllers, components, and form/data objects can be written in Java, Groovy, or Kotlin. Java and Kotlin are supported
on the JVM and with XIS Boot Native; Groovy is supported on the JVM path.

## Runtime Choice

Choose one application runtime:

`build.gradle` for Spring:

```groovy
plugins {
    id "java"
    id "org.springframework.boot" version "3.3.0"
    id "io.spring.dependency-management" version "1.1.5"
    id "one.xis.plugin" version "0.16.2"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation "org.springframework.boot:spring-boot-starter-web"
    implementation "one.xis:xis-spring"
}
```

`build.gradle` for XIS Boot on the JVM:

```groovy
plugins {
    id "java"
    id "one.xis.plugin" version "0.16.2"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation "one.xis:xis-boot" // or xis-spring
}
```

`build.gradle` for standalone HTTP endpoints on XIS Boot:

```groovy
plugins {
    id "java"
    id "one.xis.plugin" version "0.16.2"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation "one.xis:xis-boot-http"
}
```

When the XIS Gradle plugin is used, it aligns XIS dependency versions to the plugin version. Add explicit versions only
in builds that do not use the plugin.

Use `xis-spring` when the application already uses Spring Boot or needs Spring integration.

Use `xis-boot` when you want a small standalone runtime with XIS dependency injection, properties, package scanning, and
an embedded HTTP runtime.

**XIS pages, frontlets, modals, forms, and actions do not need `xis-boot-http`.** XIS already provides the browser/server
transport for normal XIS applications. Use `xis-boot-http` only when you want a standalone XIS Boot application to expose
plain HTTP endpoints for external non-XIS clients, webhooks, scripts, or integration partners.

`xis-boot-http` brings `xis-boot` and `xis-http-controller` into the application API. It provides
`one.xis.http.Controller`, `@Get`, `@Post`, and the other HTTP controller annotations through one dependency. It is
intentionally named HTTP rather than REST because XIS does not try to define or enforce the full REST architectural
style.

Use `xis-boot-native` when you want the XIS Boot programming model and want to build a GraalVM native executable for
small containers, fast startup, and cloud-native deployment. Continue with [Cloud Native And Native Images](cloud-native.md)
for Gradle examples, native database modules, build requirements, and current limitations.

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
| `@QueryParameter` | Binds a query parameter from the current page URL. |
| `@ActionParameter` | Binds a parameter sent by the triggering action element. |
| `@FrontletParameter` | Binds a stable parameter of the current frontlet. |
| `@ModalParameter` | Binds a stable parameter of the current modal. |

## Validation API

The selected runtime also brings validation support into the application.

## XIS Boot

`xis-boot` is the standalone runtime. A minimal application:

```java
package example;

import one.xis.boot.XISBootApplication;
import one.xis.boot.XISBootRunner;

@XISBootApplication
class Application {

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

XIS also uses classpath `public` resources when it builds the root page:

- every `public/**/*.css` resource is added to the root page as a stylesheet
- every `public/**/*.js` resource is added to the root page as a script
- `public/default-main.css` is provided by XIS itself and contains default structural styles for framework-owned DOM
  elements and built-in pages, for example modal dialogs, toast messages, and login forms

You normally do not need to add stylesheet links for application CSS under `public`. Put app-specific CSS there and let
XIS add it to the root page. The generated URLs omit the `public` segment, for example
`src/main/resources/public/css/app.css` becomes `/css/app.css`.

If you want to replace the XIS default styling completely, provide your own `src/main/resources/public/default-main.css`.
An empty file is enough when you want no default styling at all.

If you want to start from the XIS default file instead of an empty replacement, copy it from the resolved dependency:

```groovy
tasks.register("copyXisDefaultMainCss", Copy) {
    from {
        configurations.runtimeClasspath
                .filter { it.name.startsWith("xis-javascript-") }
                .collect { zipTree(it) }
    }
    include "public/default-main.css"
    into "src/main/resources"
}
```

`default-main.css` is not the optional theme. It is the replaceable XIS default stylesheet and is loaded even when the
optional `xis-theme` dependency is not used. Additional theme stylesheets are only relevant when you add `xis-theme`;
see [XIS theme](advanced/theme.md).

In XIS Boot development runs, public assets under `src/main/resources/public` are read from the source file when it is
available. CSS edits therefore become visible after a browser reload without rebuilding the application. Packaged jar
resources remain static.

For JavaScript, prefer [Custom JavaScript and custom EL functions](advanced/custom-javascript.md) when you want to extend
the XIS browser runtime. Files listed through `META-INF/xis/js/extensions` are bundled into `/bundle.min.js`; files under
`public` are loaded as normal root-page scripts.

Optional JavaScript extension artifacts can register browser libraries through the same mechanism. For example,
`one.xis:xis-javascript-jquery` adds the jQuery WebJar and registers `jquery.min.js` as a XIS JavaScript extension.

## Distributed Mode

The default deployment mode is same-origin: pages, frontlets, static assets, and XIS endpoints are served from the same
host.

Distributed page and frontlet delivery is an advanced deployment mode. Use it when one browser application should be
composed from multiple XIS runtimes, for example a shell application plus independently deployed frontlets or pages.

Continue with [Microfrontend Architecture](advanced/microfrontend-architecture.md) when you need this setup.
