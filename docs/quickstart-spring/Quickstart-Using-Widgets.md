# Quickstart Guide – Using Widgets

In XIS, you can use `@Page` to build full pages — and everything you've seen so far works this way. However, for *
*modular and reusable UIs**, it's often better to use **widgets** instead.

Widgets allow you to:

- Split complex screens into manageable components
- Reuse logic and templates across pages
- Compose your app like building blocks

Templates for widgets are defined **exactly the same way** as for pages — HTML fragments with `${...}` expressions.

---

## ✅ What you’ll build

A page that includes a greeting widget.

Project structure:

```
my-xis-app/
├── src/
│   └── main/
│       └── java/
│           └── com/example/demo/
│               ├── AppLauncher.java
│               ├── mainpage/
│               │   ├── MainPage.java
│               │   └── MainPage.html
│               └── greeting/
│                   ├── GreetingWidget.java
│                   └── GreetingWidget.html
```

---

## 1. The Widget HTML

`src/main/java/com/example/demo/greeting/GreetingWidget.html`

```html

<div>
    <h1>${greeting}</h1>
</div>
```

---

## 2. The Widget Java Class

`src/main/java/com/example/demo/greeting/GreetingWidget.java`

```java
package com.example.demo.greeting;

import one.xis.Widget;
import one.xis.ModelData;

@Widget
public class GreetingWidget {

    @ModelData("greeting")
    public String getGreeting() {
        return "Hello from a Widget!";
    }
}
```

---

## 3. Embedding the Widget in a Page

### a) Using a Widget Container (recommended)

`src/main/java/com/example/demo/mainpage/MainPage.html`

```html
<!DOCTYPE html>
<html xmlns:xis="https://xis.one/xsd">
<head>
    <title>WidgetPage</title>
</head>
<body>
<div>
    <xis:widget-container default-widget="GreetingWidget" container-id="container"/>
</div>
</body>
</html>
```

> 💡 Note: The default-widget ID is the **simple class name** of the widget. It must be unique across your app. If
> needed, you can override it via `@Widget("CustomId")`, but in most cases this is not necessary.

---

## 4. The Page Controller

`src/main/java/com/example/demo/mainpage/MainPage.java`

```java
package com.example.demo.mainpage;

import one.xis.Page;
import one.xis.WelcomePage;

@WelcomePage
@Page("/mainpage/MainPage.html")
public class MainPage {
    // no logic needed — this page embeds the widget only
}
```

---

## ✅ Summary

- `@Widget` classes can be reused in multiple pages
- Templates and binding work just like for `@Page`
- Widgets are auto-discovered automatically — you don't need `@Component`
- Use `<xis:widget-container>` to include them in a page

Widgets help you organize your UI into **clean, composable units** — great for maintainability and teamwork.
