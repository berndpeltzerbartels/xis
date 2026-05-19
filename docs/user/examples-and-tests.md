# Examples and Tests

[Documentation map](../README.md)

XIS documentation examples are part of the public API. A user should be able to copy a documented example into an
application and use it as a starting point.

XIS also provides fast integration tests. These tests create a lightweight XIS application context, render pages through
the same controller/template lifecycle as the runtime, and let you inspect and interact with the resulting document.

If your build uses the [XIS Gradle plugin and tools](gradle-plugin.md), the XIS test starter and the JUnit 5 test
platform are configured for you. Do not add `xis-test` or `xis-boot-starter-test` yourself in that case; the plugin keeps
the test dependency on the same version as the plugin.

## Generate Starter Tests

If your build uses the XIS Gradle plugin, the `xisTests` task can generate missing integration-test skeletons for page
controllers:

```bash
./gradlew xisTests
```

For a page controller such as:

```text
src/main/java/example/dashboard/DashboardPage.java
```

the task creates:

```text
src/test/java/example/dashboard/DashboardPageTest.java
```

The generated test uses `@XisBootTest`, gets an `IntegrationTestContext` field, opens the page URL from `@Page`, and
starts with a minimal document assertion. It does not overwrite an existing test file:

```java
package example.dashboard;

import one.xis.boot.test.XisBootTest;
import one.xis.context.IntegrationTestContext;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@XisBootTest
class DashboardPageTest {

    private IntegrationTestContext context;

    @Test
    void test() {
        var client = context.openPage("/dashboard.html");

        assertNotNull(client.getDocument());
    }
}
```

When the XIS Gradle plugin generated this test, the required `xis-boot-starter-test` dependency has already been added by
the plugin.

This works well with the template generator:

```bash
./gradlew xisTemplates xisTests
```

A practical TDD workflow is:

1. Create the page class with `@Page`.
2. Sketch the `@ModelData`, `@FormData`, and `@Action` methods that define the page contract.
3. Run `./gradlew xisTemplates xisTests`.
4. Edit the generated test so it describes the UI behavior.
5. Implement the controller, services, and HTML template until the test passes.

Because these tests mostly interact with the rendered UI document, the generated test normally compiles even while the
page still has missing behavior.

## Test A Page

For plugin-based builds, the most compact style is `@XisBootTest`. The extension creates the integration-test context,
injects it into an `IntegrationTestContext` field, and supports XIS context annotations on the test class.

```java
package example.products;

import one.xis.boot.test.XisBootTest;
import one.xis.context.IntegrationTestContext;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@XisBootTest
class ProductPageTest {

    private IntegrationTestContext context;

    @Test
    void opensProductPage() {
        var client = context.openPage("/products/42.html");
        var document = client.getDocument();

        assertNotNull(document);
        assertEquals("Desk", document.getElementByTagName("h1").getInnerText());
    }
}
```

For small explicit tests, or for builds that do not use the plugin test starter, create an `IntegrationTestContext`
yourself with the page controller and the services it needs. You can pass classes, which XIS will instantiate, or
ready-made instances such as mocks.

```java
package example.products;

import one.xis.context.IntegrationTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProductPageTest {

    private IntegrationTestContext context;
    private ProductService productService;

    @BeforeEach
    void setUp() {
        productService = new ProductService() {
            @Override
            public Product findById(long id) {
                return new Product(id, "Desk", "129.00");
            }
        };
        context = IntegrationTestContext.builder()
                .withSingleton(ProductPage.class)
                .withSingleton(productService)
                .build();
    }

    @Test
    void rendersProductFromUrl() {
        var result = context.openPage("/products/42.html");
        var document = result.getDocument();

        assertEquals("Desk", document.getElementByTagName("h1").getInnerText());
        assertEquals("129.00", document.getElementById("price").getInnerText());
    }
}
```

The matching controller can be ordinary application code:

```java
package example.products;

import one.xis.ModelData;
import one.xis.Page;
import one.xis.PathVariable;

@Page("/products/{id}.html")
class ProductPage {

    private final ProductService productService;

    ProductPage(ProductService productService) {
        this.productService = productService;
    }

    @ModelData("product")
    Product product(@PathVariable("id") long id) {
        return productService.findById(id);
    }
}
```

```html
<!DOCTYPE html>
<html xmlns:xis="https://xis.one/xsd" lang="en">
<head><title>${product.name}</title></head>
<body>
    <h1>${product.name}</h1>
    <p id="price">${product.price}</p>
</body>
</html>
```

Prefer opening pages by URL when the URL matters. It tests the `@Page` mapping, path variables, and query parameters.
Opening by class is useful for very small tests where the URL is not part of the behavior.

## Interact With The Document

The rendered document supports DOM-style lookup and interaction. After opening a page, navigate by clicking links or
buttons instead of calling `openPage` again in the same flow.

```java
@Test
void actionUpdatesThePage() {
    var result = context.openPage("/counter.html");
    var document = result.getDocument();

    assertEquals("0", document.getElementById("count").getInnerText());

    document.getElementById("increment").click();

    assertEquals("1", document.getElementById("count").getInnerText());
}
```

Forms can be tested the same way: set form control values, click the submit button, and assert the rendered validation
or navigation result.

## Test Drag And Drop

Use `DragAndDrop` from `xis-test` for XIS drag and drop actions. The helper sends `dragstart`, `dragover`, and `drop`
with one shared `DataTransfer` object, so the test follows the same public interaction model as the browser feature.

```java
import one.xis.test.dom.DragAndDrop;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Test
void dropCallsTheAction() {
    var result = context.openPage("/drag-drop.html");
    var document = result.getDocument();

    new DragAndDrop(
            document.getElementById("source"),
            document.getElementById("target")
    ).doDragAndDrop();

    assertEquals("a2", boardService.lastFrom());
    assertEquals("a4", boardService.lastTo());
}
```

The matching template can use normal `xis:drag` and `xis:drop` attributes:

```html
<div id="source" xis:drag="from:${source}">Source</div>
<div id="target" xis:drop="move(from, to='${target}')">Target</div>
```

See [Drag and drop](drag-and-drop.md) for the template syntax.

## Build The Test Context

Register individual classes or instances when you want a small, explicit test:

```java
context = IntegrationTestContext.builder()
        .withSingleton(ProductPage.class)
        .withSingleton(productService)
        .build();
```

Use package scanning when the test should load a larger slice:

```java
context = IntegrationTestContext.builder()
        .withBasePackageClass(Application.class)
        .build();
```

You can also include and exclude packages explicitly:

```java
context = IntegrationTestContext.builder()
        .withPackage("example.products")
        .withoutPackage("example.external")
        .build();
```

The XIS test context recognizes XIS context annotations and common Spring stereotypes for component discovery, but it is
not a real Spring application context. It is deliberately smaller and faster.

## Authentication In Tests

For protected pages, provide a logged-in user in the test context:

```java
@BeforeEach
void setUp() {
    var user = new UserInfoImpl();
    user.setUserId("u-1");
    user.setName("Ada");
    user.setRoles(Set.of("USER", "ADMIN"));

    context = IntegrationTestContext.builder()
            .withSingleton(AccountPage.class)
            .withLoggedInUser(user, "test-password")
            .build();
}
```

Use this for pages that inject `@UserId` or require `@Roles`.

## Test Dependencies Without The Plugin

With the XIS Gradle plugin, the generated-test dependency is automatic. Without the plugin, add the starter explicitly:

`build.gradle`

```groovy
plugins {
    id "java"
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation "one.xis:xis-boot-starter-test:0.12.0"
}
```

Use this for `@XisBootTest` and generated-test style. If you only use the lower-level `IntegrationTestContext` builder
and configure JUnit yourself, `one.xis:xis-test` is enough.

## What To Test Where

Use `xis-test` for fast checks of controller behavior, template rendering, forms, validation, navigation decisions, and
frontlet composition.

Use browser E2E tests when browser behavior itself matters: real CSS layout, asset loading, OpenID Connect redirects,
SSE refresh events, or JavaScript behavior that cannot be represented well by the integration-test DOM.

## Rule

For public behavior, documentation should move toward this standard:

```text
documented API
        |
        +-- copyable Java example
        +-- matching HTML template example
        +-- test or example project that exercises the behavior
```

Not every migrated example is test-backed yet. Treat that as documentation debt, not as an acceptable permanent state.

## What Makes a Good Example

A good XIS example names the files:

```text
src/main/java/example/products/ProductPage.java
src/main/java/example/products/ProductPage.html
```

It shows the complete public API interaction:

```java
@Page("/products/{id}.html")
class ProductPage {

    @ModelData
    Product product(@PathVariable("id") long id) {
        return productService.findById(id);
    }
}
```

```html
<h1>${product.name}</h1>
```

It avoids pseudocode for annotation names, template attributes, binding names, and URL shapes.

## Test Coupling

When an API is stable, prefer one of these approaches:

- Put a small executable example under an `examples/` directory and test it.
- Add an integration test that mirrors the documented Java and HTML.
- Extract code blocks from Markdown only if the extraction workflow is reliable enough to maintain.

The first practical default is to write real example files and reference them from docs. Markdown extraction can come
later if it proves useful.

## Change Discipline

When changing public behavior:

- update the implementation
- update or add tests
- update the user docs
- update examples so copied code still works

If the documentation and the tests disagree, treat that as a bug in the project, not as a cosmetic documentation issue.
