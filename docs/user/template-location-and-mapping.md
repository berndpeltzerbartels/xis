# Template Location And Mapping

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

The same package can also be mirrored under `src/main/resources`:

```text
src/main/resources/
  example/products/
    ProductPage.html
```

Use the side-by-side `src/main/java` style when you want the controller and template to be easy to find together. Use
`src/main/resources` when your project convention keeps all resource files there.

## Generate Missing Templates

The XIS Gradle plugin can generate missing template files for page and frontlet controllers:

```bash
./gradlew templates
```

The generated files are only a starting point. They are written to the package of the Java controller, so the project
tree shows which template belongs to which controller.

## Explicit Template With `@HtmlFile`

Use `@HtmlFile` when the template name or location should not follow the default convention.

```java
package example.products;

import one.xis.HtmlFile;
import one.xis.Page;

@Page("/products.html")
@HtmlFile("ProductList.html")
public class ProductPage {
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
public class ProductPage {
}
```

This looks for:

```text
templates/ProductList.html
```

## Shared Templates

Several controllers can use the same template by pointing them to the same `@HtmlFile`.

```java
package example.products;

import one.xis.HtmlFile;
import one.xis.Page;

@Page("/products.html")
@HtmlFile("/templates/catalog.html")
public class ProductPage {
}
```

```java
package example.offers;

import one.xis.HtmlFile;
import one.xis.Page;

@Page("/offers.html")
@HtmlFile("/templates/catalog.html")
public class OfferPage {
}
```

This is useful when different controllers expose the same model shape and should render with the same HTML.

## Default Templates

`@DefaultHtmlFile` is mainly useful for reusable library controllers that provide a framework default while still
allowing an application to override the template with `@HtmlFile`.

```java
package example.library;

import one.xis.DefaultHtmlFile;
import one.xis.Page;

@Page("/profile.html")
@DefaultHtmlFile("/default-profile.html")
public class ProfilePage {
}
```

Application code normally uses the default convention or `@HtmlFile`.
