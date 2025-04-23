# Quickstart Guide – Parameters

XIS supports multiple types of parameters in both **pages** and **widgets**. You can combine values from the **page URL
**, the **query string**, and the **widget invocation** itself. This section shows each case in a simple, focused
example.

---

## ✅ What you’ll build

Three examples:

1. Reading a value from the URL path
2. Reading a query string parameter
3. Passing a value directly to a widget

---

## 1. Parameters from the URL path

URL: `/product/42/details.html`

```java

@Page("/product/{id}/details.html")
public class ProductPage {

    @ModelData("id")
    public int id(@PathVariable("id") int id) {
        return id;
    }
}
```

---

## 2. Parameters from the query string

URL: `/search.html?term=xis`

```java

@Page("/search.html")
public class SearchPage {

    @ModelData("term")
    public String term(@URLParameter("term") String term) {
        return term;
    }
}
```

---

## 3. Parameters passed to a widget

`MainPage.html`

```html

<xis:widget-container default-widget="GreetingWidget?name=Taylor" container-id="box"/>
```

Widget controller:

```java

@Widget
public class GreetingWidget {

    @ModelData("greeting")
    public String greeting(@Parameter("name") String name) {
        return "Hello, " + name + "!";
    }
}
```

Widget template:

```html
<h2>${greeting}</h2>
```

> 💡 The widget parameter `name=Taylor` is passed via the URL (`?name=...`) and received using `@Parameter`. This is
> equivalent to placing `<xis:parameter name="name" value="Taylor"/>` inside the `xis:widget-container`.

---

## ✅ Summary

- Use `@PathVariable`, `@URLParameter`, and `@Parameter` depending on the source of the data
- All three work in both pages and widgets
- `@Parameter` values are passed explicitly in the widget declaration (e.g., `GreetingWidget?name=...`) or via
  `<xis:parameter>`
- `@URLParameter` values come from the query string
- `@PathVariable` values are defined in the page path
- You can expose parameters in the UI with `@ModelData`, but this is optional

Next up: Action return values and navigation

