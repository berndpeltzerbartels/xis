# 4. Pagelets (`@Pagelet`)

Pagelets are modular controller units in XIS. Each pagelet is bound to a specific fragment of your HTML and handled by a
Java class annotated with `@Pagelet`.

Unlike frontend UI components, pagelets are **server-side logic fragments**. They are typically instantiated once per
page (singleton) and are meant to encapsulate the behavior of a clearly scoped part of the layout.

---

## Basic Structure

Each pagelet consists of:

- A Java class annotated with `@Pagelet`
- A matching HTML file
- Optional model, request, and action methods

Example:

```java

@Pagelet
public class WeatherPagelet {

    @ModelData("currentWeather")
    public WeatherData currentWeather() {
        return weatherService.loadForToday();
    }
}
```

```html
<!-- WeatherWidget.html -->
<div class="weather">
    <p>Temperature: ${currentWeather.temperature}°C</p>
</div>
```

---

## File Matching

- The HTML file must match the class name and package.
- `WeatherWidget.java` ↔ `WeatherWidget.html`
- XIS resolves this automatically unless overridden via `@HtmlFile(...)`.

---

## Embedding Pagelets

Pagelets are loaded into a designated container within a page or another pagelet.

To define a target container in your HTML:

```html
...
<xis:pagelet-container default-pagelet="WeatherPagelet"/>
...
```

From the controller, you can load a pagelet into that container:

In earlier versions, the tag was called `widget-container` – in future versions, it will consistently be
`pagelet-container`.

---

## Lifecycle Methods

Pagelets support all major lifecycle annotations:

- `@ModelData`
- `@FormData`
- `@RequestScope`

They behave exactly as in pages and can be used for data preparation, request handling, and scoped dependencies.

---

## Summary

- Pagelets define logic for HTML fragments inside a page or another pagelet
- Instantiated once per use, usually as a singleton
- Loaded via `<xis:pagelet-container>` and `WidgetResponse.pagelet(...)`
- Use the same annotations and lifecycle as pages
- Ideal for modular, structured server-side rendering

---

→ [Continue to Chapter 5: Data & Parameters](05-data.md)
