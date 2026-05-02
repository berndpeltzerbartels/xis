# Examples and Tests

XIS documentation examples are part of the public API. A user should be able to copy a documented example into an
application and use it as a starting point.

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
public class ProductPage {

    @ModelData
    public Product product(@PathVariable("id") long id) {
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
