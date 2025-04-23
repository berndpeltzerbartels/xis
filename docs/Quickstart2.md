# Quickstart Guide – Simple Actions

> 💡 You can also use `@Action` and `<xis:a>` inside widgets. The syntax and behavior are exactly the same as in pages.

In this part of the Quickstart, you'll learn how to trigger Java methods from the frontend using **action links**. This
is the simplest way to implement interactions in XIS — no JavaScript required.

---

## ✅ What you’ll build

A page that shows a value from the controller and includes a clickable link that sends parameters to the backend.

---

## 1. The Page Template

`src/main/java/com/example/demo/action/ActionPage.html`

```html
<!DOCTYPE html>
<html xmlns:xis="https://xis.one/xsd">
<head>
    <title>ActionPage</title>
</head>
<body>
<h1>Action Example</h1>
<p>Current: ${current}</p>

<xis:a action="change" id="action-link">
    Click Me
    <xis:parameter name="value" value="Hello from client"/>
</xis:a>
</body>
</html>
```

> 💡 `xis:a` is shorthand for `<a xis:action="...">`. You can also use `<button>` or regular `<a>` tags with
`xis:action`.

You can also use dynamic values like `${...}` for the action name or parameter values.

---

## 2. The Page Controller

`src/main/java/com/example/demo/action/ActionPage.java`

```java
package com.example.demo.action;

import one.xis.Page;
import one.xis.Action;
import one.xis.ActionParameter;
import one.xis.ModelData;

@Page("/action-example.html")
public class ActionPage {

    private String current = "-";

    @ModelData("current")
    public String getCurrent() {
        return current;
    }

    @Action("change")
    public void updateValue(@ActionParameter("value") String value) {
        this.current = value;
    }
}
```

---

## ✅ Summary

- `@Action("...")` binds Java methods to frontend events
- Use `<xis:a>` or `<a xis:action="...">` to call them
- Parameters are passed via `<xis:parameter>`
- You can use variables like `${actionName}` or `${value}` for dynamic behavior

Next up: Form-based interactions and input handling.

