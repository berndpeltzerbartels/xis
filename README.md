# XIS – Java Web Framework

**Server-side rendered HTML meets Single Page Application**

XIS is a lightweight Java web framework that brings back the simplicity of server-side rendering while keeping the
smooth user experience of modern SPAs.

## Why XIS?

Most Java web frameworks force you to choose between:

- Server-side rendering (full page reloads, no smooth navigation)
- REST APIs + JavaScript framework (complex, lots of boilerplate, coordination overhead)

XIS gives you both: Write plain Java controllers and plain HTML templates, get SPA navigation automatically.

**No REST endpoints. No fetch() calls. No Redux. Just Java and HTML.**

## Hello World in 5 Minutes

**1. Add the dependency:**

```groovy
plugins {
    id 'one.xis.plugin' version '0.2.0'
}
```

**2. Create a page controller:**

```java
package com.example;

import one.xis.Page;
import one.xis.ModelData;
import one.xis.Action;

@Page("/hello.html)
public class HelloPage {

    private int counter = 0;

    @ModelData
    public String message() {
        return "Hello World!";
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

**3. Create the HTML template (same package):**

```html

<html xmlns:xis="https://xis.one/xsd" lang="en">
<head>
    <title>Example</title>
</head>
<body>
<h1>${message}</h1>
<p>Counter: ${count}</p>
<button xis:action="increment">Click me</button>
</body>
</html>
```

**That's it!** The button calls your Java method without page reload. Navigation between pages happens smoothly. No
JavaScript needed.

## How Does It Work?

XIS automatically:

- Maps URLs to page controllers
- Renders HTML templates with your data
- Handles form submissions and button clicks
- Updates only changed parts of the page (SPA behavior)
- Manages browser history and deep linking

All communication between frontend and backend is handled by the framework. You just write business logic.

## Key Features

✅ **Plain Java + Plain HTML** – No template language to learn, no DSL  
✅ **Single Page Navigation** – Smooth transitions without full reloads  
✅ **Zero JavaScript** – Actions, forms, navigation work declaratively  
✅ **Convention over Configuration** – HTML files next to Java classes  
✅ **Testable** – Pure Java controllers, easy to unit test  
✅ **Spring & Micronaut** – Works with your favorite framework  
✅ **Micro-Frontend Ready** – Built for vertical modularity

## Real-World Example

```java

@Page("/products/{id}")
public class ProductDetailPage {

    @Inject
    ProductService productService;

    @ModelData
    public Product product(@PathVariable("id") Long id) {
        return productService.findById(id);
    }

    @Action
    public Class<?> deleteProduct() {
        productService.delete(product.getId());
        return ProductListPage.class;  // Navigate to list
    }
}
```

```html

<html xmlns:xis="https://xis.one/xsd" lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0
    <h1>${product.name}</h1>

    <button xis:action=" deleteProduct
    ">Delete Product</button>

    <a xis:page="ProductListPage">Back to list</a>
</html>
```

Navigation, confirmation dialogs, and page transitions all work without writing a single line of JavaScript.

## Learn More

📖 **[Documentation](https://xis.one/docs/introduction.html)** – Complete guide  
🚀 **[Quickstart](https://xis.one/quickstart/installation.html)** – Get started in 10 minutes  
📚 **[JavaDoc](https://xis.one/docs/introduction.html)** – API reference

## License

Apache License 2.0
