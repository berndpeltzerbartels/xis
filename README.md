# XIS

**XIS is a lightweight SPA framework for Java and Groovy: annotation-driven, fast, and built around backend controllers
plus plain HTML templates. XIS provides ready-to-use client-server transport, state synchronization, navigation, form
handling, validation feedback, and partial UI refreshes without extra work and without any boilerplate code. It runs
standalone on the JVM with XIS Boot, as a GraalVM native executable for cloud-native deployment with XIS Boot Native,
or inside Spring. It supports distributed Microfrontend Architecture, and in most applications you write just annotated POJO
controllers and templates. Coding is as simple as in the old days of request-response, but the result is a modern and
fast SPA application.**

The framework keeps application code close to the feature it implements. Instead of maintaining REST endpoints, DTOs,
frontend API clients, client-side state management, and duplicated validation rules, most features are built from a
server-side controller and the HTML template that presents it.

The programming model is server-driven: Java or Groovy controllers describe pages, frontlets, actions, form data, and model data.
HTML templates describe the UI. XIS handles browser-server communication, navigation, form submission, validation
feedback, frontlet updates, and SPA-style page transitions.

## Why XIS Exists

Many web applications end up with the same split:

- Java or Groovy backend controllers
- REST endpoints and DTOs
- JavaScript or TypeScript API clients
- frontend state management
- duplicated validation and navigation rules

XIS removes most of that coordination layer. You build a feature as a vertical slice: one Java controller and one HTML
template can define a working interactive page.

That vertical-slice model also fits Microfrontend Architecture. A service can own not only its business logic, but also
the page or frontlet that presents that logic, while XIS composes those pieces into one browser application.

The Gradle plugin adds comfortable tools for that workflow: it can generate starter templates and starter integration
tests, configure the matching XIS test starter automatically, run XIS-specific validation checks for local development
or Jenkins pipelines, and build or run standalone XIS Boot applications.

Optional persistence modules add the same lightweight style to data access: `xis-sql` provides JDBC repository support
with transactions, and `xis-mongodb` provides MongoDB document repositories plus change-stream callbacks for
event-driven UI refreshes.

## Minimal Page

`src/main/java/example/HelloPage.java`

```java
package example;

import one.xis.Action;
import one.xis.ModelData;
import one.xis.Page;
import one.xis.WelcomePage;

@WelcomePage
@Page("/hello.html")
class HelloPage {

    private int counter;

    @ModelData
    String greeting() {
        return "Hello XIS";
    }

    @ModelData
    int count() {
        return counter;
    }

    @Action
    void increment() {
        counter++;
    }
}
```

`src/main/java/example/HelloPage.html`

```html
<!DOCTYPE html>
<html xmlns:xis="https://xis.one/xsd" lang="en">
<head>
    <meta charset="UTF-8">
    <title>Hello XIS</title>
</head>
<body>
<h1>${greeting}</h1>
<p>Count: ${count}</p>
<button xis:action="increment">Increment</button>
</body>
</html>
```

The button calls the Java `increment` action. XIS transports the interaction and refreshes the rendered state without
requiring a REST endpoint or a handwritten frontend client.

## Runtime Choices

Most applications should choose one runtime:

- `xis-spring` for Spring Boot applications
- `xis-boot` for standalone XIS applications without Spring Boot
- `xis-boot-native` for XIS Boot applications that should be built and deployed as GraalVM native executables

The selected runtime brings the controller annotations such as `@Page`, `@Frontlet`, `@ModelData`, `@FormData`,
`@Action`, and `@PathVariable`.

`xis-boot-native` is the cloud-native path for small containers, fast startup, and deployments where you want a native
binary without building your own discovery, reflection, resource, and proxy infrastructure.

XIS also supports Groovy 4+ controllers and form DTOs. This is useful when a team wants a lighter JVM syntax while still
using the same XIS annotations, templates, forms, validation, and runtime behavior. Java remains the primary and fastest
path; Groovy support does not add runtime overhead to Java applications.

## Feature Index

- [Pages, model data, actions, shared values, frontlets, frontlet containers, and includes](docs/user/core-model.md)
- [Template expressions, property/index access, operators, conditions, iteration, built-in EL functions, and formatting helpers](docs/user/templates.md)
- [XIS tags and attributes for navigation, actions, drag and drop, modals, frontlets, forms, validation messages, dynamic classes, client state, and raw content](docs/user/tags-and-attributes.md)
- [Navigation responses for pages, frontlets, modals, deep links, action buttons, action links, and form actions](docs/user/navigation.md)
- [Routers for server-side navigation decisions before a page, frontlet, or modal is selected](docs/user/routers.md)
- [Forms, validation, nested objects, lists, type conversion, custom formatters, validation highlighting, and validation messages](docs/user/forms-and-validation.md)
- [Modal dialogs with their own controller, template, model data, form data, actions, validation, and parent reload behavior](docs/user/modals.md)
- [Drag and drop actions with integration-test support](docs/user/drag-and-drop.md)
- [Refresh and push events for already open pages and frontlets, including targeted client updates](docs/user/events.md)
- [Client state with localStorage, sessionStorage, clientStorage, and storage bindings](docs/user/annotations.md#browser-storage-parameters)
- [SQL repositories, entity mapping, relations, CRUD, explicit SQL, transactions, save/delete cascades, functions, and stored procedures](docs/user/sql.md)
- [MongoDB document repositories, JSON queries, document mapping, and change streams](docs/user/mongodb.md)
- [Security with local authentication, roles, template visibility, external OpenID Connect providers, XIS as IDP, and distributed SSO](docs/user/security.md)
- [Microfrontend Architecture for distributed XIS applications](docs/user/advanced/microfrontend-architecture.md)
- [Groovy 4+ support for controllers and forms](docs/user/groovy.md)
- [Gradle tools for template generation, test generation, validation, runnable jars, and local runs](docs/user/gradle-plugin.md)
- [Cloud Native deployment with XIS Boot Native, GraalVM native-image, and native database modules](docs/user/cloud-native.md)
- [Integration tests, generated tests, E2E tests, and the integration-test browser model](docs/user/examples-and-tests.md)
- [Custom JavaScript extensions, custom EL functions, global browser behavior, and form submission from JavaScript](docs/user/advanced/custom-javascript.md)
- [Custom proxies for generated clients, repositories, and infrastructure extensions](docs/user/advanced/custom-proxies.md)
- [XIS Theme for generated standard pages, forms, navigation, dashboards, panels, messages, and basic layout](docs/user/advanced/theme.md)

## Documentation

The canonical user documentation lives in this repository:

- [Quickstart](docs/user/quickstart.md)
- [Runtime and dependency model](docs/user/runtime-and-dependencies.md)
- [Gradle plugin and tools](docs/user/gradle-plugin.md)
- [Groovy support](docs/user/groovy.md)
- [Template location and mapping](docs/user/template-location-and-mapping.md)
- [Core model: pages, frontlets, includes, actions](docs/user/core-model.md)
- [Annotation reference](docs/user/annotations.md)
- [Template syntax](docs/user/templates.md)
- [Tags and attributes](docs/user/tags-and-attributes.md)
- [Request lifecycle](docs/user/request-lifecycle.md)
- [Navigation and responses](docs/user/navigation.md)
- [Routers](docs/user/routers.md)
- [Forms and validation](docs/user/forms-and-validation.md)
- [Modals](docs/user/modals.md)
- [SQL](docs/user/sql.md)
- [MongoDB](docs/user/mongodb.md)
- [Drag and drop](docs/user/drag-and-drop.md)
- [Events](docs/user/events.md)
- [Security](docs/user/security.md)
- [Examples and tests](docs/user/examples-and-tests.md)
- [Advanced topics](docs/user/advanced/README.md)

**Working reference:** use the [Documentation map](docs/README.md) when you are actively building with XIS. It is the
detailed clickable index for features, annotations, tags, attributes, persistence annotations, security hooks, and
Gradle tasks.

If you want quick results, start with the Quickstart and then read the core user documentation. Advanced topics cover
optional capabilities such as security integration, Microfrontend Architecture, customization, and client-side storage.

Module README files are mostly framework-developer notes. Start with the files above when learning XIS as a user.

## Documentation Principle

This project treats documentation examples as part of the public API. A documented example should be copyable into a
project and should become test-covered when the related API stabilizes.

When changing public XIS behavior, update the user documentation and add or adapt an executable example.

## License

Apache License 2.0
