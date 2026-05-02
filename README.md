# XIS

XIS is a Java web framework for building interactive web applications with plain Java and plain HTML.

The programming model is server-driven: Java controllers describe pages, frontlets, actions, form data, and model data.
HTML templates describe the UI. XIS handles browser-server communication, navigation, form submission, validation
feedback, frontlet updates, and SPA-style page transitions.

## Why XIS Exists

Many web applications end up with the same split:

- Java or Kotlin backend controllers
- REST endpoints and DTOs
- JavaScript or TypeScript API clients
- frontend state management
- duplicated validation and navigation rules

XIS removes most of that coordination layer. You build a feature as a vertical slice: one Java controller and one HTML
template can define a working interactive page.

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
public class HelloPage {

    private int counter;

    @ModelData
    public String greeting() {
        return "Hello XIS";
    }

    @ModelData
    public int count() {
        return counter;
    }

    @Action
    public void increment() {
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

The controller annotations such as `@Page`, `@Frontlet`, `@ModelData`, `@FormData`, `@Action`, and `@PathVariable` live
in `xis-controller-api`, but application projects normally receive them transitively through the selected runtime.

## Documentation

The canonical user documentation lives in this repository:

- [Quickstart](docs/user/quickstart.md)
- [Core model: pages, frontlets, includes, actions](docs/user/core-model.md)
- [Navigation and responses](docs/user/navigation.md)
- [Template syntax](docs/user/templates.md)
- [Tags and attributes](docs/user/tags-and-attributes.md)
- [Forms and validation](docs/user/forms-and-validation.md)
- [Runtime and dependency model](docs/user/runtime-and-dependencies.md)
- [Examples and tests](docs/user/examples-and-tests.md)
- [Advanced topics](docs/user/advanced/README.md)
- [Documentation map](docs/README.md)

If you want quick results, start with the Quickstart and then read the core user documentation. Advanced topics cover
optional capabilities such as security integration, distributed applications, customization, client-side storage, and
refresh events.

Module README files are mostly framework-developer notes. Start with the files above when learning XIS as a user.

## Documentation Principle

This project treats documentation examples as part of the public API. A documented example should be copyable into a
project and should become test-covered when the related API stabilizes.

When changing public XIS behavior, update the user documentation and add or adapt an executable example.

## Current Status

XIS is actively evolving. Spring Boot and XIS Boot are the main supported runtime paths. Some modules exist for planned
or experimental architecture, including distributed and micro-frontend scenarios, but the default deployment model is a
same-origin application.

## License

Apache License 2.0
