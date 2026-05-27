# Template Location And Mapping

[Documentation map](../../README.md)

Every page, frontlet, and include has Java code and an HTML template. XIS looks for the template by convention first, and
you only need an annotation when you want to override that convention.

## Default Location

By default, put the HTML file next to the Java controller in the same package:

```text
src/main/java/
  example/products/
    ProductPage.java
    ProductPage.html
```

For a controller class named `ProductPage`, XIS looks for `ProductPage.html` in the same package. This works for files
kept under `src/main/java` because the XIS Gradle plugin copies HTML templates into the application resources during the
build.

This side-by-side layout is the recommended XIS style. Most changes touch the controller and template together, and the
project tree immediately shows which Java class owns which HTML file.

During development, XIS resolves side-by-side templates from `src/main/java` before it falls back to copied build
resources. When an HTML template is changed on disk, the next browser request reparses it if the file timestamp changed.
That means normal template edits usually only need a browser reload, not a Java restart or a Gradle resource-copy step.

The same package can also be mirrored under `src/main/resources`:

```text
src/main/resources/
  example/products/
    ProductPage.html
```

Use the side-by-side `src/main/java` style when you want the controller and template to be easy to find together. Use
`src/main/resources` when your project convention keeps all resource files there.

If the same resource path exists in both source roots, `src/main/java` wins. This keeps the controller-near template as
the active development file.

## Generate Missing Templates And Tests

The [XIS Gradle plugin and tools](gradle-plugin.md) can generate missing template files for page and frontlet controllers:

```bash
./gradlew xisTemplates
```

The generated files are only a starting point. They are written to the package of the Java controller, so the project
tree shows which template belongs to which controller.

It is usually better to write the controller skeleton before running the generator. If the controller already contains
its `@ModelData`, `@FormData`, and `@Action` methods, `xisTemplates` can create a more useful template: model data is
rendered with expressions or `xis:foreach`, form data becomes a bound form with validation message placeholders, and
actions become buttons. In other words, the generated template is not just based on the controller class name; it tries
to mirror the controller methods that define the page contract.

For example, a controller with a `@FormData("customer")` method and a matching save action will produce a form using
`xis:binding="customer"`, field bindings, error bindings, and a submit button. You still edit the generated HTML, but
you start with the important XIS wiring already in place.

The same plugin can generate missing integration-test skeletons for page controllers:

```bash
./gradlew xisTests
```

The generated tests are written under `src/test/java` in the same package as the page controller. They use
`@XisBootTest`, register the page controller in the XIS test context, and open the page by URL. The XIS
Gradle plugin adds the required test starter automatically; do not add `xis-test` or `xis-boot-starter-test` again in a
normal plugin-based build. Existing files are never overwritten, so both tasks are safe to run repeatedly while you add
controllers.

You can run both tasks together after creating a page class:

```bash
./gradlew xisTemplates xisTests
```

That gives you the two files a new page usually needs next: the HTML template and a first executable test. For best
results, first sketch the controller methods that should exist, then generate the template and test, and then refine both
files into the final behavior.

## Explicit Template With `@HtmlFile`

Use `@HtmlFile` when the template name or location should not follow the default convention. The annotation works for
templates copied from `src/main/java` and for templates kept directly in `src/main/resources`; at runtime both are
classpath resources.

```java
package example.products;

import one.xis.HtmlFile;
import one.xis.Page;

@Page("/products.html")
@HtmlFile("ProductList.html")
class ProductPage {
}
```

A relative `@HtmlFile` path is resolved from the controller package. The example above looks for:

```text
example/products/ProductList.html
```

An absolute path starts with `/` and is resolved from the classpath resource root:

```java
package example.products;

import one.xis.HtmlFile;
import one.xis.Page;

@Page("/products.html")
@HtmlFile("/templates/ProductList.html")
class ProductPage {
}
```

This looks for:

```text
templates/ProductList.html
```

Use an absolute path when a template should live in a shared folder independent of the controller package. Use a relative
path when the template should still be organized near the controller package, but should not use the controller class
name.

## Shared Templates

Several controllers can use the same template by pointing them to the same `@HtmlFile`.

```java
package example.products;

import one.xis.HtmlFile;
import one.xis.Page;

@Page("/products.html")
@HtmlFile("/templates/catalog.html")
class ProductPage {
}
```

```java
package example.offers;

import one.xis.HtmlFile;
import one.xis.Page;

@Page("/offers.html")
@HtmlFile("/templates/catalog.html")
class OfferPage {
}
```

This is useful when different controllers expose the same model shape and should render with the same HTML.

## Default Templates

`@DefaultHtmlFile` is mainly useful for reusable library controllers that provide a framework default while still
allowing an application to override the template with `@HtmlFile`. See
[Reusable web artifacts](advanced/reusable-web-artifacts.md) for the full pattern.

```java
package example.library;

import one.xis.DefaultHtmlFile;
import one.xis.Page;

@Page("/profile.html")
@DefaultHtmlFile("/default-profile.html")
class ProfilePage {
}
```

Application code normally uses the default convention or `@HtmlFile`.
