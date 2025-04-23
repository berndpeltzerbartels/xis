# Quickstart Guide – Action Return Values

XIS action methods can return different types of values to control what happens after the action is executed. This lets
you:

- Navigate to a different page
- Replace a widget in a container
- Stay on the same view

These return types work the same in **pages** and **widgets**:

- A **widget** can trigger a page change or replace a widget
- A **page** can trigger a page change or replace a widget (in the latter case only via `WidgetResponse`, and
  `targetContainer` must be set)

This also works in microfrontend scenarios where page and widget are managed separately.

---

## ✅ What you’ll build

Four scenarios:

1. Action with no return → page stays the same
2. Action returns a new page class → navigate to that page
3. Action returns a widget class → widget container swaps its content
4. Use a response object (PageResponse or WidgetResponse) with parameters and control

---

## 1. No return → Stay on current page

```java

@Action("save")
public void save() {
    // save something...
    // no return → model may be updated, and UI will reflect new values via @ModelData
}
```

If your action modifies model state, the UI will update accordingly — even without returning a value.

---

## 2. Navigate to a different page

```java

@Action("goHome")
public Class<?> goHome() {
    return HomePage.class;
}
```

This works from both widgets and page controllers.

---

## 3. Replace widget in a container

```html

<xis:widget-container container-id="main" default-widget="WidgetA"/>
```

```java

@Action("switch")
public Class<?> switchWidget() {
    return WidgetB.class;
}
```

Works from both widgets and pages. If the action is in a page controller, the target container must be set explicitly
using `WidgetResponse`:

```java

@Action("replace")
public Response replaceWidget() {
    return new WidgetResponse(ChartWidget.class)
            .targetContainer("main");
}
```

---

## 4. Return a response object with parameters

Use `PageResponse` or `WidgetResponse` to pass parameters or set the target container:

```java

@Action("toDetail")
public Response goToDetail() {
    return new PageResponse(DetailPage.class)
            .pathVariable("id", 42)
            .urlParameter("tab", "info");
}
```

```java

@Action("replace")
public Response replaceWidget() {
    return new WidgetResponse(ChartWidget.class)
            .targetContainer("sidebar")
            .widgetParameter("type", "pie");
}
```

---

## ✅ Summary

- `void` → model may change, and updated values will be reflected via `@ModelData`
- `Class<?>` → navigate or swap widget
- `PageResponse` and `WidgetResponse` → for parameters and control
- Works from both widgets and page controllers
- Widget replacement from a page controller requires `WidgetResponse` with `targetContainer`

Next up: Forms and user input

